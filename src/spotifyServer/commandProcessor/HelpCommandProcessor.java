package spotifyServer.commandProcessor;

class HelpCommandProcessor extends AbstractProcessor {
    @Override
    public String processCommand(String command) {
        if (command.equalsIgnoreCase("help")) {
            return "Available commands:\n" +
                    "help - Show this help message\n" +
                    "play <song_id> - Play a song\n" +
                    "playlist <playlist_id> - Play a playlist\n" +
                    "search <query> - Search for songs or artists\n" +
                    "exit - Close the connection";
        }
        return handleNext(command);
    }
}
