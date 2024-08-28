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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ericsson.component.aia.sdk.git.exceptions.SdkGitException;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;

/**
 * Testing stub for tests.
 */
public class GitSshServiceStub extends GitSshService {
    @Override
    public boolean checkGitRepoExists(final String gitRepoUri) throws SdkGitException {
        return true;
    }

    @Override
    public Path clone(final String gitRepoUri, final String dirName) throws SdkGitException {
        Path gitFolder = null;
        try {
            gitFolder = Paths.get("target", "TEST_REPO_" + System.currentTimeMillis());
            if (!Files.exists(gitFolder)) {
                final InputStream inputStream = getClass().getResourceAsStream("/my-aia-spark-streaming-application-1.0.0.zip");
                Files.createDirectories(gitFolder);

                final Path output = gitFolder.resolve("my-aia-spark-streaming-application-1.0.0.zip");
                if (!Files.exists(output)) {
                    Files.copy(inputStream, output);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return gitFolder;
    }

    @Override
    public void pushToGitRepo(final Path gitRepo, final String name, final String tag, final String email, final String message)
            throws SdkGitException {
    }

    @Override
    public void checkout(final Path gitDir, final String name) {
    }

    @Override
    public void checkoutBranch(final Path gitDir, final String name) {

    }
}
