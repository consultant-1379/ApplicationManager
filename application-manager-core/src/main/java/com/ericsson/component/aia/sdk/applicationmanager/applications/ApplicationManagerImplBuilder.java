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
package com.ericsson.component.aia.sdk.applicationmanager.applications;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;

/**
 * Builder class for creating {@link ApplicationManagerImpl}
 *
 */
public class ApplicationManagerImplBuilder {

    private TemplateManager templateManager;
    private MetaDataServiceIfc metaDataServiceManager;
    private PBASchemaTool pbaSchemaTool;
    private SdkDockerService dockerService;
    private GitSshService gitSSHService;

    /**
     * This method will set the {@link TemplateManager} reference.
     *
     * @param templateManager
     *            {@link TemplateManager} reference.
     * @return current {@link ApplicationManagerImplBuilder} reference
     */
    public ApplicationManagerImplBuilder templateManager(final TemplateManager templateManager) {
        this.templateManager = templateManager;
        return this;
    }

    /**
     * This method will set the {@link MetaDataServiceIfc} reference.
     *
     * @param metaDataServiceManager
     *            {@link MetaDataServiceIfc} reference.
     * @return current {@link ApplicationManagerImplBuilder} reference
     */
    public ApplicationManagerImplBuilder metaDataServiceManager(final MetaDataServiceIfc metaDataServiceManager) {
        this.metaDataServiceManager = metaDataServiceManager;
        return this;
    }

    /**
     * This method will set the {@link PBASchemaTool} reference.
     *
     * @param pbaSchemaTool
     *            {@link PBASchemaTool} reference.
     * @return current {@link ApplicationManagerImplBuilder} reference
     */
    public ApplicationManagerImplBuilder pbaSchemaTool(final PBASchemaTool pbaSchemaTool) {
        this.pbaSchemaTool = pbaSchemaTool;
        return this;
    }

    /**
     * This method will set the {@link SdkDockerService} reference.
     *
     * @param dockerService
     *            {@link SdkDockerService} reference.
     * @return current {@link ApplicationManagerImplBuilder} reference
     */
    public ApplicationManagerImplBuilder sdkDockerService(final SdkDockerService dockerService) {
        this.dockerService = dockerService;
        return this;
    }

    /**
     * This method will set the {@link GitSshService} reference.
     *
     * @param gitSSHService
     *            {@link GitSshService} reference.
     * @return current {@link ApplicationManagerImplBuilder} reference
     */
    public ApplicationManagerImplBuilder gitSSHService(final GitSshService gitSSHService) {
        this.gitSSHService = gitSSHService;
        return this;
    }

    /**
     * This method will create new instance of {@link ApplicationManagerImpl}.
     *
     * @return new instance of {@link ApplicationManagerImpl}.
     */
    public ApplicationManagerImpl build() {

        if (templateManager == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_REGISTERING_SERVICES, "TemplateManager is null");
        }
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
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_REGISTERING_SERVICES,
                    "GitReadOnlySSHRepositoryService is null");
        }
        return new ApplicationManagerImpl(templateManager, metaDataServiceManager, pbaSchemaTool, dockerService, gitSSHService);
    }
}
