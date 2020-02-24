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

            while(true)
            {
                keyboard = new BufferedReader(new InputStreamReader(System.in));
                String x = keyboard.readLine();
                String connectionData = x.substring(x.indexOf(", ", 2) + 2);
                try
                {
                    String serverIP = connectionData.substring(0,connectionData.indexOf(":"));
                    String serverPort = connectionData.substring(connectionData.indexOf(":") + 1);
                    socket = new Socket(serverIP, Integer.parseInt(serverPort));
                }
                catch (Exception e)
                {
                    System.err.println("Failed to connect! Try again");
                    continue;
                }

                output = new PrintWriter(socket.getOutputStream(), true);
                output.println(x);
                break;
            }

            clientServerHandler = new ClientServerHandler(socket);
            /**
             * Only one thread is needed in the client class because the client do not need to connect to multiple servers
             */
            new Thread(clientServerHandler).start();

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask()
            {
                @Override
                public void run()
                {
                   output.println("IMAV");
                }
            }, 60*1000, 60*1000);

            while(true)
            {
                String command = keyboard.readLine();

                /**
                 * The Client must send a Quit message when it is closing.
                 */
                switch (command.substring(0,4))
                {
                    case "QUIT":
                        output.println("QUIT");
                        System.exit(1);
                        break;

                    case "JOIN":
                        break;
                    case "DATA":
                    case "LIST":
                        output.println(command);
                    break;
                    default:
                        System.err.println("Command Error - No such command exists!");
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
