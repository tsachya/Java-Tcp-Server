import java.util.concurrent.*;

/**
 * This class represents an Entity that can be either executed or submitted to a ThreadPool
 * This is a generic type
 * Uses strategy pattern (as PriorityRunnable did)
 * There will be 2 constructors: one for Runnable tasks and another for Callable<V>
 * tasks
 * @param <V> the return type
 */
public class PriorityTask<V> implements RunnableFuture<V>, Comparable<PriorityTask<V>> {
    // This data member will wrap either Runnable or Callable<V>
    private RunnableFuture<V> target;
    private int priority;

    public PriorityTask(Callable<V> computation,int priority){
        this.priority = priority;
        this.target = new FutureTask<>(computation);
    }

    public PriorityTask(Runnable runnable,V result, int priority){
        this.priority = priority;
        this.target = new FutureTask<>(runnable,result);
    }

    @Override
    public int compareTo(PriorityTask<V> o) {
        return 0;
    }

    @Override
    public void run() {

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return target.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return target.isCancelled();
    }

    @Override
    public boolean isDone() {
        return target.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return target.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return target.get(timeout,unit);
    }
}