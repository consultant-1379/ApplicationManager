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
package com.ericsson.component.aia.sdk.applicationmanager.services.endpoints;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

// import com.ericsson.component.aia.sdk.applicationmanager.docker.SdkDockerService;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ItemHandle;
import org.jfrog.artifactory.client.RepositoryHandle;
import org.jfrog.artifactory.client.model.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.applications.async.task.PublishStatus;
// import com.ericsson.component.aia.sdk.applicationmanager.docker.DockerClientProvider;
import com.ericsson.component.aia.sdk.applicationmanager.services.TestUtils;
import com.ericsson.component.aia.sdk.applicationmanager.services.configuration.ApplicationManagerApplication;
import com.ericsson.component.aia.sdk.applicationmanager.services.configuration.ApplicationManagerTestBeans;
import com.ericsson.component.aia.sdk.applicationmanager.services.configuration.EmbeddedMongoConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.stubs.GogsMockServer;
import com.ericsson.component.aia.sdk.applicationmanager.views.PublishApplicationView;
import com.ericsson.component.aia.sdk.applicationmanager.views.TaskStatusView;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ImageInfo;

/**
 * Integration test for {@link ApplicationManagerEndPoint} publish application endpoint.
 *
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationManagerTestBeans.class, ApplicationManagerApplication.class,
        EmbeddedMongoConfiguration.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class PublishApplicationEndPointIntegrationTest {

    private static GogsMockServer gogsMockServer;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestUtils testUtils;

    //    @Autowired
    //    private DockerClientProvider dockerClientProvider;
    @Autowired
    private SdkDockerService sdkDockerService;

    @BeforeClass
    public static void setupMongo() throws Exception {
        gogsMockServer = new GogsMockServer();
        gogsMockServer.startServer();
    }

    @Test(timeout = 300000)
    public void shouldSuccessfullyPublishApplication() throws Exception {
        gogsMockServer.acceptCreationOfRepo("my-aia-spark-streaming-application");
        setupDockerClientProvider("my-aia-spark-streaming-application");
        mockDockerImageToNotExist();

        final ResponseEntity<PublishApplicationView> response = this.restTemplate.postForEntity("/applications/publish",
                publishEntity("/my-aia-spark-streaming-application-1.0.0.zip"), PublishApplicationView.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));

        final ResponseEntity<TaskStatusView> publishStatus = waitForPublishOperationToFinish(response);
        assertThat(publishStatus.getStatusCodeValue(), equalTo(200));
        assertThat(publishStatus.getBody().getStepDescription(), equalTo(PublishStatus.FINISHED.toString()));
    }

    @Test(timeout = 30000)
    public void shouldPublishApplicationIfDockerImageAlreadyExists() throws Exception {
        gogsMockServer.acceptCreationOfRepo("my-aia-spark-streaming-application");
        setupDockerClientProvider("my-aia-spark-streaming-application");
        mockDockerImageToExist();

        final ResponseEntity<PublishApplicationView> response = this.restTemplate.postForEntity("/applications/publish",
                publishEntity("/my-aia-spark-streaming-application-1.0.0.zip"), PublishApplicationView.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));

        final ResponseEntity<TaskStatusView> publishStatus = waitForPublishOperationToFinish(response);
        assertThat(publishStatus.getStatusCodeValue(), equalTo(200));
        assertThat(publishStatus.getBody().getStepDescription(), equalTo(PublishStatus.FINISHED.toString()));
    }

    @Test(timeout = 30000)
    public void shouldAllowApplicationWithoutExportedDockerToBePublished() throws Exception {
        gogsMockServer.acceptCreationOfRepo("malformed-aia-spark-streaming-application");
        setupDockerClientProvider("malformed-aia-spark-streaming-application");
        mockDockerImageToNotExist();

        final ResponseEntity<PublishApplicationView> response = this.restTemplate.postForEntity("/applications/publish",
                publishEntity("/malformed-aia-spark-streaming-application-1.0.0.zip"), PublishApplicationView.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));

        final ResponseEntity<TaskStatusView> publishStatus = waitForPublishOperationToFinish(response);
        assertThat(publishStatus.getStatusCodeValue(), equalTo(200));
        assertThat(publishStatus.getBody().getStepDescription(), equalTo(PublishStatus.FINISHED.toString()));
    }

    @Test(timeout = 30000)
    public void shouldFailPublishOperationIfProjectHasNoPba() throws Exception {
        gogsMockServer.acceptCreationOfRepo("no-pba-aia-spark-streaming-application");
        setupDockerClientProvider("no-pba-aia-spark-streaming-application");
        mockDockerImageToNotExist();

        final ResponseEntity<PublishApplicationView> response = this.restTemplate.postForEntity("/applications/publish",
                publishEntity("/no-pba-aia-spark-streaming-application-1.0.1.zip"), PublishApplicationView.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));

        final ResponseEntity<TaskStatusView> publishStatus = waitForPublishOperationToFinish(response);
        assertThat(publishStatus.getStatusCodeValue(), equalTo(200));
        assertThat(publishStatus.getBody().getStepDescription(), equalTo(PublishStatus.FAILED.toString()));
    }

    @After
    public void cleanUp() throws MetaDataServiceException {
        testUtils.clearMetaStore();
    }

    @AfterClass
    public static void serverShutdown() {
        gogsMockServer.stopServer();
    }

    private ResponseEntity<TaskStatusView> waitForPublishOperationToFinish(final ResponseEntity<PublishApplicationView> response)
            throws InterruptedException {
        ResponseEntity<TaskStatusView> publishStatus = null;
        boolean finished = false;
        while (!finished) {
            publishStatus = this.restTemplate.getForEntity("/applications/publish/" + response.getBody().getTaskId() + "/status",
                    TaskStatusView.class);
            finished = publishStatus.getBody().isFinished();
            Thread.sleep(2000l);
        }
        return publishStatus;
    }

    private HttpEntity<MultiValueMap<String, Object>> publishEntity(final String applicationZip) {
        final MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("applicationzip", new ClassPathResource(applicationZip, getClass()));
        map.add("version", "1.0.0");
        return new HttpEntity<>(map);
    }

    private void setupDockerClientProvider(final String applicationName) {
        try {
            // final DockerClient mockDockerClient = dockerClientProvider.getDockerClient();
            final DockerClient mockDockerClient = sdkDockerService.getDockerClientProvider().getDockerClient();
            final Set<String> imageName = new HashSet<>();
            imageName.add("my-aia-spark-streaming-application");
            when(mockDockerClient.load(any())).thenReturn(imageName);

            final ImageInfo imageInfo = Mockito.mock(ImageInfo.class);
            when(mockDockerClient.inspectImage(eq(applicationName))).thenReturn(imageInfo);
            when(imageInfo.id()).thenReturn("0123456789");

        } catch (DockerException | InterruptedException e) {

            e.printStackTrace();
        }

    }

    private void mockDockerImageToNotExist() {
        final Artifactory mockArtifactory = sdkDockerService.getDockerClientProvider().getArtifactoryClient();
        Mockito.reset(mockArtifactory);
        final ItemHandle itemHandleMock = Mockito.mock(ItemHandle.class);
        final RepositoryHandle repositoryHandleMock = Mockito.mock(RepositoryHandle.class);

        when(mockArtifactory.repository(eq("docker-v2-global-local"))).thenReturn(repositoryHandleMock);
        when(repositoryHandleMock.file(anyString())).thenReturn(itemHandleMock);

    }

    private void mockDockerImageToExist() {
        final Artifactory mockArtifactory = sdkDockerService.getDockerClientProvider().getArtifactoryClient();
        Mockito.reset(mockArtifactory);
        final File fileMock = Mockito.mock(File.class);
        final ItemHandle itemHandleMock = Mockito.mock(ItemHandle.class);
        final RepositoryHandle repositoryHandleMock = Mockito.mock(RepositoryHandle.class);

        when(mockArtifactory.repository(eq("docker-v2-global-local"))).thenReturn(repositoryHandleMock);
        when(repositoryHandleMock.file(anyString())).thenReturn(itemHandleMock);
        when(itemHandleMock.info()).thenReturn(fileMock);
    }

}
