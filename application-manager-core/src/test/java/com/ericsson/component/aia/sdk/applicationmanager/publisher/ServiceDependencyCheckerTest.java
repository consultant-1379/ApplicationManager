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
package com.ericsson.component.aia.sdk.applicationmanager.publisher;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.service.views.DependencyInfo;
import com.ericsson.component.aia.sdk.applicationmanager.services.DependencyInformationService;
import com.ericsson.component.aia.sdk.applicationmanager.services.DependencyInformationServiceImpl;
import com.ericsson.component.aia.sdk.metadataservice.stub.MetaDataServiceIfcStub;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;

public class ServiceDependencyCheckerTest {

    private MetaDataServiceIfc metaDataServiceIfc;
    private final PBASchemaTool pbaSchemaTool = new PBASchemaTool();

    private DependencyInformationService dependencyInformationService;

    private String app1;
    private String app2;
    private String service1;
    private String service2;

    private DependencyInfo app1DependencyInfo;
    private DependencyInfo app2DependencyInfo;
    private DependencyInfo service1DependencyInfo;
    private DependencyInfo service2DependencyInfo;

    @Before
    public void setup() throws MetaDataServiceException, IOException {
        metaDataServiceIfc = new MetaDataServiceIfcStub();
        ApplicationManagerConfiguration.serviceCatalogName = "aiaServiceCatalog";
        ApplicationManagerConfiguration.applicationCatalogName = "aia-application-catalog";

        metaDataServiceIfc.createSchema("aia-application-catalog");
        metaDataServiceIfc.createSchema("aiaServiceCatalog");

        dependencyInformationService = new DependencyInformationServiceImpl(metaDataServiceIfc, pbaSchemaTool);

        app1 = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("dependencyTest/app1.json"), "UTF-8");
        app2 = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("dependencyTest/app2.json"), "UTF-8");

        service1 = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("dependencyTest/service1.json"), "UTF-8");
        service2 = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("dependencyTest/service2.json"), "UTF-8");

        metaDataServiceIfc.put("aia-application-catalog", "a-1", app1);
        metaDataServiceIfc.put("aia-application-catalog", "a-2", app2);
        metaDataServiceIfc.put("aiaServiceCatalog", "s-1", service1);
        metaDataServiceIfc.put("aiaServiceCatalog", "s-2", service2);

        app1DependencyInfo = new DependencyInfo(pbaSchemaTool.getPBAModelInstance(app1).getPba());
        app2DependencyInfo = new DependencyInfo(pbaSchemaTool.getPBAModelInstance(app2).getPba());
        service1DependencyInfo = new DependencyInfo(pbaSchemaTool.getPBAModelInstance(service1).getPba());
        service2DependencyInfo = new DependencyInfo(pbaSchemaTool.getPBAModelInstance(service2).getPba());

    }

    @Test
    public void shouldVerifyDependenciesExist() throws IOException {
        final Collection<DependencyInfo> dependencyInfo = dependencyInformationService.getDependencyCollection("a-1");
        assertEquals(dependencyInfo.size(), 4);
        assertThat(dependencyInfo, containsInAnyOrder(app1DependencyInfo, app2DependencyInfo, service1DependencyInfo, service2DependencyInfo));
    }

}
