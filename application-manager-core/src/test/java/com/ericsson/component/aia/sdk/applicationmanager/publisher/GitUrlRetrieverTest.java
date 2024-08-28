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
package com.ericsson.component.aia.sdk.applicationmanager.publisher;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.git.project.service.GitProjectService;
import com.ericsson.component.aia.sdk.git.project.service.GitRepoInfo;
import com.ericsson.component.aia.sdk.metadataservice.stub.MetaDataServiceIfcStub;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;

@RunWith(MockitoJUnitRunner.class)
public class GitUrlRetrieverTest {

    @Mock
    private GitProjectService gitProjectService;

    private GitUrlRetriever gitUrlRetriever;
    private final MetaDataServiceIfcStub metaDataServiceManager = new MetaDataServiceIfcStub();
    private final PBASchemaTool pbaSchemaTool = new PBASchemaTool();

    private String level1PbaString;
    private String level2PbaString;
    private String level3PbaString;

    private Pba level1Pba;
    private Pba level2Pba;
    private Pba level3Pba;

    private PublishOperationResult publishOperation;
    private GitRepoInfo parentRepo = new GitRepoInfo("aia-flink-streaming-app", "ssh://gerrit.ericsson.se:29418/aia-flink-streaming-app", "");

    @Before
    public void setup() throws MetaDataServiceException, IOException {
        level1PbaString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("gitRelationshipTest/pba-level-1.json"), "UTF-8");
        level2PbaString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("gitRelationshipTest/pba-level-2.json"), "UTF-8");
        level3PbaString = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("gitRelationshipTest/pba-level-3.json"), "UTF-8");

        ApplicationManagerConfiguration.applicationCatalogName = "aia-application-catalog";

        level1Pba = pbaSchemaTool.getPBAModelInstance(level1PbaString).getPba();
        level2Pba = pbaSchemaTool.getPBAModelInstance(level2PbaString).getPba();
        level3Pba = pbaSchemaTool.getPBAModelInstance(level3PbaString).getPba();

        gitUrlRetriever = new GitUrlRetriever(pbaSchemaTool, metaDataServiceManager, gitProjectService);
        publishOperation = new PublishOperationResult();
        metaDataServiceManager.createSchema("aia-application-catalog");
    }

    @Test
    public void shouldRetrieveParentsScmUrl() throws MetaDataServiceException {
        mockMetaStoreEntries();
        when(gitProjectService.getExistingGitRepository("aia-flink-streaming-app")).thenReturn(of(parentRepo));

        assertThat(gitUrlRetriever.getGitRepoUrl(level2Pba, publishOperation), is("ssh://gerrit.ericsson.se:29418/aia-flink-streaming-app"));
    }

    @Test
    public void shouldRetrieveParentsScmUrlUsingRecursion() throws MetaDataServiceException {
        mockMetaStoreEntries();
        when(gitProjectService.getExistingGitRepository("aia-flink-streaming-app")).thenReturn(of(parentRepo));

        assertThat(gitUrlRetriever.getGitRepoUrl(level3Pba, publishOperation), is("ssh://gerrit.ericsson.se:29418/aia-flink-streaming-app"));
    }

    @Test
    public void shouldCreateGitRepoIfNotPresent() throws MetaDataServiceException {
        mockMetaStoreEntries();
        when(gitProjectService.getExistingGitRepository("aia-flink-streaming-app")).thenReturn(empty());
        when(gitProjectService.createGitRepository(Mockito.eq("aia-flink-streaming-app"), Mockito.anyString())).thenReturn(parentRepo);

        assertThat(gitUrlRetriever.getGitRepoUrl(level1Pba, publishOperation), is("ssh://gerrit.ericsson.se:29418/aia-flink-streaming-app"));
    }

    @Test
    public void shouldReturnExistingGitRepoIfExisting() throws MetaDataServiceException {
        mockMetaStoreEntries();
        when(gitProjectService.getExistingGitRepository("aia-flink-streaming-app")).thenReturn(of(parentRepo));

        assertThat(gitUrlRetriever.getGitRepoUrl(level1Pba, publishOperation), is("ssh://gerrit.ericsson.se:29418/aia-flink-streaming-app"));
    }

    @Test(expected = ApplicationManagerException.class)
    public void shouldThrowExceptionIfParentIsNotInMetaStore() throws MetaDataServiceException {
        mockOnlyChildPba();
        metaDataServiceManager.delete("aia-application-catalog", "652e1e6-4e5bfea-73b39fe");

        gitUrlRetriever.getGitRepoUrl(level2Pba, publishOperation);
    }

    @Test(expected = ApplicationManagerException.class)
    public void shouldThrowExceptionIfParentRepoDoesNotExist() throws MetaDataServiceException {
        mockMetaStoreEntries();
        when(gitProjectService.getExistingGitRepository("aia-flink-streaming-app")).thenReturn(empty());
        gitUrlRetriever.getGitRepoUrl(level2Pba, publishOperation);
    }

    private void mockMetaStoreEntries() throws MetaDataServiceException {

        mockOnlyChildPba();
        metaDataServiceManager.put("aia-application-catalog", "153e1e1-4e51fea-53a397e", level2PbaString);
        metaDataServiceManager.put("aia-application-catalog", "a1f211b-4e5bfea-53ab13e", level1PbaString);
    }

    private void mockOnlyChildPba() throws MetaDataServiceException {
        metaDataServiceManager.put("aia-application-catalog", "652e1e6-4e5bfea-73b39fe", level3PbaString);
    }
}
