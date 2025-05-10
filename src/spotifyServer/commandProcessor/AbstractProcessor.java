package spotifyServer.commandProcessor;

import java.net.Socket;

/**
 * AbstractProcessor class is responsible for processing commands received from the client.
 * It handles the command execution and interacts with the appropriate services.
 * This class uses a chain of responsibility pattern to delegate the command processing to the appropriate handler.
 */

public abstract class AbstractProcessor {
    protected AbstractProcessor nextProcessor;

    public void setNextProcessor(AbstractProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    public abstract String processCommand(String command);

    protected String handleNext(String command) {
        if (nextProcessor != null) {
            return nextProcessor.processCommand(command);
        }
        return "Unknown command. Type 'help' for available commands.";
    }
}
