import java.util.*;
import java.util.concurrent.Callable;

public class ThreadLocalBellmanFordVisit<T> {
    protected final ThreadLocal<Stack<Node<T>>> stackThreadLocal = ThreadLocal.withInitial(Stack::new);
    protected final ThreadLocal<Queue<Node<T>>> queueThreadLocal = ThreadLocal.withInitial(LinkedList::new);

    /**
     * recursive function that recover all the paths using parents map and update the paths
     * @param partOfGraph
     * @param paths - all the relevant paths, list of path
     * @param path
     * @param parents - map that hold for every node list of his parents
     * @param dest - pointer to node in graph
     */
    public synchronized void find_paths(Traversable<T> partOfGraph, List<ArrayList<Node<T>>> paths, ArrayList<Node<T>> path, Map<Node<T>, ArrayList<Node<T>>> parents, Node<T> dest) {
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
     * function that calculate the minimum distances between the origin to every node
     * @param partOfGraph
     * @param parents - map the show the parent of every node
     */
    public void BellmanFord(Traversable<T> partOfGraph, Map<Node<T>, ArrayList<Node<T>>> parents) {

        Map<Node<T>, Integer> distances = new Hashtable<>();
        queueThreadLocal.get().add(partOfGraph.getOrigin());
        Collection<Node<T>> reachableNodes;
        distances.put(partOfGraph.getOrigin(), partOfGraph.getValueTravers(partOfGraph.getOrigin()));
        parents.put(partOfGraph.getOrigin(), new ArrayList<>());
        Node<T> poppedNode;

        while (!queueThreadLocal.get().isEmpty()) {
            poppedNode = queueThreadLocal.get().peek();
            queueThreadLocal.get().remove();

            reachableNodes = partOfGraph.getReachableNodesWithoutCross(poppedNode);
            for (Node<T> singleReachableNode : reachableNodes) {

                if (!distances.containsKey(singleReachableNode)) {
                    distances.put(singleReachableNode, distances.get(poppedNode) + partOfGraph.getValueTravers(singleReachableNode)); //max value for infinity
                    queueThreadLocal.get().add(singleReachableNode);
                }
                if (!parents.containsKey(singleReachableNode)) {
                    parents.put(singleReachableNode, new ArrayList<>());

                }
                if (distances.get(singleReachableNode) > distances.get(poppedNode) + partOfGraph.getValueTravers(singleReachableNode)) {
                    distances.put(singleReachableNode, distances.get(poppedNode) + partOfGraph.getValueTravers(singleReachableNode));
                    parents.get(singleReachableNode).clear();
                    parents.get(singleReachableNode).add(poppedNode);
                } else if (distances.get(singleReachableNode) == distances.get(poppedNode) + partOfGraph.getValueTravers(singleReachableNode)) {
                    {
                        if (!parents.get(singleReachableNode).contains(poppedNode) && !parents.get(poppedNode).contains(singleReachableNode))
                            parents.get(singleReachableNode).add(poppedNode);
                    }
                }
            }
        }
    }
    /**
     *
     * @param partOfGraph
     * @return the simple paths in graph
     */
    public List<ArrayList<Node<T>>> getSimplePaths(Traversable<T> partOfGraph) {
        List<ArrayList<Node<T>>> paths = new ArrayList<>();
        ArrayList<Node<T>> path = new ArrayList<>();
        Map<Node<T>, ArrayList<Node<T>>> parents = new Hashtable<>();
        BellmanFord(partOfGraph, parents);
        find_paths(partOfGraph, paths, path, parents, partOfGraph.getDestination());
        return paths;
    }
}