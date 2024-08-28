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
import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ericsson.component.aia.sdk.applicationmanager.api.ApplicationManager;
import com.ericsson.component.aia.sdk.applicationmanager.applications.ApplicationController;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.applicationmanager.services.endpoints.response.ServiceResponse;
import com.ericsson.component.aia.sdk.applicationmanager.util.AppSdkUtil;
import com.ericsson.component.aia.sdk.pba.exception.InvalidPbaException;
import com.ericsson.component.aia.sdk.pba.model.BuildInfo;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.cache.ArchivePathCache;

/**
 * GenericApplication for creating applications
 *
 */
public class GenericApplication implements Application {

    private static final Logger Log = LoggerFactory.getLogger(GenericApplication.class);

    private ApplicationManager applicationManager;

    private PBASchemaTool pbaTool;

    @Autowired
    private ArchivePathCache simpleCache;

    private final ApplicationFactoryBuilder applicationFactoryBuilder;

    /**
     * initializes GenericApplication for injecting applicationFactoryBuilder
     * @param applicationFactoryBuilder
     *           applicationFactoryBuilder
     */
    GenericApplication(final ApplicationFactoryBuilder applicationFactoryBuilder) {
        this.applicationFactoryBuilder = applicationFactoryBuilder;
    }

    @Override
    public ResponseEntity<ServiceResponse<String>> create(final String jsonStr) {

        final ServiceResponse<String> serviceResponse = new ServiceResponse<>();

        applicationManager = applicationFactoryBuilder.getApplicationManager();
        pbaTool = applicationFactoryBuilder.getPbaSchemaTool();
        simpleCache = applicationFactoryBuilder.getSimpleCache();

        try {

            final PBAInstance pbaObject = pbaTool.getPBAModelInstance(jsonStr);

            if (pbaObject.getPba() == null || pbaObject.getPba().getBuildInfo() == null) {
                throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, "Pba buildInfo information is invalid");
            }

            final BuildInfo buildInfo = pbaObject.getPba().getBuildInfo();
            buildInfo.setDependencies(new ArrayList<>(new HashSet<>(buildInfo.getDependencies())));

            final String applicationId = AppSdkUtil.createApplicationId(pbaObject.getPba());
            Log.info("Generated Application id is {}", applicationId);
            pbaObject.getPba().getApplicationInfo().setId(applicationId);

            final Path path = applicationManager.createApplication(pbaObject);
            //add application for collection in the future
            ApplicationController.instance().addApplication(applicationId, path);

            serviceResponse.setData(simpleCache.put(applicationId, path));

        } catch (final InvalidPbaException ex) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, ex);
        }

        return status(HttpStatus.OK).body(serviceResponse);
    }

}