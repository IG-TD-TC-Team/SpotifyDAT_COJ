package persistence;


import songsOrganisation.Library;
import persistence.interfaces.LibraryRepositoryInterface;
import songsOrganisation.Playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A repository for persisting and retrieving Library objects.
 * Extends the generic JsonRepository to provide Library-specific functionalities.
 */
public class LibraryRepository extends JsonRepository<Library> implements LibraryRepositoryInterface  {
    /**
     * Singleton instance of LibraryRepository.
     */
    private static LibraryRepository instance;

    /**
     * Private constructor that initializes the repository.
     * This constructor calls the superclass constructor with the Library class type and the JSON file name.
     * It also specifies the method to get the ID of a Library object.
     *
     */
    private LibraryRepository() {
        super(Library.class, "Library.json", Library::getUserID);
    }

    /**
     * Gets the singleton instance of LibraryRepository.
     *
     * @return The singleton instance of LibraryRepository.
     */
    public static synchronized LibraryRepository getInstance() {
        if (instance == null) {
            instance = new LibraryRepository();
        }
        return instance;
    }

    /**
     * Adds a playlist to a user's library.
     * If the library doesn't exist, it creates a new one.
     *
     * @param userId The ID of the user.
     * @param playlist The playlist to be added.
     * @return true if the playlist was added successfully, false otherwise.
     */
    @Override
    public boolean addPlaylistToLibrary(int userId, Playlist playlist) {
        Optional<Library> libraryOpt = findById(userId);

        if (libraryOpt.isPresent()) {
            Library library = libraryOpt.get();
            List<Playlist> playlists = library.getPlaylists();

            // Check if the playlist already exists in the library
            boolean playlistExists = playlists.stream()
                    .anyMatch(p -> p.getName().equals(playlist.getName()) &&
                            p.getOwnerID() == playlist.getOwnerID());

            if (!playlistExists) {
                playlists.add(playlist);
                update(library);
                return true;
            }

            return true; // Playlist already exists in the library
        }

        // If library doesn't exist, create it
        Library library = createLibrary(userId);
        library.addPlaylist(playlist);
        update(library);
        return true;
    }

    /**
     * Removes a playlist from a user's library.
     *
     * @param userId The ID of the user.
     * @param playlistName The name of the playlist to be removed.
     * @return true if the playlist was removed successfully, false otherwise.
     */
    @Override
    public boolean removePlaylistFromLibrary(int userId, String playlistName) {
        Optional<Library> libraryOpt = findById(userId);

        if (libraryOpt.isPresent()) {
            Library library = libraryOpt.get();
            List<Playlist> playlists = library.getPlaylists();

            boolean removed = playlists.removeIf(p -> p.getName().equals(playlistName));

            if (removed) {
                update(library);
                return true;
            }

            return false; // Playlist not found in the library
        }

        return false; // Library not found
    }

    /**
     * Gets all playlists in a user's library.
     *
     * @param userId The ID of the user.
     * @return A list of playlists in the user's library.
     */
    @Override
    public List<Playlist> getUserPlaylists(int userId) {
        Optional<Library> libraryOpt = findById(userId);

        if (libraryOpt.isPresent()) {
            return libraryOpt.get().getPlaylists();
        }

        return new ArrayList<>(); // Return empty list if library doesn't exist
    }

    /**
     * Creates a new library for a user if it doesn't exist.
     *
     * @param userId The ID of the user.
     * @return The created or existing library.
     */
    @Override
    public Library createLibrary(int userId) {
        Optional<Library> existingLibrary = findById(userId);

        if (existingLibrary.isPresent()) {
            return existingLibrary.get(); // Library already exists
        }

        Library library = new Library(userId, new ArrayList<>());
        save(library);
        return library;

    }
}
