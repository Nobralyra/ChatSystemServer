import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable
{
    private Socket client;
    /**
     * BufferedReader is synchronized (thread safe) https://medium.com/@codespeaks/bufferedreader-vs-console-vs-scanner-in-java-74273bb280a7
     */
    private BufferedReader input;
    private PrintWriter output;
    private ArrayList<ClientHandler> allClients;

    public ClientHandler(Socket clientSocket, ArrayList<ClientHandler> allClients)
    {
        client = clientSocket;
        this.allClients = allClients; //The name should maybe change to something else
        try
        {
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new PrintWriter(client.getOutputStream(), true);
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
             * You must use threads in client and/or in server. The client should at the start ask the user
             * his/her chat-name and then send a join message to the server.
             * So this while is in the wrong class!
             */
            /**
             * If a new user tries to join with the same name as an already active user, then an error message should be sent back to client.
             * Client can try again with a new name.
             * Protocol J_ER should be used
             *
             * An active client can send user text message to the server that will just send a copy to all active clients in the client list.
             * Protocol DATA <<user_name>>: <<free text…>> should be used
             */
            while(true)
            {
                String request = input.readLine();
                /**
                 * Message from client, that is displayed out to all clients inclusive the client itself
                 */
                int firstSpace = request.indexOf(" ");

                /**
                 * This should not be and if else statement, but something else!
                 */
                if (request.startsWith("Name"))
                {
                    System.out.println("Name: " + request);
                }
                else if (request.startsWith("Message"))
                {
                    if (firstSpace != -1) //If firstSpace exist there is a message to display
                    {
                        outToAll(request.substring(firstSpace + 1));
                    }
                }
                else
                {
                    System.out.println("Duplicate name"); //not right!!!
                }
            }
        }
        catch (Exception e)
        {
            /**
             * https://stackoverflow.com/questions/12095378/difference-between-e-printstacktrace-and-system-out-printlne
             */
            e.printStackTrace(); //Uses System.err and should go to a log file
            System.err.println(e.getStackTrace());
        }
        finally
        {
            output.close();
            try
            {
                input.close();
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

    private void outToAll(String message)
    {
        for (ClientHandler oneClient: allClients)
        {
            /**
             *  oneClient.out.println(message); is how the video wrote it, but does  System.out.println(message); do the same
             *  because I get the error at out.
             */
            //oneClient.out.println(message);
            System.out.println(message);

        }
    }
}
