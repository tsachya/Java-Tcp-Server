import java.util.*;

public class ThreadLocalDfsVisit<T> {
    protected final ThreadLocal<Stack<Node<T>>> stackThreadLocal = ThreadLocal.withInitial(Stack::new);
    protected final ThreadLocal<Queue<Node<T>>> queueThreadLocal = ThreadLocal.withInitial(LinkedList::new);
    protected final ThreadLocal<Set<Node<T>>> setThreadLocal = ThreadLocal.withInitial(HashSet::new);
    //ExecutorService service = new ThreadPoolExecutor(3, 4, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    protected void threadLocalPush(Node<T> node) {
        stackThreadLocal.get().push(node);
    }

    protected Node<T> threadLocalPop() {
        return stackThreadLocal.get().pop();
    }

    public HashSet<T> traverse(Traversable<T> partOfGraph) {
        System.out.println(Thread.currentThread().getName());
        setThreadLocal.remove();
        stackThreadLocal.remove();
        threadLocalPush(partOfGraph.getOrigin());
        while (!stackThreadLocal.get().isEmpty()) {
            Node<T> poppedNode = threadLocalPop();
            setThreadLocal.get().add(poppedNode);
            Collection<Node<T>> reachableNodes = partOfGraph.getReachableNodes(poppedNode);
            for (Node<T> singleReachableNode : reachableNodes) {
                if (!setThreadLocal.get().contains(singleReachableNode) && !stackThreadLocal.get().contains(singleReachableNode)) {
                    threadLocalPush(singleReachableNode);
                }
            }
        }
        HashSet<T> blackList = new HashSet<>();
        for (Node<T> node : setThreadLocal.get()) {
            blackList.add(node.getData());
        }
        return blackList;
    }


}