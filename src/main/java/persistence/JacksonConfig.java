package persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Provides a centralized configuration for Jackson ObjectMapper.
 * This ensures consistent JSON serialization/deserialization behavior
 * throughout the application.
 */
public class JacksonConfig {

    /**
     * Returns a pre-configured ObjectMapper with appropriate settings
     * for handling our domain model classes.
     *
     * @return A configured ObjectMapper instance
     */
    public static ObjectMapper getConfiguredMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Don't fail on unknown properties (helps with fields calculated by methods)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Don't fail on empty beans (helps with interface implementations like SubscriptionPlan)
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return mapper;
    }
}