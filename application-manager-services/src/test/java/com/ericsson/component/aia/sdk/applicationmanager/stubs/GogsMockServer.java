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
package com.ericsson.component.aia.sdk.applicationmanager.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.Response;

/**
 * Testing stub for tests.
 */
public class GogsMockServer {

    private static final int GOGS_PORT = 10090;
    private static final String GOGS_BASE_URL = "/api/v1";
    private static final String GOGS_ROOT_REPO_URL = GOGS_BASE_URL + "/repos/root/";
    private static final String GOGS_ALL_REPO_URL = GOGS_BASE_URL + "/user/repos";

    private static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().withRootDirectory("src/test/resources").port(GOGS_PORT));
    private static final Path BASE_GOGS_RESPONSE = Paths.get("src/test/resources/__files/GogsResponseJson.json");

    private Set<String> createdRepos = new HashSet<>();

    public String startServer() {
        wireMockServer.start();
        WireMock.configureFor(GOGS_PORT);

        wireMockServer.addMockServiceRequestListener(new RequestListener() {
            @Override
            public void requestReceived(final Request request, final Response response) {
                if (request.getMethod().equals(RequestMethod.POST) && response.getStatus() != 500) {
                    try {
                        addRepo(new JSONObject(request.getBodyAsString()).getString("name"));
                    } catch (final JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return "http://localhost:" + wireMockServer.port() + "/api/v1";
    }

    public void acceptCreationOfRepo(final String repoName) throws IOException {
        final String baseGetResponse = new String(Files.readAllBytes(BASE_GOGS_RESPONSE), UTF_8);
        final String repoResponse = baseGetResponse.replace("REPONAME", repoName);
        wireMockServer.stubFor(post(urlEqualTo(GOGS_ALL_REPO_URL)).willReturn(aResponse().withBody(repoResponse).withStatus(200)));
    }

    public void addRepo(final String repoName) throws IOException {
        final String baseGetResponse = new String(Files.readAllBytes(BASE_GOGS_RESPONSE), UTF_8);
        final String singleRepo = baseGetResponse.replace("REPONAME", repoName);
        final String availableRepos = "[" + singleRepo + "]";
        createdRepos.add(repoName);

        wireMockServer.stubFor(get(urlEqualTo(GOGS_ALL_REPO_URL)).willReturn(aResponse().withBody(availableRepos).withStatus(200)));
        wireMockServer.stubFor(delete(urlEqualTo(GOGS_ROOT_REPO_URL + repoName)).willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(get(urlEqualTo(GOGS_ROOT_REPO_URL + repoName)).willReturn(aResponse().withBody(singleRepo).withStatus(200)));
    }

    public boolean isExistingRepo(final String repoName) {
        return createdRepos.contains(repoName);
    }

    public void stopServer() {
        wireMockServer.stop();
    }
}
