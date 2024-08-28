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

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.util.docker.DockerClientProvider;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;

/**
 * Builder class for creating {@link ApplicationPublisher}
 *
 */
public class ApplicationPublisherBuilder {

    private MetaDataServiceIfc metaDataServiceManager;
    private PBASchemaTool pbaSchemaTool;
    private SdkDockerService dockerService;
    private GitSshService gitSSHService;

    /**
     * This method will set the {@link MetaDataServiceIfc} reference.
     *
     * @param metaDataServiceManager
     *            {@link MetaDataServiceIfc} reference.
     * @return current {@link ApplicationPublisherBuilder} reference
     */
    public ApplicationPublisherBuilder metaDataServiceManager(final MetaDataServiceIfc metaDataServiceManager) {
        this.metaDataServiceManager = metaDataServiceManager;
        return this;
    }

    /**
     * This method will set the {@link PBASchemaTool} reference.
     *
     * @param pbaSchemaTool
     *            {@link PBASchemaTool} reference.
     * @return current {@link ApplicationPublisherBuilder} reference
     */
    public ApplicationPublisherBuilder pbaSchemaTool(final PBASchemaTool pbaSchemaTool) {
        this.pbaSchemaTool = pbaSchemaTool;
        return this;
    }

    /**
     * This method will set the {@link DockerClientProvider} reference.
     *
     * @param dockerService
     *            {@link SdkDockerService} reference.
     * @return current {@link ApplicationPublisherBuilder} reference
     */
    public ApplicationPublisherBuilder dockerRepoClient(final SdkDockerService dockerService) {
        this.dockerService = dockerService;
        return this;
    }

    /**
     * This method will set the {@link GitReadOnlySSHRepositoryService} reference.
     *
     * @param gitReadOnlySSHRepositoryService
     *            {@link GitReadOnlySSHRepositoryService} reference.
     * @return current {@link ApplicationPublisherBuilder} reference
     */
    public ApplicationPublisherBuilder gitSshService(final GitSshService gitReadOnlySSHRepositoryService) {
        this.gitSSHService = gitReadOnlySSHRepositoryService;
        return this;
    }

    /**
     * This method will set the {@link GitReadOnlySSHRepositoryService} reference.
     *
     * @return new {@link ApplicationPublisherBuilder} reference
     */
    public static ApplicationPublisherBuilder builder() {
        return new ApplicationPublisherBuilder();
    }

    /**
     * This method will create new instance of {@link ApplicationPublisher}.
     *
     * @return new instance of {@link ApplicationPublisher}.
     */
    public ApplicationPublisher build() {
        if (metaDataServiceManager == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_REGISTERING_SERVICES, "MetaDataServiceIfc is null");
        }
        if (pbaSchemaTool == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_REGISTERING_SERVICES, "PBASchemaTool is null");
        }
        if (dockerService == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_REGISTERING_SERVICES, "SdkDockerService is null");
        }
        if (gitSSHService == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_REGISTERING_SERVICES, "GitSSHService is null");
        }
        return new ApplicationPublisher(metaDataServiceManager, pbaSchemaTool, dockerService, gitSSHService);
    }
}
