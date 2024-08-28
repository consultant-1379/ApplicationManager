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
package com.ericsson.component.aia.sdk.applicationmanager.util;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class AppSdkUtilTest {

    private Path applicationZipFile = Paths.get("src/test/resources/aia-flink-streaming-1.0.12.zip");

    @Test
    public void shouldReplaceReadMeIfAlreadyExistingInRepo() throws IOException {
        final Path gitRepoPath = Files.createTempDirectory("AppSdkUtilTest_REPO");
        gitRepoPath.resolve("README.md").toFile().createNewFile();

        AppSdkUtil.updateApplicationReadMe(applicationZipFile, gitRepoPath);
        assertTrue(gitRepoPath.resolve("README.md").toFile().exists());

        final String readMeContents = IOUtils.toString(new FileInputStream(gitRepoPath.resolve("README.md").toFile()));
        assertNotEquals("", readMeContents);

        deleteQuietly(gitRepoPath.toFile());
    }

    @Test
    public void shouldAddReadMeIfNotAlreadyExistingInRepo() throws IOException {
        final Path gitRepoPath = Files.createTempDirectory("AppSdkUtilTest_REPO");
        AppSdkUtil.updateApplicationReadMe(applicationZipFile, gitRepoPath);

        assertTrue(gitRepoPath.resolve("README.md").toFile().exists());
        deleteQuietly(gitRepoPath.toFile());
    }

    @Test
    public void shouldExtractRepoNameFromGitUrl() {
        final String repoName = AppSdkUtil.getRepoNameFrom("ssh://git@10.44.149.69:443/root/my-aia-spark-streaming-application.git");
        assertThat(repoName, is("my-aia-spark-streaming-application"));

        final String gerritRepoName = AppSdkUtil
                .getRepoNameFrom("ssh://xxxxx@gerrit.ericsson.se:29418/AIA/com.ericsson.component.aia.sdk/ApplicationManager");
        assertThat(gerritRepoName, is("ApplicationManager"));
    }
}
