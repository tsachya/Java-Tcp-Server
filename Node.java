
import java.io.Serializable;
import java.util.Objects;

public class Node<T> implements Serializable {
    private static final long serialVersionUID = 1L;


    private T data;
    private Node<T> parent;

    public Node() {
        this(null);
    }

    public Node(final T data) {
        this(data,null);
    }

    public Node(final T data, final Node<T> parent) {
        this.data = data;
        this.parent = parent;
    }


    public T getData() {
        return data;
    }


    public Node<T> setData(T data) {
        this.data = data;
        return this;
    }

    public Node<T> getParent() {
        return parent;
    }


    public Node<T> setParent( final Node<T> parent) {
        this.parent = parent;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        Node<?> state1 = (Node<?>) o;

        return Objects.equals(data, state1.data);
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override
    public String toString() {
        return data.toString();
    }


}