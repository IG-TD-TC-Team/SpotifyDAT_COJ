package spotifyServer.commandProcessor;

/**
 * DefaultCommandProcessor class is responsible for handling unknown commands.
 * It provides a default response when no other processor can handle the command.
 */
class DefaultCommandProcessor extends AbstractProcessor {
    @Override
    public String processCommand(String command) {
        return "Unknown command: " + command + ". Type 'help' for available commands.";
    }
}