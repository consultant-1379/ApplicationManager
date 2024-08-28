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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.services.TestUtils;
import com.ericsson.component.aia.sdk.applicationmanager.services.configuration.ApplicationManagerApplication;
import com.ericsson.component.aia.sdk.applicationmanager.services.configuration.ApplicationManagerTestBeans;
import com.ericsson.component.aia.sdk.applicationmanager.services.configuration.EmbeddedMongoConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.services.endpoints.response.ServiceResponse;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration test for {@link ServiceManagerEndPoint}
 *
 * @author eanmerr
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationManagerTestBeans.class, ApplicationManagerApplication.class,
        EmbeddedMongoConfiguration.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ServiceManagerEndPointIntegrationTest {

    private static final String AVAILABLE_SERVICES = "src/test/resources/response/ServiceRepo/services.json";
    private static final String KAFKA_VERSIONS = "src/test/resources/response/ServiceRepo/serviceKafkaVersion.json";
    private static final String KAFKA_9_SERVICE = "src/test/resources/response/ServiceRepo/kafkaService.json";

    private static final String SERVICE_REPO_TEST_ENTRY_1 = "src/test/resources/response/ServiceRepo/ServiceRegistryEntries/Entry1.json";
    private static final String SERVICE_REPO_TEST_ENTRY_2 = "src/test/resources/response/ServiceRepo/ServiceRegistryEntries/Entry2.json";
    private static final String SERVICE_REPO_TEST_ENTRY_3 = "src/test/resources/response/ServiceRepo/ServiceRegistryEntries/Entry3.json";

    private static String UUID_KAFKA_VERSION_8;
    private static String UUID_KAFKA_VERSION_9;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${service.catalog.name}")
    private String serviceCatalog;

    @Autowired
    private TestUtils testUtils;

    @Before
    public void setup() throws MetaDataServiceException, IOException {
        UUID_KAFKA_VERSION_9 = testUtils.createService(SERVICE_REPO_TEST_ENTRY_2);
        UUID_KAFKA_VERSION_8 = testUtils.createService(SERVICE_REPO_TEST_ENTRY_1);
        testUtils.createService(SERVICE_REPO_TEST_ENTRY_3);
    }

    @Test
    public void shouldCreateService() throws Exception {
        final String servicePba = testUtils.getPbaAsString(SERVICE_REPO_TEST_ENTRY_1);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(servicePba, headers);

        final ResponseEntity<String> postResponse = this.restTemplate.exchange("/service", HttpMethod.POST, entity, String.class);
        assertThat(postResponse.getStatusCodeValue(), equalTo(200));

        final ResponseEntity<ServiceResponse> getResponse = this.restTemplate.getForEntity("/services/io/kafka/" + postResponse.getBody(),
                ServiceResponse.class);

        assertThat(getResponse.getStatusCodeValue(), equalTo(200));
        assertNotNull(getResponse.getBody().getData());
    }

    @Test
    public void shouldDeleteService() throws Exception {
        this.restTemplate.delete("/service/" + UUID_KAFKA_VERSION_9);

        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/services/io/kafka/" + UUID_KAFKA_VERSION_9,
                ServiceResponse.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
        assertNull(response.getBody().getData());
    }

    @Test
    public void shouldReturnAListOfallListedServices() throws Exception {
        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/services/io", ServiceResponse.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
        final String responseAsString = objectMapper.writeValueAsString(response.getBody());

        final File expectedResonse = new File(AVAILABLE_SERVICES);
        final String expectedResonseAsString = PBASchemaTool.readStreamAsStringFronFile(expectedResonse.toPath());

        assertEquals(expectedResonseAsString, responseAsString, JSONCompareMode.LENIENT);
    }

    @Test
    public void shouldReturnAListOfallVersionsAvailableForAService() throws Exception {
        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/services/io/kafka", ServiceResponse.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
        final String responseAsString = objectMapper.writeValueAsString(response.getBody());

        final File expectedResonse = new File(KAFKA_9_SERVICE);
        final String expectedResonseAsString = PBASchemaTool.readStreamAsStringFronFile(expectedResonse.toPath())
                .replace("UUID_KAFKA_VERSION_8", UUID_KAFKA_VERSION_8).replace("UUID_KAFKA_VERSION_9", UUID_KAFKA_VERSION_9);

        assertEquals(expectedResonseAsString, responseAsString, JSONCompareMode.LENIENT);
    }

    @Test
    public void shouldReturnTheDefaultValuesAssociatedWithSpecificServiceVersion() throws Exception {
        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/services/io/kafka/" + UUID_KAFKA_VERSION_9,
                ServiceResponse.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
        final String responseAsString = objectMapper.writeValueAsString(response.getBody());

        final File expectedResonse = new File(KAFKA_VERSIONS);
        final String expectedResonseAsString = PBASchemaTool.readStreamAsStringFronFile(expectedResonse.toPath()).replace("UUID_KAFKA_VERSION_9",
                UUID_KAFKA_VERSION_9);
        ;

        assertEquals(expectedResonseAsString, responseAsString, JSONCompareMode.LENIENT);
    }

    @After
    public void cleanUp() throws MetaDataServiceException {
        testUtils.clearMetaStore();
    }

}
