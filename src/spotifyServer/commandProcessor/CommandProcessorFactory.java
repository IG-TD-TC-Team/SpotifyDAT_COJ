package spotifyServer.commandProcessor;

/**
 * CommandProcessorFactory class is responsible for creating the chain of command processors.
 * It initializes the processors and sets up the chain of responsibility.
 */
public class CommandProcessorFactory {
    /**
     * Private constructor to prevent instantiation.
     */
    private static CommandProcessorFactory instance;


    private CommandProcessorFactory() {

    }
    public static synchronized CommandProcessorFactory getInstance() {
        if (instance == null) {
            instance = new CommandProcessorFactory();
        }
        return instance;
    }

            /**
             * Creates a chain of command processors.
             *
             * @return The first processor in the chain.
             */

            public AbstractProcessor createProcessorChainInstance() {
            // Create new instances of each processor
            HelpCommandProcessor helpProcessor = new HelpCommandProcessor();
            AuthenticationCommandProcessor authProcessor = new AuthenticationCommandProcessor();
            ProfileCommandProcessor profileProcessor = new ProfileCommandProcessor();
            SocialCommandProcessor socialProcessor = new SocialCommandProcessor();
            SubscriptionCommandProcessor subscriptionProcessor = new SubscriptionCommandProcessor();
            PlaylistManagementCommandProcessor playlistManagementProcessor = new PlaylistManagementCommandProcessor();
            PlaylistSocialCommandProcessor playlistSocialProcessor = new PlaylistSocialCommandProcessor();
            PlayCommandProcessor playProcessor = new PlayCommandProcessor();
            PlaylistCommandProcessor playlistProcessor = new PlaylistCommandProcessor();
            SearchCommandProcessor searchProcessor = new SearchCommandProcessor();
            ListMusicCommandProcessor listMusicProcessor = new ListMusicCommandProcessor();
            DefaultCommandProcessor defaultProcessor = new DefaultCommandProcessor();

            // Connect the chain
            helpProcessor.setNextProcessor(authProcessor);
            authProcessor.setNextProcessor(profileProcessor);
            profileProcessor.setNextProcessor(socialProcessor);
            socialProcessor.setNextProcessor(subscriptionProcessor);
            subscriptionProcessor.setNextProcessor(playlistManagementProcessor);
            playlistManagementProcessor.setNextProcessor(playlistSocialProcessor);
            playlistSocialProcessor.setNextProcessor(playProcessor);
            playProcessor.setNextProcessor(playlistProcessor);
            playlistProcessor.setNextProcessor(searchProcessor);
            searchProcessor.setNextProcessor(listMusicProcessor);
            listMusicProcessor.setNextProcessor(defaultProcessor);

            return helpProcessor;
        }
    }

