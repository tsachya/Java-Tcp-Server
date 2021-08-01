import java.util.*;

public class BellmanFord<T> {

    // Utility function to check if current
// vertex is already present in path
    public boolean isNotVisited(Node<T> x, List<Node<T>> path) {
        //int size = path.size();
        for (Node<T> v : path)
            if (path.contains(x))
                return false;

        return true;
    }

    // Utility function for finding paths in graph
    // from source to destination
    public List<List<Node<T>>> findpathsBellmanFord(Traversable<T> someGraph, Node<T> src, Node<T> dst) {
        int sum = 0, min = Integer.MAX_VALUE;
        List<List<Node<T>>> listMinWeight = new ArrayList<>();
        List<List<Node<T>>> listMinTotalWeight = new ArrayList<>();
        Queue<List<Node<T>>> queue = new LinkedList<>();

        // Path vector to store the current path
        List<Node<T>> path = new ArrayList<>();
        path.add(src);
        queue.offer(path);
        // listMinWeight.add(path);
        while (!queue.isEmpty()) {

            path = queue.poll();
            Node<T> last = path.get(path.size() - 1);
            // If last vertex is the desired destination
            // then print the path
            if (last.equals(dst)) {
                for (Node<T> node : path) {
                    sum = sum + someGraph.getValueTravers(node);
                }
                if(sum<=min) {
                    min = sum;
                    listMinWeight.add(path);
                }
            }
            sum = 0;

            // Traverse to all the nodes connected to
            // current vertex and push new path to queue
            Collection<Node<T>> lastNode = someGraph.getReachableNodesWithoutCross(last);
            for (Node<T> single : lastNode) {
                if (isNotVisited(single, path)) {
                    List<Node<T>> newpath = new ArrayList<>(path);
                    newpath.add(single);
                    queue.offer(newpath);
                }
            }
        }
        for (List<Node<T>> listNode : listMinWeight) {
            for (Node<T> nodeT : listNode){
                sum = sum + someGraph.getValueTravers(nodeT);
            }
            if (sum==min)
            {
                listMinTotalWeight.add(listNode);
            }
            sum=0;
        }
        return listMinTotalWeight;
    }
}