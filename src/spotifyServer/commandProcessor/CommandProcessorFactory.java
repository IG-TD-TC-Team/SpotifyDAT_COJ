package spotifyServer.commandProcessor;

import services.songServices.SongService;
import services.playlistServices.PlaylistService;
import songsAndArtists.Song;
import songsOrganisation.Playlist;

public class CommandProcessorFactory {
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
