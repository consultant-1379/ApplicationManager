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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.api.ApplicationManager;
import com.ericsson.component.aia.sdk.applicationmanager.api.ServiceManager;
import com.ericsson.component.aia.sdk.applicationmanager.applications.ApplicationManagerImpl;
import com.ericsson.component.aia.sdk.applicationmanager.factory.ApplicationFactoryBuilder;
import com.ericsson.component.aia.sdk.applicationmanager.factory.ApplicationFactoryProducer;
import com.ericsson.component.aia.sdk.applicationmanager.factory.ApplicatorFactoryProducerBuilder;
import com.ericsson.component.aia.sdk.applicationmanager.services.DependencyInformationServiceImpl;
import com.ericsson.component.aia.sdk.applicationmanager.services.ServiceManagerImpl;
import com.ericsson.component.aia.sdk.git.project.service.GitProjectService;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.templatemanager.impl.TemplateManagerImpl;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;
import com.ericsson.component.aia.sdk.util.docker.dependency.DependencyChecker;
import com.ericsson.component.aia.sdk.util.docker.dependency.DependencyCheckerBuilder;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * This will define beans needed for this application to run.
 *
 * @author echchik
 *
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, EmbeddedMongoAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class, CassandraAutoConfiguration.class })
@EnableSwagger2
@ComponentScan({ "com.ericsson.component.aia.sdk.applicationmanager.services", "com.ericsson.component.aia.sdk.servicemanager.services",
        "com.ericsson.component.aia.sdk.applicationmanager.factory" })
public class ApplicationManagerApplication {

    @Autowired
    private ApplicationManagerServiceConfig applicationManagerServiceConfig;

    @Autowired
    private TemplateManagerServiceConfig templateManagerServiceConfig;

    @Autowired
    private PBASchemaTool pbaSchemaTool;

    @Autowired
    private GitSshService gitSshService;

    @Autowired
    private MetaDataServiceIfc metaDataServiceIfc;

    @Autowired
    private SdkDockerService sdkDockerService;

    /**
     * This method creates {@link ApplicationManager} bean.
     *
     * @return {@link ApplicationManager} bean.
     * @throws MetaDataServiceException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     * @throws IOException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     */
    @Bean
    public ApplicationManager applicationManager() throws MetaDataServiceException, IOException {
        applicationManagerServiceConfig.printConfiguration();
        applicationManagerServiceConfig.updateApplicationManagerConfiguration();
        return ApplicationManagerImpl.builder().metaDataServiceManager(metaDataServiceIfc).templateManager(templateManager())
                .pbaSchemaTool(pbaSchemaTool).sdkDockerService(sdkDockerService).gitSSHService(gitSshService).build();
    }

    /**
     * This method creates {@link ApplicationFactoryProducer} bean.
     *
     * @return {@link ApplicationFactoryProducer} bean.
     * @throws MetaDataServiceException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     * @throws IOException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     */
    @Bean
    public ApplicationFactoryProducer applicationFactoryManager() throws MetaDataServiceException, IOException {

        //TODO inject simple cache
        final ApplicationFactoryBuilder applicationFactoryBuilder = new ApplicationFactoryBuilder(applicationManager(), pbaSchemaTool);

        return ApplicatorFactoryProducerBuilder.builder(applicationFactoryBuilder).build();
    }

    /**
     * This method creates {@link TemplateManager} bean.
     *
     * @return {@link TemplateManager} bean.
     * @throws MetaDataServiceException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     * @throws IOException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     */
    @Bean
    public TemplateManager templateManager() throws MetaDataServiceException, IOException {
        templateManagerServiceConfig.printConfiguration();
        templateManagerServiceConfig.updateTemplateManagerConfiguration();
        final GitProjectService gitProjectService = GitProjectService.newGitProjectRepository(applicationManagerServiceConfig.getGitServiceType(),
                applicationManagerServiceConfig.getGitAccessToken(), applicationManagerServiceConfig.getGitServiceUrl(),
                applicationManagerServiceConfig.getGitServiceSslUrl());

        final TemplateManagerImpl templateManagerImpl = new TemplateManagerImpl(metaDataServiceIfc, pbaSchemaTool, gitProjectService, gitSshService,
                dependencyChecker());
        return templateManagerImpl;
    }

    /**
     * Get a new Dependency checker which is used to check if all of a PBAs dependencies exist if not an exception is thrown.
     *
     * @return the sdk docker service
     * @throws MetaDataServiceException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     * @throws IOException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     */
    public DependencyChecker dependencyChecker() throws MetaDataServiceException, IOException {
        return new DependencyCheckerBuilder().setServiceCatalogName(applicationManagerServiceConfig.getServiceCatalogName())
                .setApplicationCatalogName(applicationManagerServiceConfig.getApplicationCatalogName()).setMetaDataServiceManager(metaDataServiceIfc)
                .setPbaSchemaTool(pbaSchemaTool).setSdkDockerService(sdkDockerService).build();
    }

    /**
     * This method creates {@link TemplateManager} bean.
     *
     * @return {@link TemplateManager} bean.
     * @throws MetaDataServiceException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     * @throws IOException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     */
    @Bean
    public ServiceManager serviceManager() throws MetaDataServiceException, IOException {
        final ServiceManagerImpl serviceManagerImpl = new ServiceManagerImpl(pbaSchemaTool);
        serviceManagerImpl.setMetaDataService(metaDataServiceIfc);
        serviceManagerImpl.setServiceCatalogName(applicationManagerServiceConfig.getServiceCatalogName());
        serviceManagerImpl.setDependencyInformationService(new DependencyInformationServiceImpl(metaDataServiceIfc, pbaSchemaTool));
        return serviceManagerImpl;
    }

    /**
     * Swagger Configuration
     *
     * @return Docket
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.any()).paths(paths()).build();
    }

    // Describe your App mgr APIs
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("Application Manager APIs").description("Lists Application Manager APIs").build();
    }

    // Only select apis that matches the given Predicates.
    private Predicate<String> paths() {
        // Match all paths except /error
        return Predicates.and(PathSelectors.regex("/.*"), Predicates.not(PathSelectors.regex("/error.*")));
    }

    /**
     * Starting point of the application
     *
     * @param args
     *            arguments for the application.
     */
    public static void main(final String[] args) {
        SpringApplication.run(ApplicationManagerApplication.class, args);
    }
}
