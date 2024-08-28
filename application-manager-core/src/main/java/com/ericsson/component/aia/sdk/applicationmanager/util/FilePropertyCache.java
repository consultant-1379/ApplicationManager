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
package com.ericsson.component.aia.sdk.applicationmanager.util;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.sdk.applicationmanager.applications.generator.ApplicationFilePropertyCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * FilePropertyCache is generic class used to cache properties of files
 */
public class FilePropertyCache {

    private static final Logger Log = LoggerFactory.getLogger(FilePropertyCache.class);

    private final LoadingCache<String, Map<String, List<String>>> cache;

    /**
     * FilePropertyCache constructor
     *
     * @param zipFilePath
     *            zipFilePath
     * @param expiryTime
     *            expiryTime
     */
    public FilePropertyCache(final Path zipFilePath, final int expiryTime) {

        final ApplicationFilePropertyCache appPropertyCache = new ApplicationFilePropertyCache();

        CacheLoader<String, Map<String, List<String>>> loader;

        loader = new CacheLoader<String, Map<String, List<String>>>() {

            @Override
            public Map<String, List<String>> load(final String pbaId) {

                final String currentPath = Paths.get("").toAbsolutePath().toString();

                final File zipFile = zipFilePath.isAbsolute() ? zipFilePath.toFile() : new File(currentPath + File.separator + zipFilePath);
                final Path zipPath = zipFile.toPath();

                try (FileSystem zipfs = FileSystems.newFileSystem(zipPath, this.getClass().getClassLoader())) {
                    final Iterable<Path> iterable = zipfs.getRootDirectories();

                    for (final Path path : iterable) {
                        Files.walkFileTree(path, appPropertyCache);
                    }

                } catch (final Exception e) {
                    e.printStackTrace();
                }

                return appPropertyCache.getPropertyMap();
            }
        };

        cache = CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(expiryTime, TimeUnit.MINUTES).build(loader);

    }

    /**
     * get method retrieves cached properties based on passed cache id
     *
     * @param identifier
     *            identifier
     * @return map of filenames and their properties
     * @throws ExecutionException
     *             ExecutionException
     */
    public Map<String, List<String>> get(final String identifier) throws ExecutionException {
        return cache.get(identifier);
    }

    /**
     * clearCache method used to invalidate cache
     */
    public void clearCache() {
        Log.info("Clearing the contents of FilePropertyCache cache");
        cache.invalidateAll();
    }

}
