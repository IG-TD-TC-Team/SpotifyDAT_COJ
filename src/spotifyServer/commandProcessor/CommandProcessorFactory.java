package spotifyServer.commandProcessor;

/**
 * CommandProcessorFactory class is responsible for creating the chain of command processors.
 * It initializes the processors and sets up the chain of responsibility.
 */
public class CommandProcessorFactory {
    /**
     * Creates a chain of command processors.
     *
     * @return The first processor in the chain.
     */
    public static AbstractProcessor createProcessorChain() {
        // Create the chain of processors
        HelpCommandProcessor helpProcessor = new HelpCommandProcessor();
        PlayCommandProcessor playProcessor = new PlayCommandProcessor();
        PlaylistCommandProcessor playlistProcessor = new PlaylistCommandProcessor();
        SearchCommandProcessor searchProcessor = new SearchCommandProcessor();
        DefaultCommandProcessor defaultProcessor = new DefaultCommandProcessor();

        // Connect the chain
        helpProcessor.setNextProcessor(playProcessor);
        playProcessor.setNextProcessor(playlistProcessor);
        playlistProcessor.setNextProcessor(searchProcessor);
        searchProcessor.setNextProcessor(defaultProcessor);

        return helpProcessor;
    }
}
