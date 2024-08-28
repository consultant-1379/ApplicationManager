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
package com.ericsson.component.aia.sdk.applicationmanager.cleanup;

import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration.applicationCatalogName;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration.artifactoryServerUrl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.api.CompletionStatus;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.git.exceptions.SdkGitException;
import com.ericsson.component.aia.sdk.git.project.service.GitProjectService;
import com.ericsson.component.aia.sdk.pba.model.Docker;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.exception.AppSdkException;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;

/**
 * The Class ApplicationCleanup.
 */
public class ApplicationCleanup {

    /** Logger for ApplicationCleanup */
    private static final Logger Log = LoggerFactory.getLogger(ApplicationCleanup.class);

    /** The meta data service manager. */
    private final MetaDataServiceIfc metaDataServiceManager;

    /** The pba schema tool. */
    private final PBASchemaTool pbaSchemaTool;

    /** The Docker repository service. */
    private final SdkDockerService dockerService;

    /**
     * public TaskStatusView getStatusOfTask(final String asyncTaskId) { return asyncTaskCache.get(asyncTaskId).getTaskStatusView(); }
     *
     * /** Package private constructor of {@link ApplicationCleanup}.
     *
     * @param metaDataServiceManager
     *            The meta data service manager.
     * @param pbaSchemaTool
     *            The pba schema tool.
     * @param dockerService
     *            The Docker repository service.
     *
     */
    ApplicationCleanup(final MetaDataServiceIfc metaDataServiceManager, final PBASchemaTool pbaSchemaTool, final SdkDockerService dockerService) {
        this.metaDataServiceManager = metaDataServiceManager;
        this.pbaSchemaTool = pbaSchemaTool;
        this.dockerService = dockerService;
    }

    /**
     * Performs the cleanup for an application
     *
     * @param pbaId
     *            of the application to be cleaned up
     *
     * @return the status of the cleanup
     */
    public CompletionStatus cleanupApplication(final String pbaId) {
        Log.info("Cleanup application method invoked with ID {}", pbaId);

        boolean gitResult = false;
        boolean metastoreResult = false;

        final PBAInstance applicationPbaInstance = getPBAInstance(pbaId);
        final Pba pba = applicationPbaInstance.getPba();
        final String repoName = getRepoName(pba);

        if (StringUtils.isNotEmpty(repoName)) {
            //Delete git repository for this application
            try {
                Log.info("Deleting the Git repository:{}", repoName);
                gitResult = GitProjectService.newGitProjectRepository(ApplicationManagerConfiguration.gitServiceType,
                        ApplicationManagerConfiguration.gitAccessToken, ApplicationManagerConfiguration.gitServiceUrl)
                        .deleteGitRepositoryTag(repoName, pba.getScmInfo().getScmTag());
                Log.info("Delete of Git repository:{} was successful.", repoName);
            } catch (final SdkGitException exp) {
                final String errMsg = String.format("Unable to delete git repo for application with ID %s ", pbaId);
                throw new AppSdkException(ApplicationManagerExceptionCodes.ERROR_DELETING_GIT_REPOSITORY, errMsg, exp);
            }
        }

        //not executing now as the build docker image on deploy is not ready yet
        //final boolean dockerResult = deleteDockerImage(pbaId, applicationPbaInstance);

        // Cleanup entries in the metastore for this application
        try {
            Log.info("Deleting the metastore data for the application.");
            metaDataServiceManager.delete(applicationCatalogName, pbaId);
            Log.info("Delete of metastore entries for pbaId:{} was successful.", pbaId);
            metastoreResult = true;
        } catch (final MetaDataServiceException exp) {
            final String errMsg = String.format("Unable to delete metastore entry for template with ID %s ", pbaId);
            Log.warn(errMsg, exp);
        }

        if (metastoreResult && gitResult) {
            return CompletionStatus.SUCCESS;
        } else if (!metastoreResult && !gitResult) {
            return CompletionStatus.FAILED;
        }

        return CompletionStatus.PARTIAL_SUCCESS;

    }

    private String getRepoName(final Pba pba) {
        final String scmName = pba.getScmInfo().getScm();
        if (StringUtils.isNoneEmpty(scmName)) {

            try {
                return scmName.substring(scmName.lastIndexOf("/") + 1, scmName.indexOf(".git"));
            } catch (final Exception ex) {
                Log.error(ex.getMessage());
            }

        }
        return pba.getApplicationInfo().getName();
    }

    /**
     * @param pbaId
     *            - pba
     * @param applicationPbaInstance
     *            instance
     * @return true for deleted or false otherwise
     */
    protected boolean deleteDockerImage(final String pbaId, final PBAInstance applicationPbaInstance) {
        // Delete the docker image
        boolean dockerResult = false;
        try {
            Log.info("Deleting the docker image for the application.");
            dockerResult = deleteDockerImage(applicationPbaInstance);
            Log.info("Delete of docker image for pbaId:{} was successful.", pbaId);
        } catch (final Exception exp) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_DELETING_DOCKER_IMAGE,
                    "Exception during delete of docker image for pbaId: " + pbaId, exp);
        }
        return dockerResult;
    }

    private boolean deleteDockerImage(final PBAInstance pbaModel) {
        Log.trace("Deleting the docker image for the application.");
        final Docker dockerInfo = pbaModel.getPba().getBuildInfo().getContainer().getDocker();
        //supporting only a fixed repository for now
        return dockerService.deleteDockerImageFromRepo(artifactoryServerUrl, dockerInfo.getRepoPath(), dockerInfo.getImagePath());
    }

    /**
     * @param pbaId
     *            - pba
     * @return - pba instance
     */
    protected PBAInstance getPBAInstance(final String pbaId) {
        Log.trace("Retrieving pbaInstance for application with ID {} ", pbaId);
        try {
            final String pbaAsString = metaDataServiceManager.get(applicationCatalogName, pbaId);
            return pbaSchemaTool.getPBAModelInstance(pbaAsString);
        } catch (final MetaDataServiceException exp) {
            final String errMsg = String.format("Application with ID %s not available in catalog %s", pbaId, applicationCatalogName);
            Log.error(errMsg, exp);
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.APPLICATION_NOT_FOUND, errMsg);
        }
    }
}
