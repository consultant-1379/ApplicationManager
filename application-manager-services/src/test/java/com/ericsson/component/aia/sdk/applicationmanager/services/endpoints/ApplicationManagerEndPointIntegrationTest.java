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

import static com.ericsson.component.aia.sdk.applicationmanager.services.configuration.TestConstants.TEST_PBA_JSON_ACTIVE;
import static com.ericsson.component.aia.sdk.applicationmanager.services.configuration.TestConstants.TEST_PBA_JSON_ACTIVE_V2;
import static com.ericsson.component.aia.sdk.applicationmanager.services.configuration.TestConstants.TEST_PBA_JSON_INACTIVE;
import static com.ericsson.component.aia.sdk.applicationmanager.services.configuration.TestConstants.TEST_TEMPLATE_PBA_JSON_V2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ItemHandle;
import org.jfrog.artifactory.client.RepositoryHandle;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
// import com.ericsson.component.aia.sdk.applicationmanager.docker.DockerClientProvider;
import com.ericsson.component.aia.sdk.applicationmanager.services.TestUtils;
import com.ericsson.component.aia.sdk.applicationmanager.services.configuration.ApplicationManagerApplication;
import com.ericsson.component.aia.sdk.applicationmanager.services.configuration.ApplicationManagerTestBeans;
import com.ericsson.component.aia.sdk.applicationmanager.services.configuration.EmbeddedMongoConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.services.endpoints.response.ServiceResponse;
import com.ericsson.component.aia.sdk.applicationmanager.views.ApplicationVersionView;
import com.ericsson.component.aia.sdk.applicationmanager.views.PublishedApplicationsView;
import com.ericsson.component.aia.sdk.pba.exception.InvalidPbaException;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.util.docker.DockerClientProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

/**
 * Integration test for {@link ApplicationManagerEndPoint}
 *
 * @author echchik
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationManagerTestBeans.class, ApplicationManagerApplication.class,
        EmbeddedMongoConfiguration.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ApplicationManagerEndPointIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DockerClientProvider dockerClientProvider;

    private final PBASchemaTool pbaSchemaTool = new PBASchemaTool();

    private static final Configuration configuration = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider()).build();

    @Test
    public void shouldGetCurrentApplicationVersion() throws Exception {
        testUtils.createApplication(TEST_PBA_JSON_ACTIVE);
        testUtils.createApplication(TEST_PBA_JSON_ACTIVE_V2);

        final ResponseEntity<ApplicationVersionView> response = this.restTemplate.getForEntity("/applications/aia-flink-streaming-app/version",
                ApplicationVersionView.class);

        assertThat(response.getStatusCodeValue(), equalTo(200));

        final ApplicationVersionView applications = response.getBody();
        assertThat(applications.isNewApplication(), is(false));
        assertThat(applications.getMaxVersion(), is("1.0.2"));
        assertThat(applications.getVersions(), contains("1.0.2", "1.0.0"));
    }

    @Test
    public void shouldGetBlankVersionIfNoApplicationExists() throws Exception {
        testUtils.createApplication(TEST_PBA_JSON_ACTIVE);
        testUtils.createApplication(TEST_PBA_JSON_ACTIVE_V2);

        final ResponseEntity<ApplicationVersionView> response = this.restTemplate.getForEntity("/applications/aia-spark-streaming-app/version",
                ApplicationVersionView.class);

        assertThat(response.getStatusCodeValue(), equalTo(200));

        final ApplicationVersionView applications = response.getBody();
        assertThat(applications.isNewApplication(), is(true));
        assertThat(applications.getMaxVersion(), is("0.0.0"));
        assertThat(applications.getVersions(), empty());
    }

    @Test
    public void shouldMarkApplicationAsInactiveWhenDeleted() throws Exception {
        final String pbaId = testUtils.createApplication(TEST_PBA_JSON_ACTIVE);
        this.restTemplate.delete("/applications/" + pbaId);

        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/applications", ServiceResponse.class);
        final Collection<PublishedApplicationsView> applications = (Collection<PublishedApplicationsView>) response.getBody().getData();

        assertThat(response.getStatusCodeValue(), equalTo(200));
    }

    @Test
    public void shouldMarkApplicationAsInactiveAndDeleteDockerWhenUnpublished() throws Exception {

        final Artifactory mockArtifactory = dockerClientProvider.getArtifactoryClient();
        Mockito.reset(mockArtifactory);
        final org.jfrog.artifactory.client.model.File fileMock = Mockito.mock(org.jfrog.artifactory.client.model.File.class);
        final ItemHandle itemHandleMock = Mockito.mock(ItemHandle.class);
        final RepositoryHandle repositoryHandleMock = Mockito.mock(RepositoryHandle.class);

        when(mockArtifactory.repository(eq("docker-v2-global-local"))).thenReturn(repositoryHandleMock);
        when(repositoryHandleMock.file(anyString())).thenReturn(itemHandleMock);
        when(itemHandleMock.info()).thenReturn(fileMock);

        final String pbaId = testUtils.createApplication(TEST_PBA_JSON_ACTIVE);
        this.restTemplate.delete("/applications/publish/" + pbaId);

        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/applications", ServiceResponse.class);
        final Collection applications = (Collection) response.getBody().getData();

        assertThat(response.getStatusCodeValue(), equalTo(200));
    }

    @Test
    public void shouldReturnOctectStreamForDownloadOfPublishedApplication() throws Exception {
        final String pbaId = testUtils.createApplication(TEST_PBA_JSON_ACTIVE);
        final ResponseEntity<InputStreamResource> response = this.restTemplate.getForEntity("/applications/" + pbaId + "/zip",
                InputStreamResource.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
        assertThat(response.getBody(), notNullValue());
    }

    @Test
    public void shouldReturnErrorForDownloadOfApplicationWhichDoesntExist() throws Exception {
        final ResponseEntity<InputStreamResource> response = this.restTemplate.getForEntity("/applications/12345/zip", InputStreamResource.class);
        assertThat(response.getStatusCodeValue(), equalTo(500));
        assertThat(response.getBody(), notNullValue());
    }

    @Test
    public void shouldReturnResponseEntityWhenListApplicationsIsSuccessful() throws Exception {
        testUtils.createApplication(TEST_PBA_JSON_INACTIVE);
        final String pbaId = testUtils.createApplication(TEST_PBA_JSON_ACTIVE);

        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/applications", ServiceResponse.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
        final String responseAsString = objectMapper.writeValueAsString(response.getBody());

        final File expectedResonse = new File(
                "src/test/resources/response/integration-test/shouldReturnResponseEntityWhenListApplicationsIsSuccessful-expected.json");
        String expectedResonseAsString = PBASchemaTool.readStreamAsStringFronFile(expectedResonse.toPath());

        expectedResonseAsString = JsonPath.using(configuration).parse(expectedResonseAsString).set("$.data[*].versions[*].id", pbaId).json()
                .toString();

        assertEquals(expectedResonseAsString, responseAsString, JSONCompareMode.LENIENT);
    }

    @Test
    public void shouldReturnFilteredResponseEntityWhenListApplicationsIsSuccessful() throws Exception {
        testUtils.createApplication(TEST_PBA_JSON_INACTIVE);
        testUtils.createApplication(TEST_PBA_JSON_ACTIVE_V2);
        final String pbaId = testUtils.createApplication(TEST_PBA_JSON_ACTIVE);
        final String templateId = testUtils.createTemplate(TEST_TEMPLATE_PBA_JSON_V2);

        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/applications?templateId=" + templateId,
                ServiceResponse.class);

        assertThat(response.getStatusCodeValue(), equalTo(200));
        final String responseAsString = objectMapper.writeValueAsString(response.getBody());

        final File expectedResonse = new File(
                "src/test/resources/response/integration-test/shouldReturnFilteredResponseEntityWhenListApplicationsIsSuccessful-expected.json");
        String expectedResonseAsString = PBASchemaTool.readStreamAsStringFronFile(expectedResonse.toPath());

        expectedResonseAsString = JsonPath.using(configuration).parse(expectedResonseAsString).set("$.data[*].versions[*].id", pbaId).json()
                .toString();

        assertEquals(expectedResonseAsString, responseAsString, JSONCompareMode.LENIENT);
    }

    @Test
    public void shouldReturnResponseEntityWhenGetApplicationIsSuccessful() throws Exception {
        final String pbaId = testUtils.createApplication(TEST_PBA_JSON_ACTIVE);

        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/applications/" + pbaId, ServiceResponse.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
        final ServiceResponse<PBAInstance> pbaInstance = response.getBody();
        final String responseAsString = objectMapper.writeValueAsString(pbaInstance.getData());
        assertEquals(getPbaAsStringWithIdUpdated(pbaId), responseAsString, false);
    }

    private String getPbaAsStringWithIdUpdated(final String pbaId) throws IOException {
        try {
            final File pbaJson = new File(TEST_PBA_JSON_ACTIVE);

            final String pbaAsString = PBASchemaTool.readStreamAsStringFronFile(pbaJson.toPath());
            final PBAInstance pbaInstance = pbaSchemaTool.getPBAModelInstance(pbaAsString);
            if (pbaInstance.getPba() == null || pbaInstance.getPba().getApplicationInfo() == null) {
                throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, "Pba application information is invalid");
            }
            pbaInstance.getPba().getApplicationInfo().setId(pbaId);
            return pbaSchemaTool.convertToJsonString(pbaInstance);
        } catch (final InvalidPbaException ex) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, ex);
        }
    }

    @After
    public void cleanUp() throws MetaDataServiceException {
        testUtils.clearMetaStore();
    }

}
