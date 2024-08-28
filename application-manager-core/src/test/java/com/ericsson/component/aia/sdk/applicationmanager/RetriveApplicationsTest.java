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
package com.ericsson.component.aia.sdk.applicationmanager;

import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.UTF_8_ENCODING;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.model.MetaData;
import com.ericsson.component.aia.sdk.applicationmanager.applications.ApplicationManagerImpl;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.tests.common.TestCommons;
import com.ericsson.component.aia.sdk.applicationmanager.views.PublishedApplicationsView;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;

@RunWith(MockitoJUnitRunner.class)
public class RetriveApplicationsTest {

    @Mock
    private TemplateManager templateManager;

    @Mock
    private MetaDataServiceIfc metaDataServiceManager;

    @Mock
    private SdkDockerService sdkDockerService;

    @Mock
    private GitSshService gitSSHService;

    private static final PBASchemaTool pbaSchemaTool = new PBASchemaTool();

    private ApplicationManagerImpl applicationManager;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        ApplicationManagerConfiguration.storageLocation = "target/applicationManager";
        ApplicationManagerConfiguration.applicationCatalogName = "aia-application-catalogs";

        ApplicationManagerConfiguration.gitServiceType = "GOGS";
        ApplicationManagerConfiguration.gitServiceUrl = "http://localhost/api/v1";
        ApplicationManagerConfiguration.gitAccessToken = "fb737170f6a647eeab1e563ce626e8642ae32c3a";

        applicationManager = ApplicationManagerImpl.builder().templateManager(templateManager).metaDataServiceManager(metaDataServiceManager)
                .pbaSchemaTool(pbaSchemaTool).sdkDockerService(sdkDockerService).gitSSHService(gitSSHService).build();

        final String pbaAsString = FileUtils.readFileToString(new File(TestCommons.CURRENT_PATH + "/src/test/resources/pba-good.json"),
                UTF_8_ENCODING);
        Mockito.when(metaDataServiceManager.get(Mockito.anyString(), Mockito.anyString())).thenReturn(pbaAsString);
        final ArrayList<MetaData> test = new ArrayList<>();
        final MetaData metaData = new MetaData();
        metaData.setKey("rt45-ury67-9ret");
        metaData.setValue(pbaAsString);
        test.add(metaData);
        when(metaDataServiceManager.findByPropertyValue(eq("aia-application-catalogs"), eq("pba.status"), eq("ACTIVE"))).thenReturn(test);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.sdk.applicationmanager.applications.ApplicationManagerImpl#getPBAInstance(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testGetApplication() {
        final PBAInstance application = applicationManager.getPBAInstance("rt45-ury67-9ret");
        assertTrue("application should not be empty",
                application != null && StringUtils.isNotBlank(application.getPba().getApplicationInfo().getName()));
    }

    @Test
    public void testListApplication() {
        final Collection<PublishedApplicationsView> listApplications = applicationManager.listApplications();
        assertTrue("listApplications should not be empty", listApplications.size() > 0);
    }
}
