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

    /**
     * Public method ClientListerners
     * When a JOIN request from a client comes, the method adds the client to an ArrayList and takes a thread from the
     * thread pool and pairs it with the client.
     */
    public void ClientListeners ()
    {
        try
        {
            /**
             * Instance of ServerSocket that is imported by a packet.
             * It waits for a request to come in over the network that targets the port and put the request in listerners
             */
            ServerSocket listeners = new ServerSocket(port);

            /**
             * Local variable:
             * ArrayList that have the usernames of the clients who succesfully join the chat
             */
            ArrayList<String> users = new ArrayList<String>();

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


            /**
             * The while loop should run as long as the server is running (true)
             * The loop wait for a client to join
             */
            while (true)
            {
                System.out.println("[Server] Waiting for client to join");

                /**
                 * The accept() method waits until a client starts up and requests a connection
                 * on the host and port of this server.
                 * When a connection is requested and successfully established,
                 * the accept method returns a new Socket object (here client?) which is bound to the same
                 * local port and has its remote address and remote port set to that of the client.
                 * https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                 */
                Socket client = listeners.accept();

                /**
                 * BufferedReader buffering the input from the client (the JOIN request)
                 */
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                /**
                 * PrintWriter writes the output the Client gets
                 */
                PrintWriter output;

                /**
                 * Reads text lines from BufferedRead where it has been stored
                 */
                String request = input.readLine();

                /**
                 * What is the incoming clients username
                 */
                String nextUserName = null;

                /**
                 * Nested if statement that runs if the request starts with JOIN
                 */
                if (request.startsWith("JOIN"))
                {
                    System.out.println(request); //Just so we can see what the server got a input
                    output = new PrintWriter(client.getOutputStream(), true);
                    /**
                     * Because the request has more than the username, we need to split the request into an Array
                     * After the first space at index 1 (where the username is)
                     */
                    nextUserName = request.split(" ") [1];
                    /**
                     * The nextUserName still have the comma, that needs to be removed.
                     * We are at index 0 and the length should be one smaller
                     */
                    nextUserName = nextUserName.substring(0, nextUserName.length() -1);

                    /**
                     * If statement that returns true if ArrayList with joined users already
                     * has the username the next client tries to join with
                     * It sends a error messages to the client and closes the socket
                     * The continue breaks the if statement (if the condition was true)
                     * and continues into the other if statement
                     */
                    if(users.contains(nextUserName))
                    {
                        output.println("J_ER 1234: Duplicate Username");
                        client.close();
                        continue;
                    }
                    /**
                     * adds the non-dubblicate username into the ArrayList of users
                     */
                    users.add(nextUserName);
                    /**
                     * Client gets the message if they joined the server
                     */
                    output.println("J_OK");

                    System.out.println("[Server] Connected to client"); //Message to us if a client is connected
                }
                /**
                 * Instance of the class ServerClientHandler that get the parameter client (socket the client and server is communicate with),
                 * allClients (ArrayList), nextUserName (the username from the client)
                 */
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
