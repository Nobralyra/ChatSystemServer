import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;

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
     * Overloadet constructor
     * @param clientSocket
     * @param allClients
     * @param user
     */
    public ServerClientHandler(Socket clientSocket, ArrayList<ServerClientHandler> allClients, String user )
    {
        client = clientSocket;
        this.allClients = allClients; //The name should maybe change to something else
        this.User = user;
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
        String result = "LIST" + getUserList() + " " + user;

        outToAll(result);
        output.println(result);

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
             * An active client can send user text message to the server that will just send a copy to all active clients in the client list.
             * Protocol DATA <<user_name>>: <<free text…>> should be used
             */
            boolean run = true;
            while(run)
            {
                String request;
                try {
                request = input.readLine();
                }
                catch (Exception e)
                {
                    outToAll(User + " has left the chat room");
                    User = "";
                    outToAll("LIST" + getUserList());
                    run = false;
                    continue;
                }
                String result = "";

                /**
                 * Message from client, that is displayed out to all clients inclusive the client itself
                 */
                switch (request.substring(0,4))
                {
                    case "DATA":
                        String validate = "DATA " + User + ": ";
                        if (!request.startsWith(validate))
                        {
                            output.println("J_ER Bad Syntax DATA <<user_name>>: <<free text…>>");
                            continue;
                        }

                        result = request.substring(5);
                        outToAll(result);
                        break;

                    case "QUIT":
                        outToAll(User + " has left the chat room");
                        User = "";

                        outToAll("LIST" + getUserList());
                        run = false;
                        break;

                    case "IMAV":
                        IMAV = LocalTime.now();
                        break;
                    case "LIST":
                        result = "LIST" + getUserList();
                        output.println(result);
                        break;
                    default:
                        System.err.println("Command Error - No such command exists!");
                        break;
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

    /**
     *
     * @param message
     */
    private void outToAll(String message)
    {
        for (ServerClientHandler oneClient: allClients)
        {
            oneClient.output.println(message);

        }
    }

    private String getUserList()
    {
        String users = "";
        for (ServerClientHandler oneClient: allClients)
        {
           users = users + " " + oneClient.User;
        }
        return users;
    }
}
