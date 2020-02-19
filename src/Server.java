import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server
{
    /**
     * Field:
     * Port of the Server
     */
    private int port = 9191;

    /**
     * Field:
     * ArrayList with generisc type of the class ServerClientHandler that have all the clients
     */
    private ArrayList<ServerClientHandler> allClients = new ArrayList<>();
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
            /**
             * Instance of ServerSocket that is imported by a packet.
             * It waits for request to come in over the network that targets the port and put the request in listerners
             */
            ServerSocket listeners = new ServerSocket(port);
            /**
             * The server must accept clients (i.e. more than one) to join the chat system, using the protocol specified below.
             * When a client joins, the server should maintain and update a list of all active clients.
             * The server will need to save for each client the user name, IP address and Port number.
             *
             * If a new user tries to join with the same name as an already active user, then an error message should be sent back to client.
             * Client can try again with a new name.
             * Protocol J_ER should be used
             *
             * The Client must send a “heartbeat alive” message once every minute to the Server. The
             * server should (maybe with a specialized thread) check the active list, and delete clients that
             * stop sending heartbeat messages. Maybe the active list should include last heartbeat time.
             */
            ArrayList<String> users = new ArrayList<String>();
            while (true)
            {
                System.out.println("[Server] Waiting for client to join");
                Socket client = listeners.accept();
                System.out.println("[Server] Connected to client");
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter output = new PrintWriter(client.getOutputStream(), true); //do not know why the right side is grayed out
                String request = input.readLine();

                String nextUserName = null;

                if (request.startsWith("JOIN"))
                {
                    System.out.println(request);
                    output = new PrintWriter(client.getOutputStream(), true);
                    nextUserName = request.split(" ") [1];
                    nextUserName = nextUserName.substring(0, nextUserName.length() -1);

                    if(users.contains(nextUserName))
                    {
                        output.println("J_ER 1234: Duplicate Username");
                        client.close();
                        continue;
                    }
                    users.add(nextUserName);
                    output.println("J_OK");
                }

                ServerClientHandler clientThread = new ServerClientHandler(client, allClients, nextUserName);

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
