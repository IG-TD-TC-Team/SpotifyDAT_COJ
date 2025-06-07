// Updated CommandProcessorFactory.java
package spotifyServer.commandProcessor;

/**
 * CommandProcessorFactory class creates and connects the chain of command processors.
 * Each processor handles specific types of commands and passes unhandled commands
 * to the next processor in the chain.
 */
public class CommandProcessorFactory {
    private static CommandProcessorFactory instance;

    private CommandProcessorFactory() {
        // Private constructor for singleton
    }

    /**
     * Returns the singleton instance, creating it if needed.
     */
    public static synchronized CommandProcessorFactory getInstance() {
        if (instance == null) {
            instance = new CommandProcessorFactory();
        }
        return instance;
    }

    /**
     * Creates a complete chain of command processors.
     *
     * IMPORTANT: The order matters! Commands flow through processors in this sequence.
     * Each processor either handles the command or passes it to the next one.
     *
     * @return The first processor in the chain (entry point)
     */
    public AbstractProcessor createProcessorChainInstance() {
        // Create all processor instances
        HelpCommandProcessor helpProcessor = new HelpCommandProcessor();
        AuthenticationCommandProcessor authProcessor = new AuthenticationCommandProcessor();
        ProfileCommandProcessor profileProcessor = new ProfileCommandProcessor();
        SocialCommandProcessor socialProcessor = new SocialCommandProcessor();
        SubscriptionCommandProcessor subscriptionProcessor = new SubscriptionCommandProcessor();
        PlaylistManagementCommandProcessor playlistManagementProcessor = new PlaylistManagementCommandProcessor();
        PlaylistSocialCommandProcessor playlistSocialProcessor = new PlaylistSocialCommandProcessor();

        // Playback-related processors
        PlayCommandProcessor playProcessor = new PlayCommandProcessor();
        PlaylistNavigationProcessor playlistNavProcessor = new PlaylistNavigationProcessor();
        PlaylistCommandProcessor playlistProcessor = new PlaylistCommandProcessor();
        PlaybackControlCommandProcessor playbackControlProcessor = new PlaybackControlCommandProcessor(); // <-- THIS WAS MISSING!

        // Search and utility processors
        SearchCommandProcessor searchProcessor = new SearchCommandProcessor();
        ListMusicCommandProcessor listMusicProcessor = new ListMusicCommandProcessor();
        DefaultCommandProcessor defaultProcessor = new DefaultCommandProcessor();

        // Connect the chain
        // Each processor's setNextProcessor() method connects it to the next one
        helpProcessor.setNextProcessor(authProcessor);
        authProcessor.setNextProcessor(profileProcessor);
        profileProcessor.setNextProcessor(socialProcessor);
        socialProcessor.setNextProcessor(subscriptionProcessor);
        subscriptionProcessor.setNextProcessor(playlistManagementProcessor);
        playlistManagementProcessor.setNextProcessor(playlistSocialProcessor);
        playlistSocialProcessor.setNextProcessor(playProcessor);
        playProcessor.setNextProcessor(playlistProcessor);

        //Connect the playback control processor
        playlistProcessor.setNextProcessor(playbackControlProcessor);
        playbackControlProcessor.setNextProcessor(searchProcessor);

        searchProcessor.setNextProcessor(listMusicProcessor);
        listMusicProcessor.setNextProcessor(playlistNavProcessor);
        playlistNavProcessor.setNextProcessor(defaultProcessor);

        // Return the first processor (entry point for all commands)
        return helpProcessor;
    }
}