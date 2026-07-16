/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
 * 
 * TODO: Move to fortify-ssc-parser-util-common after PoC validation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.ssc.parser.sarif.cache;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortify.util.io.Region;
import com.fortify.util.io.RegionInputStream;

/**
 * Hybrid caching wrapper: keeps objects in memory via SoftReference,
 * re-parses from Region on GC.
 * 
 * **Single-Pass Parsing:**
 * The static factory method `parse()` captures object region and deserializes
 * in a single JSON parsing pass by tracking byte positions with Jackson's
 * `getCurrentLocation().getByteOffset()`.
 * 
 * **Lazy Reload:**
 * When JVM garbage-collects the SoftReference under memory pressure,
 * `getOrReload()` automatically re-parses from the Region.
 * 
 * **No New Dependencies:**
 * Uses only Jackson (already in use) and java.lang.ref (JDK).
 * Eliminates MapDB + Eclipse Collections + Guava (45MB of bloat).
 * 
 * Inspired by SARIF .NET SDK's DeferredDictionary/DeferredList.
 * 
 * @param <T> Type of cached object
 */
public class CachedObject<T> {
    private static final Logger LOG = LoggerFactory.getLogger(CachedObject.class);

    private final Region region;
    private final InputStream sourceInputStream;
    private final ObjectMapper objectMapper;
    private final Class<T> objectClass;
    private SoftReference<T> cachedObjectRef;

    /**
     * Construct a cached object wrapper.
     * 
     * @param object            Deserialized object (wrapped in SoftReference)
     * @param region            Byte range in source file for this object
     * @param sourceInputStream Source file stream (must stay open for entire parse
     *                          session)
     * @param objectMapper      ObjectMapper for re-parsing on GC
     * @param objectClass       Object class (for re-deserialization)
     */
    public CachedObject(T object, Region region, InputStream sourceInputStream,
            ObjectMapper objectMapper, Class<T> objectClass) {
        this.region = region;
        this.sourceInputStream = sourceInputStream;
        this.objectMapper = objectMapper;
        this.objectClass = objectClass;
        this.cachedObjectRef = new SoftReference<>(object);
    }

    /**
     * PRIMARY FACTORY METHOD: Parse + wrap in single pass.
     * 
     * **Single-pass logic:**
     * 1. Record start byte position
     * 2. Deserialize with readValueAs() (entire object in one pass)
     * 3. Record end byte position
     * 4. Create Region from positions
     * 5. Return wrapped CachedObject
     * 
     * **Usage in handlers:**
     * ```java
     * CachedObject<Artifact> cached = CachedObject.parse(
     * jp, Artifact.class, sourceInputStream, objectMapper
     * );
     * artifacts.add(cached);
     * ```
     * 
     * @param <T>               Object type to parse
     * @param jp                JsonParser positioned at START_OBJECT
     * @param type              Class to deserialize to
     * @param sourceInputStream Source stream (for fallback re-parsing)
     * @param objectMapper      Jackson ObjectMapper
     * @return Wrapped CachedObject with both parsed object and Region
     * @throws IOException on parse failure
     */
    public static <T> CachedObject<T> parse(JsonParser jp, Class<T> type,
            InputStream sourceInputStream, ObjectMapper objectMapper) throws IOException {
        // Phase 1: Capture byte position BEFORE parsing
        long startPosition = jp.getCurrentLocation().getByteOffset();

        // Phase 2: Parse object (single deserialize pass)
        T object = jp.readValueAs(type);

        // Phase 3: Capture byte position AFTER parsing
        long endPosition = jp.getCurrentLocation().getByteOffset();

        // Phase 4: Create Region
        Region region = new Region(startPosition, endPosition);

        // Phase 5: Wrap and return
        return new CachedObject<>(object, region, sourceInputStream, objectMapper, type);
    }

    /**
     * Get object (from memory or re-parse on GC).
     * 
     * **Fast path (~1 microsecond):** Object still in memory → return immediately.
     * **Slow path (rare):** Object garbage-collected → re-parse from Region.
     * 
     * This method is transparent - callers always get the object.
     * 
     * @return The deserialized object
     * @throws IOException if re-parsing fails
     */
    public T getOrReload() throws IOException {
        // Fast path: Is object in memory?
        if (cachedObjectRef != null) {
            T obj = cachedObjectRef.get();
            if (obj != null) {
                return obj; // Return immediately
            }
        }

        // Slow path: Object was GC'd, re-parse from Region
        LOG.trace("Object garbage-collected; re-parsing from Region {}", region);
        T obj = reloadFromRegion();
        cachedObjectRef = new SoftReference<>(obj);
        return obj;
    }

    /**
     * Re-parse object from its Region.
     * 
     * Uses RegionInputStream to read only the byte range [start, end],
     * then ObjectMapper to deserialize.
     * 
     * @return Re-deserialized object
     * @throws IOException on parse failure
     */
    private T reloadFromRegion() throws IOException {
        try (InputStream regionStream = new RegionInputStream(sourceInputStream, region, false)) {
            return objectMapper.readValue(regionStream, objectClass);
        }
    }

    // ============= Accessors for monitoring/testing =============

    public Region getRegion() {
        return region;
    }

    public boolean isInMemory() {
        return cachedObjectRef != null && cachedObjectRef.get() != null;
    }

    public String getCacheStatus() {
        boolean inMemory = isInMemory();
        return String.format("CachedObject[region=%d-%d, inMemory=%b]",
                region.getStart(), region.getEnd(), inMemory);
    }
}