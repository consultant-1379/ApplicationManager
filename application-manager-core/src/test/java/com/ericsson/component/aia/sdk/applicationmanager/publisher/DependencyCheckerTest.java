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

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.metadataservice.stub.MetaDataServiceIfcStub;
import com.ericsson.component.aia.sdk.pba.model.BuildInfo;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;

@RunWith(MockitoJUnitRunner.class)
public class DependencyCheckerTest {

    private DependencyChecker dependencyChecker;

    private MetaDataServiceIfc metaDataServiceIfc;
    private final PBASchemaTool pbaSchemaTool = new PBASchemaTool();
    private Pba publishedApplicationPba;

    @Mock
    private SdkDockerService sdkDockerService;

    @Before
    public void setup() throws MetaDataServiceException {
        metaDataServiceIfc = new MetaDataServiceIfcStub();
        dependencyChecker = new DependencyChecker(metaDataServiceIfc, pbaSchemaTool, sdkDockerService);

        ApplicationManagerConfiguration.serviceCatalogName = "aiaServiceCatalog";
        ApplicationManagerConfiguration.applicationCatalogName = "aia-application-catalog";
        ApplicationManagerConfiguration.artifactoryServerUrl = "https://arm.epk.ericsson.se/artifactory";
        ApplicationManagerConfiguration.dockerRepoBasePath = "aia/test";

        metaDataServiceIfc.createSchema("aia-application-catalog");
        metaDataServiceIfc.createSchema("aiaServiceCatalog");

        final BuildInfo buildInfo = new BuildInfo();
        buildInfo.setDependencies(singletonList("a-1"));

        publishedApplicationPba = new Pba();
        publishedApplicationPba.setBuildInfo(buildInfo);
    }

    @Test
    public void shouldVerifyDependenciesExist() throws IOException, MetaDataServiceException {
        addAllDependenciesToMetaStore();
        withAllDockerImagesPresent();
        dependencyChecker.verifyDependenciesExist(publishedApplicationPba);
    }

    /**
     * An exception should be thrown if an applications dependencies don't exist in meta store
     */
    @Test(expected = ApplicationManagerException.class)
    public void shouldThrowExceptionIfDependenciesDontExist() {
        withAllDockerImagesPresent();
        dependencyChecker.verifyDependenciesExist(publishedApplicationPba);
    }

    /**
     * An exception should be thrown if an applications dependencies exist in meta store but the docker images specified by those dependencies don't
     * exist.
     *
     * @throws MetaDataServiceException
     * @throws IOException
     */
    @Test(expected = ApplicationManagerException.class)
    public void shouldThrowExceptionIfDockerDoesntExist() throws MetaDataServiceException, IOException {
        addAllDependenciesToMetaStore();
        missingADockerImage();
        dependencyChecker.verifyDependenciesExist(publishedApplicationPba);
    }

    private void missingADockerImage() {
        when(sdkDockerService.isDockerImageExistsInRepo("aia/test", "app1:1.0.0")).thenReturn(true);
        when(sdkDockerService.isDockerImageExistsInRepo("aia/test", "app2:1.0.0")).thenReturn(true);
        when(sdkDockerService.isDockerImageExistsInRepo("aia/base", "aia-docker-kafka:latest")).thenReturn(true);
        when(sdkDockerService.isDockerImageExistsInRepo("aia/base", "aia-docker-zookeeper:latest")).thenReturn(false);
    }

    private void withAllDockerImagesPresent() {
        when(sdkDockerService.isDockerImageExistsInRepo("aia/test", "app1:1.0.0")).thenReturn(true);
        when(sdkDockerService.isDockerImageExistsInRepo("aia/test", "app2:1.0.0")).thenReturn(true);
        when(sdkDockerService.isDockerImageExistsInRepo("aia/base", "aia-docker-kafka:latest")).thenReturn(true);
        when(sdkDockerService.isDockerImageExistsInRepo("aia/base", "aia-docker-zookeeper:latest")).thenReturn(true);
    }

    private void addAllDependenciesToMetaStore() throws MetaDataServiceException, IOException {
        metaDataServiceIfc.put("aia-application-catalog", "a-1",
                IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("dependencyTest/app1.json"), "UTF-8"));
        metaDataServiceIfc.put("aia-application-catalog", "a-2",
                IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("dependencyTest/app2.json"), "UTF-8"));
        metaDataServiceIfc.put("aiaServiceCatalog", "s-1",
                IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("dependencyTest/service1.json"), "UTF-8"));
        metaDataServiceIfc.put("aiaServiceCatalog", "s-2",
                IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("dependencyTest/service2.json"), "UTF-8"));
    }
}
