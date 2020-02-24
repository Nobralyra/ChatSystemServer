import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientServerHandler implements Runnable
{
    private Socket server;

    /**
     * BufferedReader is synchronized (thread safe) https://medium.com/@codespeaks/bufferedreader-vs-console-vs-scanner-in-java-74273bb280a7
     */
    private BufferedReader input;

    public ClientServerHandler(Socket serverSocket)
    {
        server = serverSocket;

        try
        {
            input = new BufferedReader(new InputStreamReader(server.getInputStream()));
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
    /**
     * Public run method perpose is to handle inputs from the server, so it is no longer needed in the Client class!
     */
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                String serverResponse = input.readLine();

                if (serverResponse == null)
                {
                    break;
                }
                System.out.println(serverResponse);
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
        finally
        {
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
}
