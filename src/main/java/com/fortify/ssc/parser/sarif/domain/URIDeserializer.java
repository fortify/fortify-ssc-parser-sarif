package com.fortify.ssc.parser.sarif.domain;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Custom deserializer for URI fields that handles both proper URIs and file
 * paths.
 */
public class URIDeserializer extends JsonDeserializer<URI> {

    @Override
    public URI deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            return handleAsFilePath(value);
        }
    }

    private URI handleAsFilePath(String value) {
        try {
            Path path = Paths.get(value);
            return path.toUri();
        } catch (Exception e) {
            return createEncodedURI(value);
        }
    }

    /**
     * Creates a URI by manually encoding special characters.
     * This is a fallback for cases where the value cannot be parsed as a Path.
     */
    private URI createEncodedURI(String value) {
        try {
            // Replace spaces and other problematic characters
            String encoded = value.replace(" ", "%20")
                    .replace("[", "%5B")
                    .replace("]", "%5D")
                    .replace("{", "%7B")
                    .replace("}", "%7D");

            // Try to create URI with the encoded string
            return new URI(encoded);
        } catch (URISyntaxException e) {
            // Last resort: create a file:// URI with the encoded path
            try {
                return new URI("file", null, "/" + value.replace(" ", "%20"), null);
            } catch (URISyntaxException ex) {
                // If everything fails, return null or throw
                // Returning null allows processing to continue
                return null;
            }
        }
    }
}
