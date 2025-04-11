package persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A generic repository for persisting lists of objects as JSON.
 *
 * @param <T> The type of object to persist.
 */
public class JsonRepository<T> {
    // The Class type of T, used by Jackson for serialization/deserialization .
    private final Class<T> type;

    // The path where the JSON file is stored.
    private final Path storagePath;

    // Jackson's ObjectMapper for converting objects to/from JSON.
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor that initializes the repository.
     *
     * @param type The class type of the objects this repository handles.
     * @param filename The name of the JSON file to store objects.
     */
    public JsonRepository(Class<T> type, String filename) {
        this.type = type;
        // Store file under the "/data" directory.
        this.storagePath = Paths.get("data", filename);
        try {
            // Ensure the parent directory exists.
            Files.createDirectories(storagePath.getParent());
            // If the file doesn't exist, create it and initialize with an empty JSON array.
            if (!Files.exists(storagePath)) {
                Files.writeString(storagePath, "[]");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the JSON file and returns a list of objects.
     *
     * @return A List of objects of type T.
     */
    public List<T> findAll() {
        try {
            // Read the JSON file as a String.
            String content = Files.readString(storagePath);
            // Use Jackson to convert the JSON string to a List<T>.
            return mapper.readValue(content, mapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (IOException e) {
            e.printStackTrace();
            // Return an empty list if something goes wrong.
            return new ArrayList<>();
        }
    }

    /**
     * Writes a list of objects to the JSON file.
     *
     * @param items The list of objects to save.
     */
    public void saveAll(List<T> items) {
        try {
            // Convert the list of objects to JSON formatted string with pretty printing.
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(items);
            // Write the JSON string to the file.
            Files.writeString(storagePath, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a single object to the repository and persists it.
     * It reads the current file content, adds the item, and saves it back.
     *
     * @param item The object to add.
     */
    public void add(T item) {
        // Load current items.
        List<T> items = findAll();
        // Add new item.
        items.add(item);
        // Save the updated list.
        saveAll(items);
    }
}
