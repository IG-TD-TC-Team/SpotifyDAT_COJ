package persistence;


import songsOrganisation.Library;

import java.util.Optional;

/**
 * A repository for persisting and retrieving Library objects.
 * Extends the generic JsonRepository to provide Library-specific functionalities.
 */
public class LibraryRepository extends JsonRepository<Library> {

    /**
     * Constructor that initializes the repository.
     * This constructor calls the superclass constructor with the Library class type and the JSON file name.
     */
    public LibraryRepository() {
        super(Library.class, "Library.json");
    }

    /**
     * Finds a library by its UserID.
     *
     * @param userId The ID of the library to find.
     * @return An Optional containing the library if found, or empty if not found.
     */
    public Optional<Library> findById(int userId) {
        return findAll().stream()
                .filter(library -> library.getUserID() == userId)
                .findFirst();
    }



}
