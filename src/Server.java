import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server
{
    /**
     * Field:
     * Port of the Server
     */
    private int port = 9191;

    /**
     * Field:
     * ArrayList with reference of class ServerClientHandler that have all the clients
     */
    private ArrayList<ServerClientHandler> allClients = new ArrayList<>();
    /**
     * Use a thread pool and limit the number of clients to e.g. 5
     *
     * If there is 2 threads in the threadpool only 2 clients can send commands to the server, but other clients can see the if they example sends a data messages.
     */
    private ExecutorService threadPool = Executors.newFixedThreadPool(5);

    /**
     * Calls on the static method in class SharedLog
     */
    private Logger logger = SharedLog.getInstance();

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
                /**
                 * Logs the message on level INFO
                 */
                logger.log(Level.INFO, "[Server] Waiting for client to join");

                /**
                 * The accept() method waits until a client starts up and requests a connection on the host and port of this server.
                 * When a connection is requested and successfully established the accept method returns a new Socket object to the client
                 * which is bound to the same local port and has its remote address and remote port set to that of the client.
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
                 * Logs the message request with the level INFO and adds the information about witch socket address the message was from
                 */
                logger.log(Level.INFO,client.getRemoteSocketAddress().toString() + " " + request);

                /**
                 * What is the incoming clients username
                 */
                String nextUserName = null;


                output = new PrintWriter(client.getOutputStream(), true);

                /**
                 * Nested if statement that runs if the request starts with JOIN
                 */
                if (request.startsWith("JOIN"))
                {
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
                     * If statement that validates if the username the user want to join with correct.
                     * If the username do not meet the requirements the client gets an errormessage, log the event, and the clients socket is closed
                     * It is "cheaper" to have this if statement before the for loop down below because it faster break the while loop
                     */
                    if(!isNextUserNameValid(nextUserName))
                    {
                        String response = "J_ER 1: Username is max 12 chars long, only letters, digits, ‘-‘ and ‘_’ allowed";
                        output.println(response);
                        logger.log(Level.INFO,client.getRemoteSocketAddress().toString() + " " +  response);
                        client.close();
                        logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " was closed");
                        continue;
                    }

                    /**
                     * For loop that checks if the username is avaliable
                     */
                    boolean existUserName = false;
                    for (ServerClientHandler oneClient: allClients)
                    {
                        String current = oneClient.User;
                        if(nextUserName.equals(current))
                        {
                            existUserName = true;
                            break;
                        }
                    }

                    /**
                     * If the username was already in use the client gets an errormessage, log the event, and the clients socket is closed
                     */
                    if (existUserName)
                    {
                        String response = "J_ER 2: Duplicate Username";
                        output.println(response);
                        logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + response);

                        client.close();
                        logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " was closed");
                        continue;
                    }

                    /**
                     * Client gets the message if they joined the server and log the event
                     */
                    String response = "J_OK";
                    output.println(response);
                    logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + response);

                }
                /**
                 * Instance of the class ServerClientHandler that get the parameter client (socket the client and server is communicate with),
                 * allClients (ArrayList), nextUserName (the username from the client)
                 */
                ServerClientHandler clientThread = new ServerClientHandler(client, allClients, nextUserName);

                /**
                 * Adds the client to the the ArrayList
                 */
                allClients.add(clientThread);

                /**
                 * Gives client a thread an execute the code
                 */
                threadPool.execute(clientThread);
            }
        }
        catch (IOException e)
        {
            /**
             * Log the event if the try failed
             */
            logger.log(Level.SEVERE, e.getStackTrace().toString());
        }
    }

    /**
     * Method that validates if the username the user want to join is meeting the requirements.
     * @param nextUserName
     * @return
     */
    public boolean isNextUserNameValid(String nextUserName)
    {
        if (nextUserName.length() <= 12 && nextUserName.matches("[a-zA-Z0-9_\\-]+"))
        {
            return true;
        }
        return false;
    }
}
