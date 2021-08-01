import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class MatrixIHandler implements IHandler {
    private Matrix matrix;
    private Index start, end;
    private TraversableMatrix traversableMatrix;
    /*
    to clear data members between clients (if same instance is shared among clients/tasks)
     */
    private void resetParams() {
        this.matrix = null;
        this.start = null;
        this.end = null;
    }

    @Override
    public void handle(InputStream fromClient, OutputStream toClient) throws IOException, ClassNotFoundException {

        // In order to read either objects or primitive types we can use ObjectInputStream
        ObjectInputStream objectInputStream = new ObjectInputStream(fromClient);
        // In order to write either objects or primitive types we can use ObjectOutputStream
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(toClient);
        this.resetParams(); // in order to use same handler between tasks/clients
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        ExecutorService threadPool = new ThreadPoolExecutor(3, 5, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        boolean doWork = true;
        while (doWork) {

            // client send a verbal command
            switch (objectInputStream.readObject().toString()) {
                case "matrix": {
                    // client will send a 2d array. handler will create a new Matrix object
                    int[][] primitiveMatrix = (int[][]) objectInputStream.readObject();
                    System.out.println("Server: Got 2d array from client");
                    this.matrix = new Matrix(primitiveMatrix);
                    this.matrix.printMatrix();
                    break;
                }
                case "mission-one": {
                    if (this.matrix != null) {
                        List<Future<HashSet<Index>>> futureList = new ArrayList<>();  // hold the futures
                        HashSet<HashSet<Index>> listOfScc = new HashSet<>();
                        ThreadLocalDfsVisit<Index> threadLocalSearch = new ThreadLocalDfsVisit<>();
                        int[][] primitiveMatrix = (int[][]) objectInputStream.readObject();
                        this.matrix = new Matrix(primitiveMatrix);
                        this.traversableMatrix = new TraversableMatrix(matrix);
                        int row = primitiveMatrix.length;
                        int column = primitiveMatrix[0].length;
                        LinkedList<Index> indicesOfOne = getIndicesOfOne(primitiveMatrix, row, column);
                        getListOfSccParallel(readWriteLock, threadPool, threadLocalSearch, futureList, listOfScc, indicesOfOne);
                        List<HashSet<Index>> sortedListOfScc = listOfScc.stream().sorted((set1, set2) -> Integer.compare(set2.size(), set1.size())).collect(Collectors.toList());
                        // send to socket's OutputStream
                        objectOutputStream.writeObject(sortedListOfScc);
                    }
                    break;
                }
                case "mission-two": {
                    if (this.matrix != null) {
                        int[][] primitiveMatrix = (int[][]) objectInputStream.readObject();
                        this.matrix = new Matrix(primitiveMatrix);
                        this.traversableMatrix = new TraversableMatrix(matrix);
                        traversableMatrix.startIndex = (Index) objectInputStream.readObject();
                        traversableMatrix.endIndex = (Index) objectInputStream.readObject();
                        ThreadLocalBfsVisit<Index> threadLocalSearch = new ThreadLocalBfsVisit<>();
                        List<ArrayList<Node<Index>>> listOfShortestPath = threadLocalSearch.getShortestPaths(traversableMatrix);
                        objectOutputStream.writeObject(listOfShortestPath);
                    }
                    break;
                }
                case "mission-three": {
                    if (this.matrix != null) {
                        int[][] primitiveMatrix = (int[][]) objectInputStream.readObject();
                        ThreadLocalDfsVisit<Index> threadLocalSearch = new ThreadLocalDfsVisit<>();
                        List<Future<HashSet<Index>>> futureList = new ArrayList<>();
                        List<Future<Integer>> resFutures = new ArrayList<>();
                        HashSet<HashSet<Index>> listOfScc = new HashSet<>();
                        int row = primitiveMatrix.length;
                        int column = primitiveMatrix[0].length;
                        Integer counter = 0;

                        this.matrix = new Matrix(primitiveMatrix);
                        this.traversableMatrix = new TraversableMatrix(matrix);
                        LinkedList<Index> indicesOfOne = getIndicesOfOne(primitiveMatrix, row, column);

                        getListOfSccParallel(readWriteLock, threadPool, threadLocalSearch, futureList, listOfScc, indicesOfOne);

                        for (HashSet scc : listOfScc) {
                            Iterator it = scc.iterator();
                            Callable<Integer> callable = () -> isSubmarine(primitiveMatrix, it);

                            Future<Integer> isSubmarineRecite = threadPool.submit(callable);
                            resFutures.add(isSubmarineRecite);
                        }
                        for (Future i : resFutures) {
                            try {
                                counter += (Integer) i.get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                        objectOutputStream.writeObject(counter);
                    }
                    break;
                }

                case "mission-four": {
                    if (this.matrix != null) {
                        int[][] primitiveMatrix = (int[][]) objectInputStream.readObject();
                        this.matrix = new Matrix(primitiveMatrix);
                        this.traversableMatrix = new TraversableMatrix(matrix);
                        traversableMatrix.startIndex = (Index) objectInputStream.readObject();
                        traversableMatrix.endIndex = (Index) objectInputStream.readObject();
                        ThreadLocalBellmanFordVisit<Index> threadLocalSearch = new ThreadLocalBellmanFordVisit<>();
                        List<ArrayList<Node<Index>>> listOfSimplePath = threadLocalSearch.getSimplePaths(traversableMatrix);
                        objectOutputStream.writeObject(listOfSimplePath);
                    }
                    break;
                }

                case "start index": {
                    this.start = (Index) objectInputStream.readObject();
                    break;
                }

                case "end index": {
                    this.end = (Index) objectInputStream.readObject();
                    break;
                }

                case "stop": {
                    doWork = false;
                    threadPool.shutdown();
                    break;
                }
            }
        }
    }

    private void getListOfSccParallel(ReentrantReadWriteLock readWriteLock, ExecutorService threadPool, ThreadLocalDfsVisit<Index> threadLocalSearch, List<Future<HashSet<Index>>> futureList, HashSet<HashSet<Index>> listOfScc, LinkedList<Index> indicesOfOne) {
        for (int i = 0; i < indicesOfOne.size(); i++) {
            int j = i;
            Callable<HashSet<Index>> callable = () -> {
                readWriteLock.writeLock().lock();
                traversableMatrix.setStartIndex(indicesOfOne.get(j));
                HashSet<Index> singleSCC = threadLocalSearch.traverse(traversableMatrix);
                readWriteLock.writeLock().unlock();
                return singleSCC;
            };
            Future<HashSet<Index>> singleSccRecipe = threadPool.submit(callable);
            futureList.add(singleSccRecipe);
        }
        System.out.println(futureList.size());
        for (Future<HashSet<Index>> hashSetFuture : futureList) {
            HashSet<Index> single = null;
            try {
                single = hashSetFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            listOfScc.add(single);
        }
    }

    /**
     *
     * @param primitiveMatrix - client matrix
     * @param it - specific scc
     * @return 1 if the scc is submarine(meet condition), else 0
     */
    private int isSubmarine(int[][] primitiveMatrix, Iterator<Index> it) {
        int maxRow = 0, maxCol = 0, minRow = Integer.MAX_VALUE, minCol = Integer.MAX_VALUE;
        while (it.hasNext()) {
            Index i = it.next();
            maxRow = Math.max(maxRow, i.row);
            maxCol = Math.max(maxCol, i.column);
            minRow = Math.min(minRow, i.row);
            minCol = Math.min(minCol, i.column);
        }
        if (maxCol == minCol && maxRow == minRow) {
            return 0;
        } else {
            for (int i = minRow; i <= maxRow; i++) {
                for (int j = minCol; j <= maxCol; j++) {
                    if (primitiveMatrix[i][j] == 0) {
                        return 0;
                    }
                }
            }
        }
        return 1;
    }

    /**
     *
     * @param primitiveMatrix - client matrix
     * @param row
     * @param column
     * @return number of indices equal to 1
     */
    private LinkedList<Index> getIndicesOfOne(int[][] primitiveMatrix, int row, int column) {
        LinkedList<Index> indicesOfOne = new LinkedList<>();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                if (primitiveMatrix[i][j] == 1) indicesOfOne.add(new Index(i, j));
            }
        }
        return indicesOfOne;
    }
}