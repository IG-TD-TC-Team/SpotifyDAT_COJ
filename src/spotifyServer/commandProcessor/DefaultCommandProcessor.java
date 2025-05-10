package spotifyServer.commandProcessor;

import java.net.Socket;

// Default command processor (end of chain)
class DefaultCommandProcessor extends AbstractProcessor {
    @Override
    public String processCommand(String command) {
        return "Unknown command: " + command + ". Type 'help' for available commands.";
    }
}