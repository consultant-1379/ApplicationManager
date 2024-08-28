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

import com.ericsson.component.aia.sdk.templatemanager.impl.TemplateManagerConfiguration;

/**
 * Template manager service configuration
 *
 * This extra configuration class is needed because PMD code analysis tool was failing due to too many fields in the ApplicationManagerServiceConfig
 * class.
 *
 */
@Component
public class TemplateManagerServiceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateManagerServiceConfig.class);

    @Value("${template.manager.local.repo.path}")
    private String templateManagerLocalRepoPath;

    @Value("${template.catalog.name}")
    private String templateCatalogName;

    @Value("${git.service.type}")
    private String gitServiceType;

    @Value("${git.service.url}")
    private String gitServiceUrl;

    @Value("${git.service.access.token}")
    private String gitAccessToken;

    /**
     * Prints the log information.
     */
    public void printConfiguration() {

        LOGGER.info("templateCatalogName::{}", templateCatalogName);

        LOGGER.info("gitServiceType::{}", gitServiceType);
        LOGGER.info("gitServiceUrl::{}", gitServiceUrl);
        LOGGER.info("gitAccessToken::{}", gitAccessToken);

        LOGGER.info("templateManagerLocalRepoPath::{} ", templateManagerLocalRepoPath);

    }

    /**
     * This method will update the TemplateManagerConfiguration to include its application properties.
     */
    public void updateTemplateManagerConfiguration() {
        TemplateManagerConfiguration.localRepoPath = templateManagerLocalRepoPath;
        TemplateManagerConfiguration.templateCatalogName = templateCatalogName;

        TemplateManagerConfiguration.gitServiceType = gitServiceType;
        TemplateManagerConfiguration.gitServiceUrl = gitServiceUrl;
        TemplateManagerConfiguration.gitAccessToken = gitAccessToken;
    }
}