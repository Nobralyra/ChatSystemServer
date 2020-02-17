import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client
{
    private int serverPort = 9191;
    private String serverIP = "127.0.0.1";

    public void ClienConnect()
    {
        try
        {
            Socket socket = new Socket(serverIP, serverPort);

            ServerConnection serverConnection = new ServerConnection(socket);

            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            /**
             * Only one thread is needed in the client class because the client do not need to connect to multiple servers
             */
            new Thread(serverConnection).start();

            while(true)
            {
                String command = keyboard.readLine();

                /**
                 * JOIN <<user_name>>, <<server_ip>>:<<server_port>>
                 * DATA <<user_name>>: <<free text…>>
                 * IMAV
                 * QUIT
                 */
                /**
                 * Maybe a switch case instead
                 */
                /**
                 * The Client must send a Quit message when it is closing.
                 */
                if (command.equals("QUIT"))
                {
                    break;
                }
            }
            socket.close(); //Noget galt her
            //System.exit(0); //should maybe not be used
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
