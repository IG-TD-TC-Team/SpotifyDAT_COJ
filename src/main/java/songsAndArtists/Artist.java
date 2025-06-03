package songsAndArtists;

import java.util.Date;
import java.util.List;

/**
 * The Artist class represents an artist with their details.
 * It includes the artist's ID, first name, last name, birth date, country of birth,
 * and a list of song IDs associated with the artist.
 */

public class Artist {
    /**
     * Default no-argument constructor.
     * Required for Jackson JSON deserialization.
     */
    public Artist() {}

    /**
     * Constructs a fully initialized Artist with all required fields.
     *
     * @param artistID The unique identifier for the artist
     * @param firstName The artist's first name
     * @param lastName The artist's last name
     * @param birthday The artist's date of birth
     * @param countryOfBirth The artist's country of origin
     */
    public Artist(int artistID, String firstName, String lastName, Date birthday, String countryOfBirth) {
        this.artistID = artistID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthday;
        this.countryOfBirth = countryOfBirth;
    }

    /**
     * The unique identifier for this artist in the system.
     */
    private int artistID;

    /**
     * The artist's first name or given name.
     */
    private String firstName;

    /**
     * The artist's last name or family name.
     */
    private String lastName;

    /**
     * The artist's date of birth.
     */
    private Date birthDate;

    /**
     * The country where the artist was born.
     */
    private String countryOfBirth;

    /**
     * List of song IDs that this artist has created.
     * Represents the one-to-many relationship between artists and songs.
     */
    private List<Integer> songsIDs;

    /**
     * Gets the unique identifier of this artist.
     *
     * @return The artist's unique ID
     */
    public int getArtistID() {
        return artistID;
    }

    /**
     * Sets the unique identifier of this artist.
     *
     * @param artistID The artist's unique ID to set
     */
    public void setArtistID(int artistID) {
        this.artistID = artistID;
    }

    /**
     * Gets the artist's first name.
     *
     * @return The artist's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the artist's first name.
     *
     * @param firstName The first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the artist's last name.
     *
     * @return The artist's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the artist's last name.
     *
     * @param lastName The last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the artist's date of birth.
     *
     * @return The artist's birth date
     */
    public Date getBirthDate() {
        return birthDate;
    }

    /**
     * Sets the artist's date of birth.
     *
     * @param birthDate The birth date to set
     */
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * Gets the artist's country of birth.
     *
     * @return The artist's country of birth
     */
    public String getCountryOfBirth() {
        return countryOfBirth;
    }

    /**
     * Sets the artist's country of birth.
     *
     * @param countryOfBirth The country of birth to set
     */
    public void setCountryOfBirth(String countryOfBirth) {
        this.countryOfBirth = countryOfBirth;
    }

    /**
     * Gets the list of song IDs associated with this artist.
     *
     * <p>These IDs reference the songs that were created by this artist.
     * The actual song objects must be fetched from a song repository using these IDs.</p>
     *
     * @return A list of song IDs created by this artist, or null if no songs are associated
     */
    public List<Integer> getSongs() {
        return songsIDs;
    }

    /**
     * Sets the list of song IDs associated with this artist.
     *
     * <p>This method replaces the entire list of song associations.
     * To add or remove individual songs, the list should be modified and then set using this method.</p>
     *
     * @param songsIDs The list of song IDs to associate with this artist
     */
    public void setSongs(List<Integer> songsIDs) {
        this.songsIDs = songsIDs;
    }
}

