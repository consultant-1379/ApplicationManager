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

import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.CHAR_HYPEN;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.applicationmanager.model.Maas;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.model.ServiceInfo;

/**
 * This util class has common methods used by application manager classes.
 */
public class AppSdkUtil {

    public static final String APPLICATION_TAG = "application";
    public static final String MAAS_TAG = "maas";
    public static final String SERVICE_TAG = "service";
    public static final String TAG_SEPARATOR = ":";

    /**
     * Instantiates a new app sdk util.
     */
    private AppSdkUtil() {

    }

    /**
     * Creates the folder if not exists.
     *
     * @param folderToCreateIfNotExists
     *            the folder to create if not exists
     * @return the path
     */
    public static Path createFolderIfNotExists(final String folderToCreateIfNotExists) {
        try {
            final Path folderPathToCheck = Paths.get(ApplicationManagerConfiguration.storageLocation).resolve(folderToCreateIfNotExists);
            if (!folderPathToCheck.toFile().exists()) {
                Files.createDirectories(folderPathToCheck);
            }
            return folderPathToCheck;
        } catch (final IOException e) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_ACCESSING_FILE_SYSTEM,
                    "exception occurred, when trying to create folder:" + folderToCreateIfNotExists, e);
        }
    }

    /**
     * Creates application ID based on PBA information.
     *
     * @param pba
     *            - pba instance to be used.
     * @return the created ID
     */
    public static String createApplicationId(final Pba pba) {

        return new StringBuilder(APPLICATION_TAG).append(TAG_SEPARATOR).append(pba.getAuthorInfo().getAuthor().toLowerCase().replaceAll("\\s+", ""))
                .append(TAG_SEPARATOR).append(pba.getApplicationInfo().getName().toLowerCase()).append(TAG_SEPARATOR)
                .append(pba.getApplicationInfo().getVersion().toLowerCase()).toString();
    }

    /**
     * Creates maas application ID based on MAASInstance information.
     *
     * @param maas
     *            - maas instance to be used.
     * @return the created ID
     */
    public static String createMaasApplicationId(final Maas maas) {

        return new StringBuilder(APPLICATION_TAG).append(TAG_SEPARATOR).append(MAAS_TAG).append(TAG_SEPARATOR)
                .append(maas.getUiDef().getParserDef().getName().toLowerCase()).append(TAG_SEPARATOR).append(maas.getVersion().toLowerCase())
                .toString();
    }

    /**
     * Creates service ID.
     *
     * @param serviceInfo
     *            - service to be used for creation.
     * @return the created ID.
     */
    public static String createServiceId(final ServiceInfo serviceInfo) {

        return new StringBuilder(SERVICE_TAG).append(TAG_SEPARATOR).append(serviceInfo.getServiceType().toLowerCase()).append(TAG_SEPARATOR)
                .append(serviceInfo.getTechnology().toLowerCase()).append(TAG_SEPARATOR).append(serviceInfo.getVersion().toLowerCase()).toString();
    }

    /**
     * This method will delete and recreate a path.
     *
     * @param path
     *            the path
     */
    public static void recreatePath(final Path path) {
        try {
            if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                FileUtils.deleteQuietly(path.toFile());
            }
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                Files.createDirectories(path);
            }
        } catch (final IOException e) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_ACCESSING_FILE_SYSTEM,
                    "exception occurred, when trying to deleting folder:" + path, e);
        }
    }

    /**
     * This method will convert a standard GIT repository URL to the repository name.
     *
     * @param scmUrl
     *            the GIT URL
     * @return the repository name from
     */
    public static String getRepoNameFrom(final String scmUrl) {
        final String[] splitUrl = scmUrl.split("/");
        final String nameSegment = splitUrl[splitUrl.length - 1];
        final String repoName = nameSegment.replace(".git", "");
        return repoName;
    }

    /**
     * Find pba file within a path.
     *
     * @param root
     *            The root path to search for the pba in.
     * @param fileName
     *            the file name
     * @return {@link Optional } The path of the pba if it is found.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Optional<Path> findPba(final Path root, final String fileName) throws IOException {
        return findFileInPath(fileName, root);
    }

    /**
     * Find pba file within a path.
     *
     * @param root
     *            The root path to search for the pba in.
     * @return {@link Optional } The path of the pba if it is found.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Optional<Path> findPba(final Path root) throws IOException {
        return findFileInPath("pba.json", root);
    }

    /**
     * Check the application zip file for the presence of a README.md, if this file is present it will replace the existing README.md within the
     * applications Git repository with the new README.md.
     *
     * @param applicationPath
     *            The path to the application zip file.
     * @param gitRepo
     *            The path to the applications Git Repository.
     */
    public static void updateApplicationReadMe(final Path applicationPath, final Path gitRepo) {
        try (final FileSystem zipfs = FileSystems.newFileSystem(applicationPath, AppSdkUtil.class.getClassLoader())) {

            Optional<Path> readMe = Optional.empty();
            for (final Path root : zipfs.getRootDirectories()) {
                readMe = findFileInPath("README.md", root);
                if (readMe.isPresent()) {
                    Files.copy(readMe.get(), gitRepo.resolve("README.md"), StandardCopyOption.REPLACE_EXISTING);
                    break;
                }
            }

        } catch (final IOException exp) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.APPLICATION_FILE_IS_CORRUPTED,
                    "Exception occurred, when trying to extract README.md file from application being published", exp);
        }
    }

    /**
     * Find file in path.
     *
     * @param fileName
     *            the file name
     * @param root
     *            the root
     * @return the optional
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Optional<Path> findFileInPath(final String fileName, final Path root) throws IOException {
        try (final Stream<Path> repoStream = Files.walk(root);) {
            return repoStream.filter(path -> (path.getFileName() != null && fileName.equalsIgnoreCase(path.getFileName().toString()))).findFirst();
        }
    }

    /**
     * Convert application name by removing all special characters and capitalising the first letter of each word.
     *
     * @param fileName
     *            the file name
     * @return the converted name
     */
    public static String convertApplicationName(final String fileName) {
        return WordUtils.capitalize(fileName, CHAR_HYPEN).replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * Appends suffix to the feature name.
     *
     * @param name
     *            - name of the feature
     * @param suffix
     *            - suffix to be applied
     * @return name + _ + suffix
     */
    public static String appendToFeatureName(final String name, final String suffix) {
        return name + "_" + suffix;
    }

    /**
     * Validates PBA information for applications.
     *
     * @param pbaModel
     *            - pba model instance to be used.
     */
    public static void validatePbaApplication(final PBAInstance pbaModel) {

        if (pbaModel.getPba() == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, "Pba information is null");
        }

        if (pbaModel.getPba().getApplicationInfo() == null || pbaModel.getPba().getApplicationInfo().getName() == null
                || pbaModel.getPba().getApplicationInfo().getVersion() == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, "ApplicationInfo is invalid or incomplete");
        }

        if (pbaModel.getPba().getAuthorInfo() == null || pbaModel.getPba().getAuthorInfo().getAuthor() == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, "AuthorInfo is invalid or incomplete");
        }

    }
}
