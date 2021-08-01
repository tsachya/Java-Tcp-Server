import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket =new Socket("127.0.0.1",8010);
        System.out.println("client: Created Socket");

        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        ObjectOutputStream toServer=new ObjectOutputStream(outputStream);
        ObjectInputStream fromServer=new ObjectInputStream(inputStream);

        // sending #1 matrix
        int[][] source = {
            {1, 1, 0, 1, 1},
            {1, 1, 0, 1, 1},
            {1, 1, 0, 0, 0},
            {1, 1, 0, 1, 1},
            {1, 1, 0, 1, 1}
        };

        int[][] source2 = {
            {100, 100, 100},
            {400, 300, 300},
            {100, 100, 100}
        };

        //send "matrix" command then write 2d array to socket
        toServer.writeObject("matrix");
        toServer.writeObject(source);

        toServer.writeObject("mission-one");
        toServer.writeObject(source);

        List<Set<Index>> scc =
            new ArrayList((List<Index>) fromServer.readObject());
        System.out.println("from client - all SCC Indices are:  "+ scc);


        toServer.writeObject("mission-two");
        toServer.writeObject(source);
        toServer.writeObject(new Index(0,0));
        toServer.writeObject(new Index(3,1));

        List<ArrayList<Node>> shortestPaths =
            new ArrayList((ArrayList<Node>) fromServer.readObject());
        System.out.println("from client - all shortestPath Indices are:  "+ shortestPaths);

        toServer.writeObject("mission-three");
        toServer.writeObject(source);

        int numberOfSubmarine = (int) fromServer.readObject();
        System.out.println("from client - number of submarine are:  "+ numberOfSubmarine);


        toServer.writeObject("mission-four");
        toServer.writeObject(source2);
        toServer.writeObject(new Index(1,0));
        toServer.writeObject(new Index(1,2));

        List<ArrayList<Node>> easyPaths =
            new ArrayList((ArrayList<Node>) fromServer.readObject());
        System.out.println("from client - all easyPaths Indices are:  "+ easyPaths);


        toServer.writeObject("stop");

        System.out.println("client: Close all streams");
        fromServer.close();
        toServer.close();
        socket.close();
        System.out.println("client: Closed operational socket");
    }
}