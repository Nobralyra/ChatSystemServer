import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Client
{
    public void ClientConnect()
    {
        try
        {
            /**
             * Local variable:
             * Clients socket to communicate with the server
             */
            Socket socket;

            ClientServerHandler clientServerHandler;

            BufferedReader keyboard;
            PrintWriter output;

            /**
             * Connection to the server
             */
            while(true)
            {
                /**
                 * Reads what the user has written on the keyboard
                 */
                keyboard = new BufferedReader(new InputStreamReader(System.in));
                String x = keyboard.readLine();
                /**
                 * Only want was is after the ", " like JOIN a, 127.0.0.1:9191 to 127.0.0.1:9191
                 */
                String connectionData = x.substring(x.indexOf(", ", 2) + 2);
                try
                {
                    /**
                     * Now we only want the first bit of the connectionDato that is 127.0.0.1
                     */
                    String serverIP = connectionData.substring(0,connectionData.indexOf(":"));
                    /**
                     * Now we want the last bit of the connectionData that is 9191.
                     * Because the ":" is still there we need say it stands on index 1
                     */
                    String serverPort = connectionData.substring(connectionData.indexOf(":") + 1);
                    /**
                     * Now the information can be put into socket
                     */
                    socket = new Socket(serverIP, Integer.parseInt(serverPort));
                }
                catch (Exception e)
                {
                    System.err.println("Failed to connect! Try again");
                    continue;
                }

                /**
                 * Sends the information to where the socket is pointing to
                 */
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println(x);
                break;
            }

            /**
             * The constructor from ClientServerHandler now gets the information about where the communication from the server and client is
             */
            clientServerHandler = new ClientServerHandler(socket);
            /**
             * Only one thread is needed in the client class because the client do not need to connect to multiple servers
             */
            new Thread(clientServerHandler).start();

            /**
             * Sends a heartbeat alive every 60 seconds
             */
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask()
            {
                @Override
                public void run()
                {
                   output.println("IMAV");
                }
            }, 60*1000, 60*1000);

            /**
             *  The while loop should run forever until something change it to false
             */
            while(true)
            {
                String command = keyboard.readLine();

                /**
                 * Switch on the 4 first charaters
                 */

                switch (command.substring(0,4))
                {
                    case "QUIT":
                        /**
                         * The Client must send a Quit message when it is closing.
                         * Clients system then exit
                         */
                        output.println("QUIT");
                        System.exit(1);
                        break;

                        /** Do not think this is needed
                    case "JOIN":
                        break;
                         */
                    case "DATA":
                    case "LIST":
                        /**
                         * Sends a command to the server asking to see the list of active users
                         */
                        output.println(command);
                    break;
                    default:
                        /**
                         * If the request from the client do now match any of the cases they get an error message
                         */
                        System.err.println("J_ER 4: Unknown Command - No such command exists!");
                        break;
                }
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
