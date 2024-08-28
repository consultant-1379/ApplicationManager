/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.applicationmanager.services;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.aia.metadata.model.MetaData;
import com.ericsson.component.aia.sdk.applicationmanager.services.configuration.TestConstants;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;

@Component
public class TestUtils {

    @Value("${application.catalog.name}")
    private String applicationCatalogName;

    @Value("${service.catalog.name}")
    private String serviceCatalogName;

    @Value("${template.catalog.name}")
    private String templateCatalogName;

    @Autowired
    private MetaDataServiceIfc metaDataService;

    /**
     * This method will erase all entries from meta store
     *
     * @throws MetaDataServiceException
     */
    public void clearMetaStore() throws MetaDataServiceException {
        for (final MetaData metaData : metaDataService.findAll(applicationCatalogName)) {
            if (!metaData.getKey().isEmpty()) {
                metaDataService.delete(applicationCatalogName, metaData.getKey());
            }
        }
        for (final MetaData metaData : metaDataService.findAll(templateCatalogName)) {
            if (!metaData.getKey().isEmpty()) {
                metaDataService.delete(templateCatalogName, metaData.getKey());
            }
        }
        for (final MetaData metaData : metaDataService.findAll(serviceCatalogName)) {
            if (!metaData.getKey().isEmpty()) {
                metaDataService.delete(serviceCatalogName, metaData.getKey());
            }
        }
    }

    public String createTemplate(final String pbaFilePath) throws IOException, MetaDataServiceException {
        final String pbaAsString = getPbaAsString(pbaFilePath);
        final String pbaId = metaDataService.put(templateCatalogName, TestConstants.TEST_TEMPLATE_METADATA_KEY, pbaAsString);
        return pbaId;
    }

    public String createApplication(final String pbaFilePath) throws IOException, MetaDataServiceException {
        final String pbaAsString = getPbaAsString(pbaFilePath);
        final String pbaId = metaDataService.put(applicationCatalogName, TestConstants.TEST_APPLICATION_METADATA_KEY, pbaAsString);
        return pbaId;
    }

    public String createService(final String pbaFilePath) throws IOException, MetaDataServiceException {
        final String pbaAsString = getPbaAsString(pbaFilePath);
        final String pbaId = metaDataService.put(serviceCatalogName, TestConstants.TEST_SERVICE_METADATA_KEY, pbaAsString);
        return pbaId;
    }

    public String getPbaAsString(final String pbaFilePath) throws IOException {
        final File pbaJson = new File(pbaFilePath);
        final String pbaAsString = PBASchemaTool.readStreamAsStringFronFile(pbaJson.toPath());
        return pbaAsString;
    }

}
