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
package com.ericsson.component.aia.sdk.applicationmanager.services.configuration;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.aia.metadata.lifecycle.MetaDataServiceLifecycleManagerIfc;
import com.ericsson.aia.metadata.lifecycle.impl.MetaDataServiceLifecycleManagerImpl;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerServiceBuilder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class contains the basic beans that make up the application manager
 */
@Component
@Profile(value = { "container", "local", "production" })
public class ApplicationManagerBeans {

    @Autowired
    protected ApplicationManagerServiceConfig applicationManagerServiceConfig;

    @Autowired
    protected MetaStoreConfig metaStoreConfig;

    /**
     * instantiates an application manager beans configuration
     */
    public ApplicationManagerBeans() {
    }

    /**
     * instantiates an application manager beans configuration
     *
     * @param applicationManagerServiceConfig
     *            The application manager service configuration.
     * @param metaStoreConfig
     *            The meta store configuration.
     */
    public ApplicationManagerBeans(final ApplicationManagerServiceConfig applicationManagerServiceConfig,
                                   // final TemplateManagerServiceConfig templateManagerServiceConfig,
                                   final MetaStoreConfig metaStoreConfig) {
        super();
        this.applicationManagerServiceConfig = applicationManagerServiceConfig;
        this.metaStoreConfig = metaStoreConfig;
    }

    /**
     * This method creates {@link ObjectMapper} bean.
     *
     * @return {@link ObjectMapper} bean.
     */
    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * This method creates {@link PBASchemaTool} bean.
     *
     * @return {@link PBASchemaTool} bean.
     */
    @Bean
    public PBASchemaTool pbaSchemaTool() {
        return new PBASchemaTool();
    }

    /**
     * This method creates {@link GitSshService} bean.
     *
     * @return {@link GitSshService} bean.
     */
    @Bean
    public GitSshService gitSSHService() {
        return new GitSshService();
    }

    /**
     * Creates the SdkDockerService
     *
     * @return SdkDockerService
     */
    @Bean
    public SdkDockerService sdkDockerService() {
        return new SdkDockerServiceBuilder().setArtifactoryServerUrl(applicationManagerServiceConfig.getArtifactoryServerUrl())
                .setArtifactoryServerPath(applicationManagerServiceConfig.getArtifactoryServerPath())
                .setDockerClientUsername(applicationManagerServiceConfig.getDockerClientUsername())
                .setDockerClientPassword(applicationManagerServiceConfig.getDockerClientPassword())
                .setDockerRepoBasePath(applicationManagerServiceConfig.getDockerRepoBasePath())
                .setDockerRepoServerUrl(applicationManagerServiceConfig.getDockerRepoServerUrl()).build();
    }

    /**
     * This method creates {@link MetaDataServiceIfc} bean.
     *
     * @return {@link MetaDataServiceIfc} bean.
     * @throws MetaDataServiceException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     * @throws IOException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     */
    @Bean
    public MetaDataServiceIfc metaDataServiceIfc() throws MetaDataServiceException, IOException {
        final MetaDataServiceLifecycleManagerIfc serviceLifecycleManager = new MetaDataServiceLifecycleManagerImpl();
        serviceLifecycleManager.provisionService(metaStoreConfig.getMetaStoreProperties());
        final MetaDataServiceIfc metaDataService = serviceLifecycleManager.getServiceReference();

        final String applicationCatalogName = applicationManagerServiceConfig.getApplicationCatalogName();
        final String serviceCatalogName = applicationManagerServiceConfig.getServiceCatalogName();
        final String templateCatalogName = applicationManagerServiceConfig.getTemplateCatalogName();

        if (!metaDataService.schemaExists(applicationCatalogName)) {
            metaDataService.createSchema(applicationCatalogName);
        }
        if (!metaDataService.schemaExists(templateCatalogName)) {
            metaDataService.createSchema(templateCatalogName);
        }
        if (!metaDataService.schemaExists(serviceCatalogName)) {
            metaDataService.createSchema(serviceCatalogName);
        }
        return metaDataService;
    }

}
