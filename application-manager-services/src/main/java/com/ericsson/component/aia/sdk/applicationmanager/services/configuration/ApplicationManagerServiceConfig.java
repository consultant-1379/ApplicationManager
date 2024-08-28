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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;

/**
 * Application manager service configuration
 *
 * TODO Using the tag below to suppress PMD TooManyFields error. Sort this out.
 */
@Component
@SuppressWarnings("PMD.TooManyFields")
public class ApplicationManagerServiceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationManagerServiceConfig.class);

    @Value("${template.manager.local.repo.path}")
    private String templateManagerLocalRepoPath;

    @Value("${storage.location}")
    private String storageLocation;

    @Value("${application.catalog.name}")
    private String applicationCatalogName;

    @Value("${template.catalog.name}")
    private String templateCatalogName;

    @Value("${service.catalog.name}")
    private String serviceCatalogName;

    @Value("${docker.client.username}")
    private String dockerClientUsername;

    @Value("${docker.client.password}")
    private String dockerClientPassword;

    @Value("${git.service.type}")
    private String gitServiceType;

    @Value("${git.service.url}")
    private String gitServiceUrl;

    @Value("${git.service.ssl.url}")
    private String gitServiceSslUrl;

    @Value("${git.service.access.token}")
    private String gitAccessToken;

    @Value("${docker.repo.application.path}")
    private String dockerRepoBasePath;

    @Value("${docker.repo.server.url}")
    private String dockerRepoServerUrl;

    @Value("${artifactory.server.url}")
    private String artifactoryServerUrl;

    @Value("${artifactory.server.path}")
    private String artifactoryServerPath;

    @Value("${max.uploaded.file.size}")
    private long maxUploadedFileSize;

    /**
     * Print the parameters being used to start the spring application
     */
    public void printConfiguration() {

        LOGGER.info("applicationCatalogName::{}", applicationCatalogName);
        LOGGER.info("templateCatalogName::{}", templateCatalogName);
        LOGGER.info("serviceCatalogName::{}", serviceCatalogName);

        LOGGER.info("gitServiceType::{}", gitServiceType);
        LOGGER.info("gitServiceUrl::{}", gitServiceUrl);
        LOGGER.info("gitAccessToken::{}", gitAccessToken);

        LOGGER.info("storageLocation::{}", storageLocation);
        LOGGER.info("templateManagerLocalRepoPath::{} ", templateManagerLocalRepoPath);

        LOGGER.info("artifactoryServerUrl::{} ", artifactoryServerUrl);
        LOGGER.info("artifactoryServerPath::{}", artifactoryServerPath);

        LOGGER.info("dockerClientUsername::{}", dockerClientUsername);
        LOGGER.info("dockerClientPassword::{}", dockerClientPassword);
        LOGGER.info("dockerRepoBasePath::{}", dockerRepoBasePath);
        LOGGER.info("dockerRepoServerUrl::{} ", dockerRepoServerUrl);

        LOGGER.info("maxUploadedFileSize::{} ", maxUploadedFileSize);

    }

    /**
     * This method will update the ApplicationManagerConfiguration to include all application properties.
     */
    public void updateApplicationManagerConfiguration() {
        ApplicationManagerConfiguration.applicationCatalogName = applicationCatalogName;
        ApplicationManagerConfiguration.templateCatalogName = templateCatalogName;
        ApplicationManagerConfiguration.serviceCatalogName = serviceCatalogName;

        ApplicationManagerConfiguration.dockerClientUsername = dockerClientUsername;
        ApplicationManagerConfiguration.dockerClientPassword = dockerClientPassword;
        ApplicationManagerConfiguration.templateManagerLocalRepoPath = templateManagerLocalRepoPath;

        ApplicationManagerConfiguration.storageLocation = storageLocation;

        ApplicationManagerConfiguration.gitServiceType = gitServiceType;
        ApplicationManagerConfiguration.gitServiceUrl = gitServiceUrl;
        ApplicationManagerConfiguration.gitAccessToken = gitAccessToken;

        ApplicationManagerConfiguration.dockerRepoBasePath = dockerRepoBasePath;
        ApplicationManagerConfiguration.dockerRepoServerUrl = dockerRepoServerUrl;

        ApplicationManagerConfiguration.artifactoryServerUrl = artifactoryServerUrl;
        ApplicationManagerConfiguration.artifactoryServerPath = artifactoryServerPath;

        ApplicationManagerConfiguration.maxUploadedFileSize = maxUploadedFileSize;

    }

    public String getServiceCatalogName() {
        return serviceCatalogName;
    }

    public String getApplicationCatalogName() {
        return applicationCatalogName;
    }

    public String getTemplateCatalogName() {
        return templateCatalogName;
    }

    public String getDockerClientUsername() {
        return dockerClientUsername;
    }

    public String getDockerClientPassword() {
        return dockerClientPassword;
    }

    public String getGitServiceType() {
        return gitServiceType;
    }

    public String getGitServiceUrl() {
        return gitServiceUrl;
    }

    public String getGitServiceSslUrl() {
        return gitServiceSslUrl;
    }

    public String getGitAccessToken() {
        return gitAccessToken;
    }

    public String getDockerRepoBasePath() {
        return dockerRepoBasePath;
    }

    public String getDockerRepoServerUrl() {
        return dockerRepoServerUrl;
    }

    public String getArtifactoryServerUrl() {
        return artifactoryServerUrl;
    }

    public String getArtifactoryServerPath() {
        return artifactoryServerPath;
    }

    public long getMaxUploadedFileSize() {
        return maxUploadedFileSize;
    }

}
