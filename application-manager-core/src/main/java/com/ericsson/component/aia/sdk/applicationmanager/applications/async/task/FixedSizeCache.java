/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.applicationmanager.applications.async.task;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to store a fixed number of tasks
 *
 * @param <T>
 *            The type of the entry in the cache
 */
public class FixedSizeCache<T> {
    /** Logger for ApplicationManagerImpl. */
    private static final Logger Log = LoggerFactory.getLogger(FixedSizeCache.class);

    private final Map<String, T> cache;

    /**
     * Instantiate a fixed size cache
     *
     * @param maxNumberOfEntries
     *            The maximum number of entries that are maintained by the cache.
     */
    public FixedSizeCache(final int maxNumberOfEntries) {
        cache = Collections.synchronizedMap(new LinkedHashMap<String, T>(maxNumberOfEntries, 0.7f, true) {
            private static final long serialVersionUID = -6233552267051192931L;

            @Override
            protected boolean removeEldestEntry(final Map.Entry<String, T> eldest) {
                return size() > maxNumberOfEntries;
            }

        });
    }

    /**
     * Adds an async Task to the cache.
     *
     * @param object
     *            the object which should be added to the cache.
     * @return the uniqueId of the inserted path.
     */
    public String add(final T object) {
        final String uniqueId = UUID.randomUUID().toString();
        cache.put(uniqueId, object);
        Log.trace("Adding new cache entry with id::" + uniqueId);
        return uniqueId;
    }

    /**
     * Gets the path of a local zip file from the cache.
     *
     * @param uniqueId
     *            the unique id.
     * @return The entry related to that ID.
     */
    public T get(final String uniqueId) {
        return cache.get(uniqueId);
    }

}
