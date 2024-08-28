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
package com.ericsson.component.aia.sdk.applicationmanager.publisher;

import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration.applicationCatalogName;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration.artifactoryServerUrl;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration.gitAccessToken;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration.gitServiceType;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration.gitServiceUrl;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConstants.DOCKER_IMAGE_TAR;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConstants.HYPHEN;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConstants.PBA_APPLICATION_INFO_ID;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.applications.async.task.FixedSizeCache;
import com.ericsson.component.aia.sdk.applicationmanager.applications.async.task.PublishStatus;
import com.ericsson.component.aia.sdk.applicationmanager.applications.async.task.PublishTaskStatus;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConstants;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.applicationmanager.util.AppSdkUtil;
import com.ericsson.component.aia.sdk.applicationmanager.views.TaskStatusView;
import com.ericsson.component.aia.sdk.git.project.service.GitProjectService;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.model.Docker;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.exception.AppSdkException;
import com.ericsson.component.aia.sdk.templatemanager.exception.WrongEnvironmentGitUrlException;
import com.ericsson.component.aia.sdk.templatemanager.util.PbaValidatorUtils;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;

/**
 * The Class ApplicationPublisher.
 */
public class ApplicationPublisher {

    private static final int MAX_NUMBER_OF_PUBLISH_OPERATION = 2;
    private static final int MAX_NUMBER_OF_WAITING_ASYNC_TASKS = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPublisher.class);

    private final MetaDataServiceIfc metaDataServiceManager;
    private final PBASchemaTool pbaSchemaTool;
    private final SdkDockerService dockerService;
    private final GitSshService gitSshService;
    private final Path cachedPublishedApplicationPath;

    private final ExecutorService executor;
    private final FixedSizeCache<PublishTaskStatus> asyncTaskCache = new FixedSizeCache<>(MAX_NUMBER_OF_WAITING_ASYNC_TASKS);
    private final GitProjectService gitProjectService = GitProjectService.newGitProjectRepository(gitServiceType, gitAccessToken, gitServiceUrl);
    private final GitUrlRetriever gitUrlRetriever;
    private final GitPushService gitPushService;
    private final DependencyChecker dependencyChecker;

    /**
     * public TaskStatusView getStatusOfTask(final String asyncTaskId) { return asyncTaskCache.get(asyncTaskId).getTaskStatusView(); }
     *
     * Package private constructor of {@link ApplicationPublisher}.
     *
     * @param metaDataServiceManager
     *            The meta data service manager.
     * @param pbaSchemaTool
     *            The pba schema tool.
     * @param dockerService
     *            The Docker repository service.
     * @param gitSSHService
     *            The GIT repository service.
     */
    ApplicationPublisher(final MetaDataServiceIfc metaDataServiceManager, final PBASchemaTool pbaSchemaTool, final SdkDockerService dockerService,
                         final GitSshService gitSSHService) {

        this.executor = Executors.newFixedThreadPool(MAX_NUMBER_OF_PUBLISH_OPERATION);
        this.metaDataServiceManager = metaDataServiceManager;
        this.pbaSchemaTool = pbaSchemaTool;
        this.dockerService = dockerService;
        this.gitSshService = gitSSHService;

        this.cachedPublishedApplicationPath = AppSdkUtil.createFolderIfNotExists(ApplicationManagerConstants.SDK_PUBLISHED_APPLICATION_CACHE_FOLDER);
        this.dependencyChecker = new DependencyChecker(metaDataServiceManager, pbaSchemaTool, dockerService);
        this.gitUrlRetriever = new GitUrlRetriever(pbaSchemaTool, metaDataServiceManager, gitProjectService);
        this.gitPushService = new GitPushService(pbaSchemaTool, gitSSHService);
    }

    /**
     * Publish the PBA model using metadata service.
     *
     * @param input
     *            An input stream of a zip file containing the docker image, PBA and application code.
     * @param fileName
     *            the zip file containing the docker image, PBA and application code.
     * @param applicationVersion
     *            the application version
     * @return the UUID of the publish task
     */
    public String publishApplication(final InputStream input, final String fileName, final String applicationVersion) {
        LOGGER.info("Starting publish operation for file::{}, version::{}", fileName, applicationVersion);
        final Path applicationPath = copyUploadedFileToServer(input, fileName);
        final PublishTaskStatus publishTaskStatus = new PublishTaskStatus();
        final String asyncTaskId = asyncTaskCache.add(publishTaskStatus);

        asyncProcessPublishApplication(applicationVersion, applicationPath, publishTaskStatus);

        return asyncTaskId;
    }

    private void asyncProcessPublishApplication(final String applicationVersion, final Path applicationPath,
                                                final PublishTaskStatus publishTaskStatus) {
        executor.execute(() -> {
            final PublishOperationResult publishOperation = new PublishOperationResult();
            try {
                publishTaskStatus.setPublishStatus(PublishStatus.STARTED);

                final PBAInstance pbaModel = pbaSchemaTool.getPBAModelInstance(extractPbaFromZip(applicationPath));

                AppSdkUtil.validatePbaApplication(pbaModel);

                final Pba pba = pbaModel.getPba();
                pba.getApplicationInfo().setVersion(applicationVersion);

                LOGGER.info("Verifying the dependencies for application named:: {}", pba.getApplicationInfo().getName());
                dependencyChecker.verifyDependenciesExist(pba);

                final Path newGitRepo = cloneGitRepo(publishOperation, pba);

                checkoutParentApplication(pba, newGitRepo);
                gitSshService.checkoutBranch(newGitRepo, pba.getApplicationInfo().getName());

                updateScmTagInPba(pba);
                removeOldZipFile(newGitRepo);
                AppSdkUtil.updateApplicationReadMe(applicationPath, newGitRepo);

                publishOperation.localGitRepo(newGitRepo);
                publishOperation.publishedDockerImage(publishDockerImageToDockerRepo(applicationPath, publishTaskStatus, pbaModel));
                publishOperation.pbaAddedToMetaStore(addPbaToMetaDataStore(publishTaskStatus, pbaModel));

                publishTaskStatus.setPublishStatus(PublishStatus.PUSHING_SOURCE_TO_REPO);
                gitPushService.updateAndPush(applicationPath, pbaModel, newGitRepo);
                publishTaskStatus.setPublishStatus(PublishStatus.FINISHED);
                LOGGER.info("Publish completed successfully for application named:: {}", pba.getApplicationInfo().getName());

            } catch (final Exception exp) {
                LOGGER.error("Failed to publish application", exp);
                publishTaskStatus.setPublishStatus(PublishStatus.FAILED);
                revertOperation(publishOperation);

            } finally {
                removeLocalGitRepository(publishOperation.getLocalGitRepo());
                removeUploadedApplicationFile(applicationPath);
            }
        });
    }

    private Path cloneGitRepo(final PublishOperationResult publishOperation, final Pba pba) {
        final String scmUrl = gitUrlRetriever.getGitRepoUrl(pba, publishOperation);
        try {
            PbaValidatorUtils.validateScmUrl(scmUrl, ApplicationManagerConfiguration.gitServiceUrl);
        } catch (final WrongEnvironmentGitUrlException e) {
            throw new AppSdkException(ApplicationManagerExceptionCodes.WRONG_ENVIRONMENT_GIT_URL, e.getMessage());
        }
        final Path newGitRepo = gitSshService.clone(scmUrl, pba.getApplicationInfo().getName());
        return newGitRepo;
    }

    private void checkoutParentApplication(final Pba pba, final Path newGitRepo) throws MetaDataServiceException {
        final String parentId = pba.getApplicationInfo().getParentId();
        if (parentId != null) {
            final PBAInstance pbaModel2 = pbaSchemaTool.getPBAModelInstance(metaDataServiceManager.get(applicationCatalogName, parentId));
            gitSshService.checkout(newGitRepo, pbaModel2.getPba().getScmInfo().getScmTag());
        }
    }

    private Docker publishDockerImageToDockerRepo(final Path publishingApplicationPath, final PublishTaskStatus publishTaskStatus,
                                                  final PBAInstance pbaModel) {

        try (final FileSystem zipfs = FileSystems.newFileSystem(publishingApplicationPath, this.getClass().getClassLoader())) {
            final Path dockerImagesPath = zipfs.getPath(DOCKER_IMAGE_TAR);
            publishTaskStatus.setPublishStatus(PublishStatus.PUSHING_DOCKER_IMAGE);
            final Docker dockerInfo = pbaModel.getPba().getBuildInfo().getContainer().getDocker();
            final String applicationVersion = pbaModel.getPba().getApplicationInfo().getVersion();

            if (Files.exists(dockerImagesPath)) {
                dockerService.publishDockerImage(dockerImagesPath, dockerInfo, applicationVersion);
                Files.delete(dockerImagesPath);
            }

            return dockerInfo;

        } catch (final IOException exp) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.APPLICATION_FILE_IS_CORRUPTED, exp);
        }
    }

    private String addPbaToMetaDataStore(final PublishTaskStatus publishTaskStatus, final PBAInstance pbaModel) {
        try {
            publishTaskStatus.setPublishStatus(PublishStatus.UPDATING_META_DATA_STORE);
            final String pbaAsString = pbaSchemaTool.convertToJsonString(pbaModel);
            final String applicationId = AppSdkUtil.createApplicationId(pbaModel.getPba());
            metaDataServiceManager.put(applicationCatalogName, PBA_APPLICATION_INFO_ID, applicationId, pbaAsString);
            pbaModel.getPba().getApplicationInfo().setId(applicationId);
            return applicationId;
        } catch (final MetaDataServiceException exp) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_SAVING_APPLICATION_IN_METADATA_SERVICE, exp);
        }
    }

    /**
     * Get the status of the specified publish operation
     *
     * @param uniqueId
     *            the UUID of the publish task
     * @return the status of the publish task
     */
    public TaskStatusView getStatus(final String uniqueId) {
        final PublishTaskStatus publishTaskStatus = asyncTaskCache.get(uniqueId);
        return publishTaskStatus.getTaskStatusView();
    }

    private void removeUploadedApplicationFile(final Path publishingApplicationPath) {
        try {
            if (publishingApplicationPath.toFile().exists()) {
                Files.delete(publishingApplicationPath);
            }
        } catch (final IOException exp) {
            LOGGER.error("Failed to clean up after Publish operation", exp);
        }
    }

    private void removeLocalGitRepository(final Optional<Path> newGitRepo) {
        if (newGitRepo.isPresent()) {
            try {
                if (Files.isDirectory(newGitRepo.get())) {
                    Files.walk(newGitRepo.get(), FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
                            .forEach(File::delete);
                } else {
                    Files.delete(newGitRepo.get());
                }
            } catch (final Exception ex) {
                LOGGER.error("Error deleting path {}", newGitRepo.get(), ex);
            }
        }
    }

    private Path copyUploadedFileToServer(final InputStream input, final String fileName) {
        final Path publishingApplicationPath = this.cachedPublishedApplicationPath.resolve(fileName);
        try {
            final long fileSize = Files.copy(input, publishingApplicationPath);
            if (fileSize > ApplicationManagerConfiguration.maxUploadedFileSize) {
                FileUtils.deleteQuietly(publishingApplicationPath.toFile());
                throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ILLEGAL_UPLOADED_FILE_SIZE,
                        "Max uploaded file size reached: " + ApplicationManagerConfiguration.maxUploadedFileSize);

            }
        } catch (final IOException exp) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_COPYING_UPLOADED_FILE_TO_SERVER,
                    "Publish operation failed for when copying uploaded file to server", exp);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (final IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return publishingApplicationPath;
    }

    private String extractPbaFromZip(final Path publishingApplicationPath) {
        try (final FileSystem zipfs = FileSystems.newFileSystem(publishingApplicationPath, this.getClass().getClassLoader())) {
            for (final Path root : zipfs.getRootDirectories()) {
                try (InputStream pbaInputStream = Files.newInputStream(AppSdkUtil.findPba(root).get())) {
                    return IOUtils.toString(pbaInputStream, "UTF-8");
                }
            }
        } catch (final IOException exp) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_EXTRACTING_APPLICATION_DATA,
                    "Exception occurred, when trying to extract data from application being published", exp);
        }
        throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_NOT_FOUND, "Unable to locate PBA within zip file");
    }

    private void revertOperation(final PublishOperationResult publishOperation) {
        try {
            publishOperation.getRemoteScmUrl().ifPresent((scmUrl) -> {
                gitProjectService.deleteGitRepository(AppSdkUtil.getRepoNameFrom(scmUrl));
            });
            publishOperation.getPublishedDockerImage().ifPresent((dockerImage) -> {
                dockerService.deleteDockerImageFromRepo(artifactoryServerUrl, dockerImage.getRepoPath(), dockerImage.getImagePath());
            });
            publishOperation.getPbaId().ifPresent((pbaId) -> {
                try {
                    metaDataServiceManager.delete(applicationCatalogName, pbaId);
                } catch (final MetaDataServiceException exp) {
                    LOGGER.error("Exception occurred, when trying to delete failed application PBA from metastore", exp);
                }
            });

        } catch (final Exception exp) {
            LOGGER.error("Exception occurred, when trying to revert failed application publish operation", exp);
        }
    }

    private void removeOldZipFile(final Path newGitRepo) throws IOException {
        try (Stream<Path> paths = Files.find(newGitRepo, 1, (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".zip"))) {
            paths.forEach((path) -> {
                try {
                    FileUtils.forceDelete(path.toFile());
                } catch (final IOException exp) {
                    LOGGER.error("Failed to remove old application zip file from repository", exp);
                }
            });
        }
    }

    private void updateScmTagInPba(final Pba pba) throws IOException {
        final String applicationName = pba.getApplicationInfo().getName();
        final String applicationVersion = pba.getApplicationInfo().getVersion();
        pba.getScmInfo().setScmTag(applicationName + HYPHEN + applicationVersion);
    }
}
