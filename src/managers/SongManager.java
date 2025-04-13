package managers;
import songsOrganisation.*;
import songsAndArtists.*;
import persistence.*;

import java.util.Optional;


public class SongManager {
    ArtistRepository artisRepo;
    SongRepository songRepo;
    AlbumRepository albumRepo;


    public SongManager() {
        JsonRepository<Artist> artistRepo = new JsonRepository<>(Artist.class, "artists.json");
        JsonRepository<Song> songRepo = new JsonRepository<>(Song.class, "songs.json");
        JsonRepository<Album> albumRepo = new JsonRepository<>(Album.class, "albums.json");
    }

    public void createSong(Song song) {
        JsonRepository<Song> songRepo = new JsonRepository<>(Song.class, "songs.json");
        songRepo.add(song);
    }

    public Song getSongById(int songId) {
        Optional<Song> songOptional = songRepo.findById(songId);



        // Option 1: Return the song if present, otherwise null
        return songOptional.orElse(null);

        //Option 2: Return the song if present, otherwise throw an exception
        // return songOptional.orElseThrow(() -> new RuntimeException("Song not found with id: " + songId));
    }
}
