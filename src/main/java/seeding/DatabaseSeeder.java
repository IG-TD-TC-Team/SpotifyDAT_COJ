package seeding;

import factory.MusicFactory;
import songsAndArtists.Genre;
import songsAndArtists.Song;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseSeeder class responsible for populating the music database with initial song data.
 * This class uses the MusicFactory's createCompleteSong method to systematically add
 * artists, albums, and songs to the database based on the existing music collection.
 *
 * The seeder organizes music by genre and creates proper relationships between
 * artists, albums, and songs while generating realistic file paths and durations.
 */
public class DatabaseSeeder {

    private final MusicFactory musicFactory;
    private final List<Song> createdSongs;

    /**
     * Constructor initializes the music factory and prepares for seeding.
     */
    public DatabaseSeeder() {
        this.musicFactory = MusicFactory.getInstance();
        this.createdSongs = new ArrayList<>();
    }

    /**
     * Main method to execute the complete database seeding process.
     * This orchestrates the creation of all music content in the database.
     */
    public void seedDatabase() {
        System.out.println("Starting database seeding process...");

        // Seed different genres systematically
        seedChildrensMusic();
        seedFrenchPop();
        seedLatinMusic();
        seedHipHopMusic();
        seedBrazilianMusic();
        seedElectronicMusic();

        System.out.println("Database seeding completed successfully!");
        System.out.println("Total songs created: " + createdSongs.size());

        // Display summary of what was created
        displaySeedingSummary();
    }

    /**
     * Seeds children's music from various French and international artists.
     * This category includes educational songs, nursery rhymes, and family entertainment.
     */
    private void seedChildrensMusic() {
        System.out.println("Seeding children's music...");

        // Aldebert - French children's entertainer
        createSongsForAldebert();

        // La Reine des chansons pour enfants et bébés - Classic French nursery rhymes
        createFrenchNurseryRhymes();

        // Mazapán - Chilean children's music
        createMazapanSongs();

        // Mister Toony - Children's music
        createMisterToonySongs();

        // Monde des Titounis - Educational children's content
        createTitounisSongs();
    }

    /**
     * Creates Aldebert's live concert songs.
     * Aldebert is a popular French children's musician known for clever, family-friendly lyrics.
     */
    private void createSongsForAldebert() {
        String albumTitle = "Enfantillages 2 - le concert (Live)";

        // Each song from the live album with estimated durations based on file sizes
        createSong("E2 dans l'espace (Live)", "Aldebert", "", albumTitle, Genre.POP, 180);
        createSong("Mon petit doigt m'a dit (Live)", "Aldebert", "", albumTitle, Genre.POP, 240);
        createSong("Mon père il est tellement fort (Live)", "Aldebert", "", albumTitle, Genre.POP, 270);
        createSong("Les amoureux (Live)", "Aldebert", "", albumTitle, Genre.POP, 235);
        createSong("Super Mamie (Live)", "Aldebert", "", albumTitle, Genre.POP, 285);
        createSong("Le dragon (Live)", "Aldebert", "", albumTitle, Genre.POP, 225);
        createSong("Le p'tit veut faire de la trompette (Live)", "Aldebert", "", albumTitle, Genre.POP, 250);
        createSong("La vie d'écolier (Live)", "Aldebert", "", albumTitle, Genre.POP, 295);
        createSong("Pour louper l'école (Live)", "Aldebert", "", albumTitle, Genre.POP, 300);
        createSong("Dans la maison de mon arrière-grand-père (Live)", "Aldebert", "", albumTitle, Genre.POP, 205);
        createSong("Samir le fakir (Live)", "Aldebert", "", albumTitle, Genre.POP, 290);
        createSong("Y'a rien qui va (Live)", "Aldebert", "", albumTitle, Genre.POP, 220);
        createSong("On ne peut rien faire quand on est petit (Live)", "Aldebert", "", albumTitle, Genre.POP, 200);
        createSong("La soucoupe volante (Live)", "Aldebert", "", albumTitle, Genre.POP, 300);
        createSong("Du gros son (Live)", "Aldebert", "", albumTitle, Genre.POP, 315);
        createSong("Plus tard quand tu seras grand (Live)", "Aldebert", "", albumTitle, Genre.POP, 260);
        createSong("Qu'est-ce qu'on va faire de moi ? (Live)", "Aldebert", "", albumTitle, Genre.POP, 280);
    }

    /**
     * Creates classic French nursery rhymes and children's songs.
     * These are traditional songs that teach children language, counting, and cultural values.
     */
    private void createFrenchNurseryRhymes() {
        String albumTitle = "80 Comptines pour enfants et bébés";
        String artistFirst = "La Reine des chansons pour enfants";
        String artistLast = "et bébés";

        // Classic French nursery rhymes with traditional melodies
        createSong("Ah les crocodiles", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 180);
        createSong("Pirouette, cacahuète", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 120);
        createSong("Au claire de la lune", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 110);
        createSong("Une souris verte", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 60);
        createSong("Promenons-nous dans les bois", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 105);
        createSong("Pomme de reinette et pomme d'api", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 55);
        createSong("Frère Jacques", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 75);
        createSong("Alouette, gentille alouette", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 110);
        createSong("Un éléphant qui se balançait", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 140);
        createSong("Il était un petit navire", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 305);
        // Adding more traditional favorites
        createSong("Ainsi font font font", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 95);
        createSong("Un grand cerf", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 80);
        createSong("Sur le pont d'Avignon", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 110);
        createSong("Fais dodo, Colas mon p'tit frère", artistFirst, artistLast, albumTitle, Genre.CLASSICAL, 130);
    }

    /**
     * Seeds contemporary French pop music from Belgium and France.
     * This includes modern chanson and pop artists with social commentary.
     */
    private void seedFrenchPop() {
        System.out.println("Seeding French pop music...");

        // Angèle - Belgian pop sensation with social awareness themes
        createAngeleMusic();

        // Bénabar - French chansonnier with observational humor
        createBenabarMusic();

        // Gauvain Sers - Contemporary French singer-songwriter
        createGauvainSersMusic();

        // MC Solaar - French hip-hop pioneer (will be categorized as hip-hop)
        createMcSolaarMusic();
    }

    /**
     * Creates Angèle's popular songs from her breakthrough albums.
     * Angèle represents the new wave of Belgian pop with feminist themes.
     */
    private void createAngeleMusic() {
        // Original Brol album - her debut
        createSong("La thune", "Angèle", "", "Brol", Genre.POP, 230);
        createSong("Balance ton quoi", "Angèle", "", "Brol", Genre.POP, 215);

        // Brol La Suite - expanded edition with more hits
        createSong("Perdus", "Angèle", "", "Brol La Suite", Genre.POP, 210);
        createSong("Oui ou non", "Angèle", "", "Brol La Suite", Genre.POP, 225);
        createSong("Insomnies", "Angèle", "", "Brol La Suite", Genre.POP, 240);
        createSong("Jalousie", "Angèle", "", "Brol La Suite", Genre.POP, 255);
        createSong("Tout oublier", "Angèle", "", "Brol La Suite", Genre.POP, 230);
        createSong("Tu me regardes", "Angèle", "", "Brol La Suite", Genre.POP, 220);
        createSong("J'entends", "Angèle", "", "Brol La Suite", Genre.POP, 245);
        createSong("La loi de Murphy", "Angèle", "", "Brol La Suite", Genre.POP, 220);
        createSong("Nombreux", "Angèle", "", "Brol La Suite", Genre.POP, 220);
        createSong("Que du love (feat. Kiddy Smile)", "Angèle", "", "Brol La Suite", Genre.POP, 175);
        createSong("Ta reine (Version orchestrale)", "Angèle", "", "Brol La Suite", Genre.POP, 310);
        createSong("Victime des réseaux", "Angèle", "", "Brol La Suite", Genre.POP, 230);
        createSong("Les matins", "Angèle", "", "Brol La Suite", Genre.POP, 200);
        createSong("Je veux tes yeux", "Angèle", "", "Brol La Suite", Genre.POP, 235);
        createSong("Ta reine", "Angèle", "", "Brol La Suite", Genre.POP, 240);
        createSong("Flemme", "Angèle", "", "Brol La Suite", Genre.POP, 290);
        createSong("Flou", "Angèle", "", "Brol La Suite", Genre.POP, 225);
    }

    /**
     * Creates Bénabar's observational songs about French life.
     * Known for his witty lyrics about everyday situations.
     */
    private void createBenabarMusic() {
        createSong("Le dîner", "Bénabar", "", "Best Of", Genre.POP, 205);
        createSong("L'effet papillon", "Bénabar", "", "Infréquentable", Genre.POP, 285);
    }

    /**
     * Creates Gauvain Sers' contemporary French songs with social themes.
     * Represents modern chanson with rural and working-class perspectives.
     */
    private void createGauvainSersMusic() {
        String albumTitle1 = "Les Oubliés";
        String albumTitle2 = "Pourvu";

        // Les Oubliés album - songs about forgotten people and places
        createSong("Les oubliés", "Gauvain", "Sers", albumTitle1, Genre.POP, 240);
        createSong("Ton jean bleu", "Gauvain", "Sers", albumTitle1, Genre.POP, 265);
        createSong("La langue de Prévert", "Gauvain", "Sers", albumTitle1, Genre.POP, 215);
        createSong("L'épaule d'un copain", "Gauvain", "Sers", albumTitle1, Genre.POP, 250);
        createSong("La boîte à chaussures", "Gauvain", "Sers", albumTitle1, Genre.POP, 235);
        createSong("Excuse-moi mon amour", "Gauvain", "Sers", albumTitle1, Genre.POP, 215);
        createSong("Au pays des Lumières", "Gauvain", "Sers", albumTitle1, Genre.POP, 245);
        createSong("Le tiroir", "Gauvain", "Sers", albumTitle1, Genre.POP, 205);
        createSong("Changement de programme", "Gauvain", "Sers", albumTitle1, Genre.POP, 280);
        createSong("Petite piaule", "Gauvain", "Sers", albumTitle1, Genre.POP, 225);
        createSong("Tu sais mon grand", "Gauvain", "Sers", albumTitle1, Genre.POP, 240);
        createSong("L'étudiante", "Gauvain", "Sers", albumTitle1, Genre.POP, 270);
        createSong("Y'a pas de retraite pour les artistes", "Gauvain", "Sers", albumTitle1, Genre.POP, 235);
        createSong("Que restera-t-il de nous ?", "Gauvain", "Sers", albumTitle1, Genre.POP, 330);

        // Pourvu album - more recent material
        createSong("Pourvu", "Gauvain", "Sers", albumTitle2, Genre.POP, 230);
        createSong("Dans mes poches", "Gauvain", "Sers", albumTitle2, Genre.POP, 225);
    }

    /**
     * Seeds Latin music from various Spanish-speaking artists.
     * Includes reggaeton, Latin pop, and contemporary Latin sounds.
     */
    private void seedLatinMusic() {
        System.out.println("Seeding Latin music...");

        // Enrique Iglesias - Spanish pop icon
        createSong("EL BAÑO (feat. Bad Bunny)", "Enrique", "Iglesias", "EL BAÑO", Genre.POP, 260);

        // Luis Fonsi & Demi Lovato - Latin pop collaboration
        createSong("Échame La Culpa", "Luis Fonsi", "Demi Lovato", "Échame La Culpa", Genre.POP, 200);

        // Juanes - Colombian rock/pop
        createSong("Fuego", "Juanes", "", "Fuego", Genre.ROCK, 190);
        createSong("La Camisa Negra", "Juanes", "", "Mi Sangre", Genre.ROCK, 245);

        // Mon Laferte - Chilean singer with diverse styles
        createSong("Amárrame (feat. Juanes)", "Mon", "Laferte", "La Trenza (Deluxe)", Genre.POP, 235);
        createSong("Tu Falta De Querer", "Mon", "Laferte", "Mon Laferte (Vol. 1: Edicion Especial)", Genre.POP, 315);

        // Chocolate - Latin dance music
        createSong("Mayonesa", "Chocolate", "", "2000 Grandes Exitos", Genre.POP, 260);
    }

    /**
     * Seeds hip-hop music, primarily French rap.
     * MC Solaar represents the sophisticated side of French hip-hop.
     */
    private void seedHipHopMusic() {
        System.out.println("Seeding hip-hop music...");

        createMcSolaarMusic();

        // Lizzo - Contemporary hip-hop/pop
        createSong("Juice (Breakbot Mix)", "Lizzo", "", "Juice (Breakbot Mix)", Genre.HIP_HOP, 195);
    }

    /**
     * Creates MC Solaar's classic French rap songs.
     * Known for sophisticated wordplay and jazz-influenced production.
     */
    private void createMcSolaarMusic() {
        String albumTitle = "Le tour de la question";

        createSong("Bouge de là", "MC", "Solaar", albumTitle, Genre.HIP_HOP, 210);
        createSong("Victime de la mode", "MC", "Solaar", albumTitle, Genre.HIP_HOP, 195);
        createSong("Les temps changent", "MC", "Solaar", albumTitle, Genre.HIP_HOP, 245);
        createSong("Caroline", "MC", "Solaar", albumTitle, Genre.HIP_HOP, 350);
    }

    /**
     * Seeds Brazilian music, representing the rich musical culture of Brazil.
     * Includes MPB (Música Popular Brasileira) and other Brazilian genres.
     */
    private void seedBrazilianMusic() {
        System.out.println("Seeding Brazilian music...");

        // Gilberto Gil - Brazilian music legend
        createSong("Palco", "Gilberto", "Gil", "Quanta Gente Veio Ver (Ao Vivo)", Genre.POP, 270);
    }

    /**
     * Seeds electronic and dance music.
     * This category could be expanded with more electronic artists.
     */
    private void seedElectronicMusic() {
        System.out.println("Seeding electronic music...");

        // Currently represented by Lizzo's remix, but this could be expanded
        // The Breakbot Mix of Juice has electronic elements
    }

    /**
     * Creates Mazapán's Chilean children's songs.
     * Mazapán is known for educational and entertaining children's content in Spanish.
     */
    private void createMazapanSongs() {
        String albumTitle = "Antología (Volumen 2)";
        String artistFirst = "Mazapán";
        String artistLast = "";

        createSong("Este Dedito/Mis Deditos", artistFirst, artistLast, albumTitle, Genre.POP, 140);
        createSong("Vamos a Jugar", artistFirst, artistLast, albumTitle, Genre.POP, 150);
        createSong("Carnavalito del Ciempiés", artistFirst, artistLast, albumTitle, Genre.POP, 135);
        createSong("La Vaquita Loca", artistFirst, artistLast, albumTitle, Genre.POP, 160);
        createSong("Mazamorra del Poroto Coscorrón", artistFirst, artistLast, albumTitle, Genre.POP, 165);
        createSong("Chapecao de Segundo", artistFirst, artistLast, albumTitle, Genre.POP, 160);
        createSong("Payas del Rezongo", artistFirst, artistLast, albumTitle, Genre.POP, 195);
        createSong("Canción Araucana", artistFirst, artistLast, albumTitle, Genre.POP, 165);
        createSong("Jerónimo", artistFirst, artistLast, albumTitle, Genre.POP, 205);
        createSong("Esta Noche Bailaré", artistFirst, artistLast, albumTitle, Genre.POP, 105);
    }

    /**
     * Creates Mister Toony's children's music.
     */
    private void createMisterToonySongs() {
        createSong("Ah! Les crocodiles", "Mister", "Toony", "Ah! Les crocodiles", Genre.POP, 150);
    }

    /**
     * Creates Monde des Titounis educational content.
     */
    private void createTitounisSongs() {
        createSong("Comptines, Vol. 2", "Monde des", "Titounis", "Comptines, Vol. 2", Genre.CLASSICAL, 300);
    }

    /**
     * Helper method to create a song using the MusicFactory.
     * This wraps the createCompleteSong method and handles error logging.
     */
    private void createSong(String title, String artistFirst, String artistLast,
                            String albumTitle, Genre genre, int duration) {
        try {
            Song song = musicFactory.createCompleteSong(
                    title, artistFirst, artistLast, albumTitle, genre, duration
            );
            createdSongs.add(song);
            System.out.printf("✓ Created: %s by %s %s (%s)\n",
                    title, artistFirst, artistLast, albumTitle);
        } catch (Exception e) {
            System.err.printf("✗ Failed to create song: %s - %s\n", title, e.getMessage());
        }
    }

    /**
     * Displays a comprehensive summary of the seeding operation.
     * Shows statistics about what was created and any potential issues.
     */
    private void displaySeedingSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DATABASE SEEDING SUMMARY");
        System.out.println("=".repeat(60));

        // Count songs by genre
        System.out.println("Songs created by genre:");
        for (Genre genre : Genre.values()) {
            long count = createdSongs.stream()
                    .filter(song -> song.getGenre() == genre)
                    .count();
            if (count > 0) {
                System.out.printf("  %s: %d songs\n", genre, count);
            }
        }

        System.out.println("\nTotal duration: " + calculateTotalDuration() + " minutes");
        System.out.println("Average song duration: " + calculateAverageDuration() + " seconds");

        System.out.println("\nSeeding completed successfully! Your music database is now populated.");
        System.out.println("You can start exploring the music collection using the Spotify client.");
    }

    /**
     * Calculates the total duration of all created songs in minutes.
     */
    private int calculateTotalDuration() {
        return createdSongs.stream()
                .mapToInt(song -> song.getDurationSeconds())
                .sum() / 60;
    }

    /**
     * Calculates the average duration of songs in seconds.
     */
    private int calculateAverageDuration() {
        return (int) createdSongs.stream()
                .mapToInt(song -> song.getDurationSeconds())
                .average()
                .orElse(0.0);
    }

    /**
     * Main method to run the seeding process standalone.
     * This can be called independently to populate the database.
     */
    public static void main(String[] args) {
        System.out.println("Starting Music Database Seeding...");
        DatabaseSeeder seeder = new DatabaseSeeder();
        seeder.seedDatabase();
    }
}