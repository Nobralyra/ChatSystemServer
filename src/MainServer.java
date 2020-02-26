/**
 * The main for the server
 */
public class MainServer
{
    public static void main(String[] args)
    {
        Server server = new Server();

        server.ClientListeners();
    }
}
