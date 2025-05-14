package spotifyServer.commandProcessor;

/**
 * HelpCommandProcessor that integrates with the enhanced
 * Chain of Responsibility pattern with direct socket handling.
 *
 * This processor provides help information directly to the client
 * through the established socket connection.
 */
public class HelpCommandProcessor extends AbstractProcessor {
    @Override
    public String processCommand(String command) {
        if (command.equalsIgnoreCase("help")) {
            // Create a comprehensive help message
            StringBuilder help = new StringBuilder();
            help.append("Available commands in Spotify Server:\n");
            help.append("=====================================\n\n");

            help.append("Basic Commands:\n");
            help.append("  help          - Show this help message\n");
            help.append("  exit          - Close the connection\n\n");

            help.append("Playback Commands:\n");
            help.append("  play <song_id>     - Play a song by its ID\n");
            help.append("  playlist <id>      - Play an entire playlist by its ID\n\n");

            help.append("Search Commands:\n");
            help.append("  search <query>     - Search for songs by title\n\n");

            help.append("More features coming soon!\n");
            help.append("=====================================\n");

            return help.toString();
        }

        // Pass to next processor if this one can't handle it
        return handleNext(command);
    }
}