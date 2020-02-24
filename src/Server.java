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
                output = new PrintWriter(client.getOutputStream(), true);
                System.out.println(request);

                if (request.startsWith("JOIN"))
                {
                    System.out.println(request); //Just so we can see what the server got a input

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
                         * What is happening here?
                         */
                    boolean existUserName = false;
                        for (ServerClientHandler oneClient: allClients)
                        {
                            String current = oneClient.User;
                            if(nextUserName.equals(current))
                            {
                                existUserName = true;
                            }
                        }
                        if (existUserName)
                        {
                            output.println("J_ER 1234: Duplicate Username");
                            client.close();
                            continue;
                        }
                        if(!isNextUserNameValid(nextUserName))
                        {
                            output.println("J_ER 1235: Username is max 12 chars long, only letters, digits, ‘-‘ and ‘_’ allowed");
                            client.close();
                            continue;
                        }

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

    public boolean isNextUserNameValid(String nextUserName)
    {
        if (nextUserName.length() <= 12 && nextUserName.matches("[a-zA-Z0-9_\\-]+"))
        {
            return true;
        }
        return false;
    }
}
