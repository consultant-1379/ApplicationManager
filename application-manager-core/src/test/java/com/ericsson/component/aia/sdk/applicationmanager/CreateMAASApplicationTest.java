package com.ericsson.component.aia.sdk.applicationmanager;

import static com.ericsson.component.aia.sdk.applicationmanager.tests.common.TestCommons.CURRENT_PATH;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.component.aia.sdk.applicationmanager.applications.ApplicationManagerImpl;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;
import com.google.common.io.Files;

/**
 * CreateApplicationTest checks all validation scenarios for Create application scenarios.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateMAASApplicationTest {

    /** Logger for CreateApplicationTest */
    private static final Logger Log = LoggerFactory.getLogger(CreateApplicationTest.class);

    @Mock
    private TemplateManager templateManager;

    @Mock
    private MetaDataServiceIfc metaDataServiceManager;

    @Mock
    private SdkDockerService sdkDockerService;

    @Mock
    private GitSshService gitSSHService;

    private ApplicationManagerImpl applicationManager;
    private Path applicationPath;
    private final Path templatePath = Paths.get(CURRENT_PATH + "/target/MAASBlankTemplate.zip");
    private static final PBASchemaTool pbaSchemaTool = new PBASchemaTool();

    @Before
    public void setup() throws IOException {
        ApplicationManagerConfiguration.storageLocation = "target/applicationManager";
        ApplicationManagerConfiguration.applicationCatalogName = "aia-application-catalogs";

        ApplicationManagerConfiguration.gitServiceType = "GOGS";
        ApplicationManagerConfiguration.gitServiceUrl = "http://localhost/api/v1";
        ApplicationManagerConfiguration.gitAccessToken = "fb737170f6a647eeab1e563ce626e8642ae32c3a";

        applicationManager = ApplicationManagerImpl.builder().templateManager(templateManager).metaDataServiceManager(metaDataServiceManager)
                .pbaSchemaTool(pbaSchemaTool).sdkDockerService(sdkDockerService).gitSSHService(gitSSHService).build();

        Files.copy(Paths.get(CURRENT_PATH + "/src/test/resources/MAASBlankTemplate.zip").toFile(), templatePath.toFile());

    }

    /**
     * testCreateApplication is a valid test scenario for testing Create-Application use-case.
     */
    @Test
    public void testCreateMAASApplication() {
        Log.info("testCreateMAASApplication");
        File maasJsonFile = null;
        try {
            //mock mass-ui.json file
            maasJsonFile = ResourceUtils.getFile("classpath:maas-ui.json");

        } catch (final Exception e) {
            e.printStackTrace();
        }

        String maasJSONStr = null;
        try {
            maasJSONStr = FileUtils.readFileToString(maasJsonFile, Charset.defaultCharset());
        } catch (final IOException e) {
            e.printStackTrace();
        }

        applicationPath = applicationManager.createMAASApplication(applicationManager.getMAASInstance(maasJSONStr));
        assertThat("ApplicationManager createApplication Failed", applicationPath.toFile().exists());
        Log.info("end testCreateMAASApplication");
    }

}