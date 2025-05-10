package spotifyServer.commandProcessor;

/**
 * AbstractHandler class is responsible for processing commands received from the client.
 * It handles the command execution and interacts with the appropriate services.
 * This class uses a chain of responsibility pattern to delegate the command processing to the appropriate handler.
 */

public abstract class AbstractHandler {
    protected AbstractHandler nextHandler;

    public void setNextHandler(AbstractHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public abstract String handleRequest(String command);

    protected String handleNext(String command) {
        if (nextHandler != null) {
            return nextHandler.handleRequest(command);
        }
        return null;
    }
}
