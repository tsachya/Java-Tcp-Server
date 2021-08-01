import java.util.*;

public class ThreadLocalBfsVisit<T> {
    protected final ThreadLocal<Stack<Node<T>>> stackThreadLocal = ThreadLocal.withInitial(Stack::new);
    protected final ThreadLocal<Queue<Node<T>>> queueThreadLocal = ThreadLocal.withInitial(LinkedList::new);
    protected final ThreadLocal<Set<Node<T>>> setThreadLocal = ThreadLocal.withInitial(HashSet::new);


    protected void threadLocalPush(Node<T> node) {
        stackThreadLocal.get().push(node);
    }

    protected Node<T> threadLocalPop() {
        return stackThreadLocal.get().pop();
    }

    /**
     * recursive function that recover all the paths using parents map and update the paths
     * @param partOfGraph
     * @param paths - all the relevant paths, list of path
     * @param path
     * @param parents - map that hold for every node list of his parents
     * @param dest - pointer to node in graph
     */
    public void find_paths(Traversable<T> partOfGraph, List<ArrayList<Node<T>>> paths, ArrayList<Node<T>> path, Map<Node<T>, ArrayList<Node<T>>> parents, Node<T> dest) {
        if (dest.equals(partOfGraph.getOrigin())) {
            path.add(dest);
            paths.add((ArrayList<Node<T>>) path.clone());
            path.remove(path.get(path.size() - 1));
            return;
        }
        for (Node<T> parent : parents.get(dest)) {
            path.add(dest);
            find_paths(partOfGraph, paths, path, parents, parent);
            path.remove(path.get(path.size() - 1));
        }
    }

    /**
     * this function implements bfs algorithm, using of distances and parents maps
     * @param partOfGraph
     * @param parents - map the contains for every node list of parents, it will help to recover the paths
     */
    public void bfs(Traversable<T> partOfGraph, Map<Node<T>, ArrayList<Node<T>>> parents) {
        Map<Node<T>, Integer> distances = new Hashtable<>();
        Queue<Node<T>> q = new LinkedList<>();
        q.add(partOfGraph.getOrigin());
        distances.put(partOfGraph.getOrigin(), 0);
        Node<T> poppedNode;

        while (!q.isEmpty()) {
            poppedNode = q.peek();
            q.remove();

            Collection<Node<T>> reachableNodes = partOfGraph.getReachableNodes(poppedNode);
            for (Node<T> singleReachableNode : reachableNodes) {
                if (!distances.containsKey(singleReachableNode)) {
                    distances.put(singleReachableNode, Integer.MAX_VALUE); //max value for infinity
                }
                if (!parents.containsKey(singleReachableNode)) {
                    parents.put(singleReachableNode, new ArrayList<>());

                }
                if (distances.get(singleReachableNode) > distances.get(poppedNode) + 1) {
                    distances.put(singleReachableNode, distances.get(poppedNode) + 1);
                    q.add(singleReachableNode);
                    parents.get(singleReachableNode).clear();
                    parents.get(singleReachableNode).add(poppedNode);
                } else if (distances.get(singleReachableNode) == distances.get(poppedNode) + 1) {
                    parents.get(singleReachableNode).add(poppedNode);
                }
            }
        }
    }
    /**
     *
     * @param partOfGraph
     * @return shortest paths in graph
     */
    public List<ArrayList<Node<T>>> getShortestPaths(Traversable<T> partOfGraph) {
        List<ArrayList<Node<T>>> paths = new ArrayList<>();
        ArrayList<Node<T>> path = new ArrayList<>();
        Map<Node<T>, ArrayList<Node<T>>> parents = new Hashtable<>();
        bfs(partOfGraph, parents);
        if (!parents.containsKey(partOfGraph.getDestination())) return paths;

        find_paths(partOfGraph, paths, path, parents, partOfGraph.getDestination());
        return paths;
    }
}