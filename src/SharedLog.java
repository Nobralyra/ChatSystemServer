import java.util.logging.Logger;

/**
 * Sharedlog uses the Singleton pattern because it is needed over and over again through the code.
 * Provide a single instance available for those classes that needs it.
 * Not risking that 2 or more threads is trying to log at the same time and conflicting each other.
 * https://refactoring.guru/design-patterns/singleton
 *
 */
public class SharedLog
{
    /**
     * Static field that make a definition from the logger package
     */
    private static Logger logger = Logger.getAnonymousLogger();

    /**
     * Creates a private constructor Sharedlog
     */
    private SharedLog ()
    {}

    /**
     * Static method that controls the access to the SharedLog instance
     * @return
     */
    public static Logger getInstance ()
    {
        return logger;
    }
}
