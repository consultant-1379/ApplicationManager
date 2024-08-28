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

import org.jfrog.artifactory.client.Artifactory;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.stubs.GitSshServiceStub;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.util.docker.DockerClientProvider;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.DockerClient;

/**
 * This will define beans needed for this application to run.
 *
 * @author echchik
 *
 */
@Configuration
@Profile("test")
public class ApplicationManagerTestBeans {

    @Autowired
    private ApplicationManagerServiceConfig applicationManagerServiceConfig;

    @Autowired
    private MetaStoreConfig metaStoreConfig;

    private ApplicationManagerBeans applicationManagerBeans;
    private final EmbeddedMongoConfiguration embeddedMongoConfiguration = new EmbeddedMongoConfiguration();
    private final GitSshService gitSshService = new GitSshServiceStub();
    private final Artifactory artifactory = Mockito.mock(Artifactory.class);

    private final DockerClient dockerClient = Mockito.mock(DockerClient.class);

    /**
     * This method creates {@link DockerClientProvider} bean.
     *
     * @return {@link DockerClientProvider} bean.
     */
    @Bean
    public DockerClientProvider dockerClientProvider() {
        return new DockerClientProvider("dummyArtifactoryUrl", "dummyUsername", "dummyPassword", "dummyRepoServerUrl") {
            @Override
            public Artifactory getArtifactoryClient() {
                return artifactory;
            }

            @Override
            public DockerClient getDockerClient() {
                return dockerClient;
            }
        };
    }

    @Bean
    public SdkDockerService sdkDockerService() {
        return new SdkDockerService(dockerClientProvider(), "docker-v2-global-local", "dummyRepoBasePath", "dummyRepoServerUrl") {
            @Override
            public com.ericsson.component.aia.sdk.util.docker.DockerClientProvider getDockerClientProvider() {
                return dockerClientProvider();
            }
        };
    }

    /**
     * This method creates {@link GitSshService} bean.
     *
     * @return {@link GitSshService} bean.
     */
    @Bean
    public GitSshService gitSSHService() {
        return gitSshService;
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
    @DependsOn("embeddedMongo")
    public MetaDataServiceIfc metaDataServiceIfc() throws MetaDataServiceException, IOException {
        return getApplicationManagerBeans().metaDataServiceIfc();
    }

    @Bean(destroyMethod = "stopMongo")
    public EmbeddedMongoConfiguration embeddedMongo() throws MetaDataServiceException, IOException {
        embeddedMongoConfiguration.startMongo();
        return embeddedMongoConfiguration;
    }

    /**
     * This method creates {@link ObjectMapper} bean.
     *
     * @return {@link ObjectMapper} bean.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return getApplicationManagerBeans().objectMapper();
    }

    /**
     * This method creates {@link PBASchemaTool} bean.
     *
     * @return {@link PBASchemaTool} bean.
     */
    @Bean
    public PBASchemaTool pbaSchemaTool() {
        return getApplicationManagerBeans().pbaSchemaTool();
    }

    private ApplicationManagerBeans getApplicationManagerBeans() {
        if (applicationManagerBeans == null) {
            applicationManagerBeans = new ApplicationManagerBeans(applicationManagerServiceConfig, metaStoreConfig);
        }
        return applicationManagerBeans;
    }
}
