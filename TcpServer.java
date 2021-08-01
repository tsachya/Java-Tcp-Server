import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TcpServer {

    private final int port;
    private volatile boolean stopServer;
    private ThreadPoolExecutor threadPool;
    private IHandler requestHandler;
    private final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();

    public TcpServer(int port) {
        this.port = port;
        stopServer = false;
        threadPool = null;
        requestHandler = null;
    }

    public void run(IHandler concreteHandler) {
        this.requestHandler = concreteHandler;

        new Thread(() -> {
            // lazy loading
            threadPool = new ThreadPoolExecutor(3, 5, 10,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                while (!stopServer) {
                    Socket serverToSpecificClient = serverSocket.accept(); // 2 operations: listen()+accept()
                /*
                 server will handle each client in a separate thread
                 define every client as a Runnable task to execute
                 */
                    Runnable clientHandling = () -> {
                        try {
                            requestHandler.handle(serverToSpecificClient.getInputStream(),
                                serverToSpecificClient.getOutputStream());
                            // finished handling client. now close all streams
                            serverToSpecificClient.getInputStream().close();
                            serverToSpecificClient.getOutputStream().close();
                            serverToSpecificClient.close();
                        } catch (IOException ioException) {
                            System.err.println(ioException.getMessage());
                        } catch (ClassNotFoundException ce) {
                            System.err.println(ce.getMessage());
                        }
                    };
                    threadPool.execute(clientHandling);
                }
                serverSocket.close();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }).start();
    }
    public void stop(){
        if(!stopServer)
            stopServer = true;
        if(threadPool!=null){
                threadPool.shutdown();
        }
    }

    public static void main(String[] args) {
        TcpServer myServer = new TcpServer(8010);
        myServer.run(new MatrixIHandler());
    }
}
