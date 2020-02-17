import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server
{
    private int port = 9191;

    private ArrayList<ClientHandler> allClients = new ArrayList<>();
    /**
     * Use a thread pool and limit the number of clients to e.g. 5
     */
    private ExecutorService treadPool = Executors.newFixedThreadPool(10);

    /**
     * Enable the system to log transaktions <<timestamp>> + <<request>> and <<timestamp>> + <<response>>
     * Requests not following the protocol should give an error response back to the client and of course log the event.
     */

    public void ClientListeners ()
    {
        try
        {
            ServerSocket listeners = new ServerSocket(port);
            /**
             * The server must accept clients (i.e. more than one) to join the chat system, using the protocol specified below.
             * When a client joins, the server should maintain and update a list of all active clients.
             * The server will need to save for each client the user name, IP address and Port number.
             *
             * The Client must send a “heartbeat alive” message once every minute to the Server. The
             * server should (maybe with a specialized thread) check the active list, and delete clients that
             * stop sending heartbeat messages. Maybe the active list should include last heartbeat time.
             */
            while (true)
            {
                System.out.println("[Server] Waiting for client to join");
                Socket client = listeners.accept();
                System.out.println("[Server] Connected to client");
                ClientHandler clientThread = new ClientHandler(client, allClients);
                allClients.add(clientThread);

                treadPool.execute(clientThread);
            }
        }
        catch (IOException e)
        {
            /**
             * https://stackoverflow.com/questions/12095378/difference-between-e-printstacktrace-and-system-out-printlne
             */
            e.printStackTrace(); //Uses System.err and should go to a log file
            System.err.println(e.getStackTrace());
        }



    }

}
