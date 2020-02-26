import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerClientHandler implements Runnable
{
    /**
     * Field:
     * Each client needs there own socket to communicate with the server
     */
    private Socket client;
    /**
     * Field:
     * BufferedReader is synchronized (thread safe) https://medium.com/@codespeaks/bufferedreader-vs-console-vs-scanner-in-java-74273bb280a7
     */
    private BufferedReader input;
    private PrintWriter output;
    private ArrayList<ServerClientHandler> allClients;
    /**
     * Field:
     * The username of the clients
     */
    public String User;

    /**
     * Field:
     * The heartbeat alive with time
     */
    public LocalTime IMAV = LocalTime.now();

    /**
     * Calls on the static method in class SharedLog
     */
    Logger logger = SharedLog.getInstance();


    /**
     * Overloadet constructor that Server uses to add the client in the ArrayList and allocates a thread
     * @param clientSocket
     * @param allClients
     * @param user
     */
    public ServerClientHandler(Socket clientSocket, ArrayList<ServerClientHandler> allClients, String user )
    {
        client = clientSocket;
        this.allClients = allClients;
        this.User = user;
        try
        {
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new PrintWriter(client.getOutputStream(), true);
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, e.getStackTrace().toString());
        }
        /**
         * The client itself and all other clients gets the List of active clients each time it someone new join the server
         */
        String result = "LIST" + getUserList() + " " + user;

        outToAll(result);
        output.println(result);
        logger.log(Level.INFO,client.getRemoteSocketAddress().toString() + " " +  result);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run()
    {
        try
        {
            /**
             * The while loop should run forever until something change the run to false
             */
            boolean run = true;
            while(run)
            {
                String request;
                try
                {
                    /**
                     * The command the client send
                     * Trigger log
                     */
                    request = input.readLine();
                    logger.log(Level.INFO,client.getRemoteSocketAddress().toString() + " " +  request);
                }
                catch (Exception e)
                {
                    /**
                     * If the client disconnects all clients gets notified of which user left and updates the list
                     * The while loop stops
                     */
                    outToAll(User + " has left the chat room");
                    User = "";
                    outToAll("LIST" + getUserList());
                    logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + "Client disconnected");

                    run = false;

                    /**
                     * Break the loop and jumps to the next iteration
                     */
                    continue;
                }

                String result;

                /**
                 * Switch on the 4 first charaters
                 */
                switch (request.substring(0,4))
                {
                    case "DATA":
                        /**
                         * Message from client that is displayed out to all clients inclusive the client itself
                         */

                        /**
                         * If message do not meet the requirements the client gets an errormessage, log the event, and jump to the next iteration of the loop
                         */
                        if (!IsDataValid(request))
                        {
                            String response = "J_ER 3: Bad Syntax DATA <<user_name>>: <<free text…>> Max 250 user characters";
                            output.println(response);
                            logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + response);
                            continue;
                        }

                        /**
                         * Print out only the name and message and trigger the log
                         */
                        result = request.substring(5);
                        outToAll(result);
                        logger.log(Level.INFO,client.getRemoteSocketAddress().toString() + " " +  result);
                        break;

                    case "QUIT":
                        /**
                         * If the client sends a QUIT all clients gets notified of which user left and updates the list
                         * The while loop stops
                         */

                        String response = User + " has left the chat room";
                        outToAll(response);
                        logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + response);


                        User = "";

                        response = "LIST" + getUserList();
                        outToAll(response);
                        logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + response);

                        run = false;
                        break;

                    case "IMAV":
                        IMAV = LocalTime.now();
                        break;
                    case "LIST":
                        /**
                         * If the client sends a LIST the client gets the newest list of active users and break the while loop
                         */
                        result = "LIST" + getUserList();
                        output.println(result);
                        logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + result);
                        break;

                    default:
                        /**
                         * If the request from the client do now match any of the cases they get an error message
                         */
                        response = "J_ER 2: Unknown Command - No such command exists!";
                        output.println(response);
                        logger.log(Level.INFO, client.getRemoteSocketAddress().toString() + " " + response);
                        break;
                }
            }
        }
        catch (Exception e)
        {
            /**
             * Log the event if the try failed
             */
            logger.log(Level.SEVERE, e.getStackTrace().toString());
        }
        /**
         * Always happens
         * Closes the input and output
         */
        finally
        {
            output.close();
            try
            {
                input.close();
            }
            catch (IOException e)
            {
                logger.log(Level.SEVERE, e.getStackTrace().toString());
            }
        }
    }

    /**
     * Sends the message out to all active clients in the ArrayList
     * @param message
     */
    private void outToAll(String message)
    {
        for (ServerClientHandler oneClient: allClients)
        {
            oneClient.output.println(message);

        }
    }

    /**
     * Finds the users in the ArrayList
     * @return
     */
    private String getUserList()
    {
        String users = "";
        for (ServerClientHandler oneClient: allClients)
        {
           users = users + " " + oneClient.User;
        }
        return users;
    }

    /**
     * Method that validates if the message the user has sent is meeting the requirements.
     * In that moment the message is greater than 250 it fails the check
     * @param request
     * @return
     */
    public boolean IsDataValid(String request)
    {
        /**
         * Only want to validate what is after the ": "
         */
        String resultOfValidate = request.substring(request.indexOf(":") + 2);
        if(resultOfValidate.length() > 250)
        {
            return false;
        }
        return true;
    }
}
