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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

/**
 * Integration test for {@link ApplicationManagerEndPoint}
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ApplicationManagerTestBeans.class, ApplicationManagerApplication.class,
        EmbeddedMongoConfiguration.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ExtendApplicationEndPointIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestUtils testUtils;

    private static final Configuration configuration = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider()).build();

    @Test
    public void shouldReturnOkStatusWhenExtendApplicationIsSuccessful() throws Exception {
        final String applicationPbaId = testUtils.createApplication(TEST_PBA_JSON_ACTIVE);
        final File pbaJson = new File(TEST_PBA_JSON_ACTIVE);
        String pbaAsString = PBASchemaTool.readStreamAsStringFronFile(pbaJson.toPath());
        pbaAsString = JsonPath.using(configuration).parse(pbaAsString).set("$.pba.applicationInfo.id", applicationPbaId).json().toString();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(pbaAsString, headers);

        final ResponseEntity<String> response = this.restTemplate.exchange("/applications/extend", HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
    }

    @Test
    public void shouldReturnErrorWhenExtendApplicationPbaIsNull() throws Exception {
        final String applicationPbaId = testUtils.createApplication(TEST_PBA_JSON_ACTIVE);
        final File pbaJson = new File(TEST_PBA_JSON_ACTIVE);
        String pbaAsString = PBASchemaTool.readStreamAsStringFronFile(pbaJson.toPath());
        pbaAsString = JsonPath.using(configuration).parse(pbaAsString).set("$.pba.applicationInfo.id", applicationPbaId).json().toString();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(headers);

        final ResponseEntity<String> response = this.restTemplate.exchange("/applications/extend", HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCodeValue(), equalTo(500));
    }

    @Test
    public void shouldReturnErrorWhenExtendingApplicationThatDoesntExist() throws Exception {
        final File pbaJson = new File(TEST_PBA_JSON_ACTIVE);
        final String pbaAsString = PBASchemaTool.readStreamAsStringFronFile(pbaJson.toPath());

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(pbaAsString, headers);

        final ResponseEntity<String> response = this.restTemplate.exchange("/applications/extend", HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCodeValue(), equalTo(500));
    }

    @After
    public void cleanUp() throws MetaDataServiceException {
        testUtils.clearMetaStore();
    }

}
