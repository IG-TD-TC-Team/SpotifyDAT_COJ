package spotifyServer.commandProcessor;

/**
 * DefaultCommandProcessor that integrates with the enhanced
 * Chain of Responsibility pattern with direct socket handling.
 *
 * This processor handles any commands that weren't caught by
 * other processors in the chain.
 */
public class DefaultCommandProcessor extends AbstractProcessor {
    @Override
    public String processCommand(String command) {
        // This is the last processor in the chain, so we handle any unknown commands
        return "Unknown command: '" + command + "'. Type 'help' for available commands.";
    }
}