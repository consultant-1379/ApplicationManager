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

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.util.docker.DockerClientProvider;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;

/**
 * Builder class for creating {@link ApplicationCleanup}
 *
 */
public class ApplicationCleanupBuilder {

    private MetaDataServiceIfc metaDataServiceManager;
    private PBASchemaTool pbaSchemaTool;
    private SdkDockerService dockerService;

    /**
     * This method will set the {@link MetaDataServiceIfc} reference.
     *
     * @param metaDataServiceManager
     *            {@link MetaDataServiceIfc} reference.
     * @return current {@link ApplicationCleanupBuilder} reference
     */
    public ApplicationCleanupBuilder metaDataServiceManager(final MetaDataServiceIfc metaDataServiceManager) {
        this.metaDataServiceManager = metaDataServiceManager;
        return this;
    }

    /**
     * This method will set the {@link PBASchemaTool} reference.
     *
     * @param pbaSchemaTool
     *            {@link PBASchemaTool} reference.
     * @return current {@link ApplicationCleanupBuilder} reference
     */
    public ApplicationCleanupBuilder pbaSchemaTool(final PBASchemaTool pbaSchemaTool) {
        this.pbaSchemaTool = pbaSchemaTool;
        return this;
    }

    /**
     * This method will set the {@link DockerClientProvider} reference.
     *
     * @param dockerService
     *            {@link SdkDockerService} reference.
     * @return current {@link ApplicationCleanupBuilder} reference
     */
    public ApplicationCleanupBuilder sdkDockerService(final SdkDockerService dockerService) {
        this.dockerService = dockerService;
        return this;
    }

    /**
     * This method will return the builder object.
     *
     * @return new {@link ApplicationCleanupBuilder} reference
     */
    public static ApplicationCleanupBuilder builder() {
        return new ApplicationCleanupBuilder();
    }

    /**
     * This method will create new instance of {@link ApplicationCleanup}.
     *
     * @return new instance of {@link ApplicationCleanup}.
     */
    public ApplicationCleanup build() {
        if (metaDataServiceManager == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_REGISTERING_SERVICES, "MetaDataServiceIfc is null");
        }
        if (pbaSchemaTool == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_REGISTERING_SERVICES, "PBASchemaTool is null");
        }
        if (dockerService == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_REGISTERING_SERVICES, "SdkDockerService is null");
        }
        return new ApplicationCleanup(metaDataServiceManager, pbaSchemaTool, dockerService);
    }
}
