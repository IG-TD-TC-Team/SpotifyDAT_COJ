package songsAndArtists;

import java.util.Date;
import java.util.List;

/**
 * The Artist class represents an artist with their details.
 * It includes the artist's ID, first name, last name, birth date, country of birth,
 * and a list of song IDs associated with the artist.
 */

public class Artist {
    public Artist() {}
    public Artist(int artistID, String firstName, String lastName, Date birthday, String countryOfBirth) {
        this.artistID = artistID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthday;
        this.countryOfBirth = countryOfBirth;
    }

    private int artistID;
    private String firstName;
    private String lastName;
    private Date birthDate;
    private String countryOfBirth;
    private List<Integer> songsIDs;

    public int getArtistID() {return artistID; }
    public void setArtistID(int artistID) {
        this.artistID = artistID;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public Date getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
    public String getCountryOfBirth() {
        return countryOfBirth;
    }
    public void setCountryOfBirth(String countryOfBirth) {
        this.countryOfBirth = countryOfBirth;
    }
    public List<Integer> getSongs() {
        return songsIDs;
    }
    public void setSongs(List<Integer> songsIDs) {
        this.songsIDs = songsIDs;
    }

}
