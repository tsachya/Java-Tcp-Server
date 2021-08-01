import java.util.Collection;

/**
 * This interface defines the functionality required for a traversable graph
 */
public interface Traversable<T> {
    public Node<T> getOrigin();
    public Node<T> getDestination();
    public int getValueTravers(Node<T> someNode);
    public Collection<Node<T>>  getReachableNodes(Node<T> someNode);
    public Collection<Node<T>> getReachableNodesWithoutCross(Node<T> someNode);

}
