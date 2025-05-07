package services.playlistServices;

import factory.RepositoryFactory;
import persistence.PlaylistRepository;
import persistence.interfaces.PlaylistRepositoryInterface;
import songsOrganisation.Playlist;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SocialPlaylistService {

    /**
     * Singleton instance of SocialPlaylistService.
     */
    private static SocialPlaylistService instance;

    /**
     * Repository for accessing playlist data.
     */
    private final PlaylistRepositoryInterface playlistRepository;

    /**
     * Cache of all playlists for faster retrieval.
     */
    private List<Playlist> playlists;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the PlaylistRepository instance and loads all playlists.
     */
    private SocialPlaylistService() {
        this.playlistRepository = RepositoryFactory.getInstance().getPlaylistRepository();
        refreshCache();
    }
    /**
     * Returns the single instance of SocialPlaylistService, creating it if it doesn't exist.
     *
     * @return the singleton instance of SocialPlaylistService
     */
    public static synchronized SocialPlaylistService getInstance() {
        if (instance == null) {
            instance = new SocialPlaylistService();
        }
        return instance;
    }

/// ------------------CACHE REFRESH------------------ ///
    /**
     * Refreshes the cache of playlists by retrieving all playlists from the repository.
     */
    public void refreshCache() {
        this.playlists = playlistRepository.findAll();
    }


    /// --------------PLAYLIST SOCIAL RETRIEVAL------------------ ///
    /**
     * Retrieves all playlists shared with a specific user.
     *
     * @param userID the ID of the user
     * @return a list of playlists shared with the specified user
     */
    public List<Playlist> getPlaylistsSharedWithUser(int userID) {
        refreshCache();
        return playlists.stream()
                .filter(playlist -> playlist.getSharedWith().contains(userID))
                .collect(Collectors.toList());
    }

    /// ----------------PLAYLIST SOCIAL UPDATE------------------ ///
    /**
     * Shares a playlist with another user.
     *
     * @param playlistId the ID of the playlist
     * @param userIdToShareWith the ID of the user to share with
     * @return true if the playlist was shared, false otherwise
     */
    public boolean sharePlaylist(int playlistId, int userIdToShareWith) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Check if already shared with user
        if (playlistRepository.isPlaylistSharedWithUser(playlistId, userIdToShareWith)){
            return true; // Already shared
        }

        playlistRepository.sharePlaylistWithUser(playlist.getName(), playlist.getOwnerID(), userIdToShareWith);
        playlistRepository.update(playlist);

        return true;
    }

    /**
     * Unshares a playlist with a user.
     *
     * @param playlistId the ID of the playlist
     * @param userIdToUnshareWith the ID of the user to unshare with
     * @return true if the playlist was unshared, false otherwise
     */
    public boolean unsharePlaylist(int playlistId, int userIdToUnshareWith) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Check if shared with user
        if (!playlistRepository.isPlaylistSharedWithUser(playlistId, userIdToUnshareWith)) {
            return false; // Not shared with this user
        }

        playlistRepository.unsharePlaylistWithUser(playlist.getName(), playlist.getOwnerID(), userIdToUnshareWith);
        playlistRepository.update(playlist);

        return true;
    }

}
