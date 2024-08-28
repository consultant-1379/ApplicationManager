package com.ericsson.component.aia.sdk.applicationmanager;

import static com.ericsson.component.aia.sdk.applicationmanager.tests.common.TestCommons.CURRENT_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.component.aia.sdk.applicationmanager.applications.ApplicationManagerImpl;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;
import com.google.common.io.Files;

/**
 * CreateApplicationTest checks all validation scenarios for Create application scenarios.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateKafkaConApplicationTest {

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

    private static final PBASchemaTool pbaSchemaTool = new PBASchemaTool();

    private final PBAInstance pbaInstance = getTemporaryPBAInstance();

    private ApplicationManagerImpl applicationManager;
    private Path applicationPath;
    private final Path templatePath = Paths.get(CURRENT_PATH + "/target/KafkaConnectBlankTemplate.zip");

    @Before
    public void setup() throws IOException {
        ApplicationManagerConfiguration.storageLocation = "target/applicationManager";
        ApplicationManagerConfiguration.applicationCatalogName = "aia-application-catalogs";

        ApplicationManagerConfiguration.gitServiceType = "GOGS";
        ApplicationManagerConfiguration.gitServiceUrl = "http://localhost/api/v1";
        ApplicationManagerConfiguration.gitAccessToken = "fb737170f6a647eeab1e563ce626e8642ae32c3a";

        applicationManager = ApplicationManagerImpl.builder().templateManager(templateManager).metaDataServiceManager(metaDataServiceManager)
                .pbaSchemaTool(pbaSchemaTool).sdkDockerService(sdkDockerService).gitSSHService(gitSSHService).build();

        Files.copy(Paths.get(CURRENT_PATH + "/src/test/resources/KafkaConnectBlankTemplate.zip").toFile(), templatePath.toFile());

        when(templateManager.downloadTemplate(Mockito.anyString()))
                .thenReturn(Paths.get(CURRENT_PATH + "/src/test/resources/KafkaConnectBlankTemplate.zip"));
    }

    /**
     * testCreateApplication is a valid test scenario for testing Create-Application use-case.
     */
    @Test
    public void testCreateApplication() {
        applicationPath = applicationManager.createApplication(pbaInstance);
        assertThat("ApplicationManager createApplication Failed", applicationPath.toFile().exists());

    }

    private static PBAInstance getTemporaryPBAInstance() {
        try {
            return pbaSchemaTool.getPBAModelInstance(
                    FileUtils.readFileToString(new File(CURRENT_PATH + "/src/test/resources/pba-kafka-connect.json"), Charsets.UTF_8));
        } catch (final IOException e) {
            Log.error("exception occurred, when trying to get a getTemporaryPBAInstance", e);
            throw new RuntimeException(e);
        }
    }

}
