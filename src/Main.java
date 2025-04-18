import songsAndArtists.*;
import persistence.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Main {
    /**
     * Dear Tatiana:
     * Main method to demonstrate the functionality of the application.
     * It creates artists, songs, and albums, adds them to repositories, and then loads them back.
     * There is one Issue I didnt have time to fix: the double save done in the method saveAll() of the JsonRepository.
     */
    public static void main(String[] args) {
        // Set up repositories
        JsonRepository<Artist> artistRepo = new JsonRepository<>(Artist.class, "artists.json");
        JsonRepository<Song> songRepo = new JsonRepository<>(Song.class, "songs.json");
        JsonRepository<Album> albumRepo = new JsonRepository<>(Album.class, "albums.json");

        // Create and add artists
        Artist daftPunk = new Artist(
                1,
                "Daft",
                "Punk",
                new Date(1973 - 1900, 1, 1), // Feb 1, 1973 (remember: 0-indexed month, and year offset)
                "France"
        );

        Artist davidBowie = new Artist(
                2,
                "David",
                "Bowie",
                new Date(1947 - 1900, 0, 8), // Jan 8, 1947
                "UK"
        );

        // Add artists to repository
        artistRepo.add(daftPunk);
        artistRepo.add(davidBowie);

        // Create and add songs for Daft Punk - now using Genre enum
        Song song1 = new Song(101, "Get Lucky", "1", "1001", Genre.ELECTRONIC, 248, "/music/getlucky.mp3");
        Song song2 = new Song(102, "Harder, Better, Faster, Stronger", "1", "1001", Genre.ELECTRONIC, 224, "/music/harder.mp3");
        Song song3 = new Song(103, "Around the World", "1", "1002", Genre.ELECTRONIC, 429, "/music/aroundtheworld.mp3");

        // Create and add songs for David Bowie - now using Genre enum
        Song song4 = new Song(201, "Space Oddity", "2", "2001", Genre.ROCK, 315, "/music/spaceoddity.mp3");
        Song song5 = new Song(202, "Heroes", "2", "2001", Genre.ROCK, 371, "/music/heroes.mp3");
        Song song6 = new Song(203, "Life on Mars?", "2", "2002", Genre.ROCK, 236, "/music/lifeonmars.mp3");

        // Add songs to repository
        songRepo.add(song1);
        songRepo.add(song2);
        songRepo.add(song3);
        songRepo.add(song4);
        songRepo.add(song5);
        songRepo.add(song6);

        // Update artists with song IDs
        daftPunk.setSongs(Arrays.asList(101, 102, 103));
        davidBowie.setSongs(Arrays.asList(201, 202, 203));

        // Create and add albums
        Album randomAccessMemories = new Album(1001, "Random Access Memories", "1", Arrays.asList(101, 102));
        Album homework = new Album(1002, "Homework", "1", Arrays.asList(103));
        Album hunkyDory = new Album(2001, "Hunky Dory", "2", Arrays.asList(201, 202));
        Album ziggyStardust = new Album(2002, "The Rise and Fall of Ziggy Stardust", "2", Arrays.asList(203));

        // Add albums to repository
        albumRepo.add(randomAccessMemories);
        albumRepo.add(homework);
        albumRepo.add(hunkyDory);
        albumRepo.add(ziggyStardust);

        // Print all loaded artists
        List<Artist> loadedArtists = artistRepo.findAll();
        System.out.println("Loaded " + loadedArtists.size() + " artists:");
        for (Artist loadedArtist : loadedArtists) {
            System.out.println("- " + loadedArtist.getFirstName() + " " + loadedArtist.getLastName() +
                    " from " + loadedArtist.getCountryOfBirth());
        }

        // Print all loaded songs
        List<Song> loadedSongs = songRepo.findAll();
        System.out.println("\nLoaded " + loadedSongs.size() + " songs:");
        for (Song loadedSong : loadedSongs) {
            System.out.println("- " + loadedSong.getTitle() + " (Genre: " + loadedSong.getGenre() +
                    ", " + loadedSong.getDurationSeconds() + " seconds)");
        }

        // Print all loaded albums
        List<Album> loadedAlbums = albumRepo.findAll();
        System.out.println("\nLoaded " + loadedAlbums.size() + " albums:");
        for (Album loadedAlbum : loadedAlbums) {
            System.out.println("- " + loadedAlbum.getTitle() + " by Artist ID: " + loadedAlbum.getArtistId() +
                    " with " + loadedAlbum.getSongs().size() + " songs");
        }

        // Demonstrate album repository functions
       /* System.out.println("\nAlbums by Daft Punk:");
        List<Album> daftPunkAlbums = albumRepo.findByArtistId("1");
        for (Album album : daftPunkAlbums) {
            System.out.println("- " + album.getTitle());
        }*/

        // Demonstrate adding a song to an album
        /*System.out.println("\nAdding a song to Random Access Memories...");
        albumRepo.addSongToAlbum(1001, 103);
        Album updatedAlbum = albumRepo.findById(1001).orElse(null);
        if (updatedAlbum != null) {
            System.out.println("Updated album now has " + updatedAlbum.getSongs().size() + " songs");
        }*/

        // Demonstrate finding songs by genre
        System.out.println("\nAll ROCK songs:");
        List<Song> rockSongs = songRepo.findAll().stream()
                .filter(song -> song.getGenre() == Genre.ROCK)
                .toList();
        for (Song song : rockSongs) {
            System.out.println("- " + song.getTitle() + " by Artist ID: " + song.getArtistId());
        }
    }
}