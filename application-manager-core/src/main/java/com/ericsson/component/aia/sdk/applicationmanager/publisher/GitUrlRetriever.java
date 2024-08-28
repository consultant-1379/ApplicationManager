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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.git.project.service.GitProjectService;
import com.ericsson.component.aia.sdk.git.project.service.GitRepoInfo;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;

/**
 * This class retrieves the URL to use of for the application income.
 */
public class GitUrlRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitUrlRetriever.class);
    private final GitProjectService gitProjectService;
    private final MetaDataServiceIfc metaDataServiceManager;
    private final PBASchemaTool pbaSchemaTool;

    /**
     * Instantiates a new git url retriever.
     *
     * @param pbaSchemaTool
     *            the pba schema tool
     * @param metaDataServiceManager
     *            the meta data service manager
     * @param gitProjectService
     *            the git project service
     */
    GitUrlRetriever(final PBASchemaTool pbaSchemaTool, final MetaDataServiceIfc metaDataServiceManager, final GitProjectService gitProjectService) {
        this.pbaSchemaTool = pbaSchemaTool;
        this.metaDataServiceManager = metaDataServiceManager;
        this.gitProjectService = gitProjectService;
    }

    /**
     * Gets the git repo url.
     *
     * @param pba
     *            the pba
     * @param publishOperation
     *            the publish operation
     * @return the git repo url
     */
    public String getGitRepoUrl(final Pba pba, final PublishOperationResult publishOperation) {
        final Optional<String> parentScmUrl = getParentIfAvailable(pba);
        if (parentScmUrl.isPresent()) {
            return parentScmUrl.get();
        }
        return createNewGitRepoIfRequired(pba, publishOperation);
    }

    private Optional<String> getParentIfAvailable(final Pba pba) {
        final String scmUrl;

        final Optional<String> parentId = Optional.ofNullable(pba.getApplicationInfo().getParentId());
        if (parentId.isPresent()) {
            final GitRepoInfo parentGitRepo = getParentRepo(parentId.get());
            scmUrl = parentGitRepo.getSshRepoUrl();
            pba.getScmInfo().setScm(scmUrl);
            return Optional.of(scmUrl);
        }

        return Optional.empty();
    }

    private String createNewGitRepoIfRequired(final Pba pba, final PublishOperationResult publishOperation) {
        final String scmUrl;
        final PbaInfo applicationInfo = pba.getApplicationInfo();
        final String applicationName = applicationInfo.getName();
        final Optional<GitRepoInfo> existingScmUrl = gitProjectService.getExistingGitRepository(applicationName);

        if (existingScmUrl.isPresent()) {
            LOGGER.info("Application git repository already exists for application named:: {}", applicationName);
            scmUrl = existingScmUrl.get().getSshRepoUrl();

        } else {
            LOGGER.info("Application git repository doesn't exist for application named:: {}, creating new git repository", applicationName);
            scmUrl = gitProjectService.createGitRepository(applicationName, applicationInfo.getDescription()).getSshRepoUrl();
            publishOperation.createdGitRepoUrl(scmUrl);
        }

        pba.getScmInfo().setScm(scmUrl);
        return scmUrl;
    }

    /**
     * This method will recursively search through an applications parents until it finds the root application (application without a parentId) it
     * will then return that applications Git repository.
     *
     * @param applicationId
     *            The Id of the application to finds parent.
     * @return {@link GitRepoInfo} the Git repository info for the root application.
     */
    private GitRepoInfo getParentRepo(final String applicationId) {
        final Pba pba;

        try {
            pba = pbaSchemaTool.getPBAModelInstance(metaDataServiceManager.get(applicationCatalogName, applicationId)).getPba();
            final Optional<String> parentId = Optional.ofNullable(pba.getApplicationInfo().getParentId());
            if (parentId.isPresent()) {
                return getParentRepo(parentId.get());
            }
        } catch (final MetaDataServiceException e) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PARENT_APPLICATION_NOT_FOUND, "Error extending application");
        }

        final Optional<GitRepoInfo> existingScmUrl = gitProjectService.getExistingGitRepository(pba.getApplicationInfo().getName());
        if (!existingScmUrl.isPresent()) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PARENT_APPLICATION_NOT_FOUND, "Error extending application");
        }

        return existingScmUrl.get();
    }

}
