import songsAndArtists.*;
import persistence.*;
import songsManagement.*;
import subscription.*;

import java.util.Arrays;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        // Create an example artist
        Artist artist = new Artist(
                1,
                "Daft",
                "Punk",
                new Date(1973 - 1900, 1, 1), // Feb 1, 1973 (remember: 0-indexed month, and year offset)
                "France"
        );
        artist.setSongs(Arrays.asList(101, 102, 103)); // Fake song IDs

        // Create a repository for artists and save this artist
        JsonRepository<Artist> artistRepo = new JsonRepository<>("artists.json", Artist.class);

        artistRepo.save(artist);

        // Load the saved artist
        Artist loadedArtist = artistRepo.load();
        System.out.println("Loaded artist: " + loadedArtist.getFirstName() + " " + loadedArtist.getLastName());
    }
}
