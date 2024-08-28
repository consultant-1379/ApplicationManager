/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.applicationmanager.factory;

import static org.springframework.http.ResponseEntity.status;

import java.nio.file.Path;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ericsson.component.aia.sdk.applicationmanager.api.ApplicationManager;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.applicationmanager.model.MAASInstance;
import com.ericsson.component.aia.sdk.applicationmanager.services.endpoints.response.ServiceResponse;
import com.ericsson.component.aia.sdk.applicationmanager.util.AppSdkUtil;
import com.ericsson.component.aia.sdk.pba.exception.InvalidPbaException;
import com.ericsson.component.aia.sdk.templatemanager.cache.ArchivePathCache;

/**
 * MaasApplication for creating mass application based on passed maas json string
 *
 */
public class MaasApplication implements Maas {

    private final ApplicationFactoryBuilder applicationFactoryBuilder;

    /**
     * initializes MaasApplication
     *
     * @param applicationFactoryBuilder
     *            applicationFactoryBuilder
     */
    MaasApplication(final ApplicationFactoryBuilder applicationFactoryBuilder) {
        this.applicationFactoryBuilder = applicationFactoryBuilder;
    }

    /**
     * create method generates maas application
     *
     * @param maasJsonStr
     *            maasJsonStr
     * @return ResponseEntity ResponseEntity
     */
    public ResponseEntity<ServiceResponse<String>> create(final String maasJsonStr) {

        final ApplicationManager applicationManager = applicationFactoryBuilder.getApplicationManager();
        final ArchivePathCache simpleCache = applicationFactoryBuilder.getSimpleCache();
        final ServiceResponse<String> serviceResponse = new ServiceResponse<>();

        try {

            final MAASInstance maasInstance = applicationManager.getMAASInstance(maasJsonStr);

            final Path path = applicationManager.createMAASApplication(maasInstance);

            final String applicationId = AppSdkUtil.createMaasApplicationId(maasInstance.getMaas());
            serviceResponse.setData(simpleCache.put(applicationId, path));

        } catch (final InvalidPbaException ex) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.MAAS_INVALID_DATA, ex);
        }

        return status(HttpStatus.OK).body(serviceResponse);
    }

}