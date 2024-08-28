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
package com.ericsson.component.aia.sdk.applicationmanager.publisher;

import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.UTF_8_ENCODING;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConstants.HYPHEN;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.applicationmanager.util.AppSdkUtil;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.model.AuthorInfo;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;

/**
 * The Class GitPushService.
 */
public class GitPushService {

    private final PBASchemaTool pbaSchemaTool;
    private final GitSshService gitSSHService;

    /**
     * Instantiates a new git repo application comitter.
     *
     * @param pbaSchemaTool
     *            the pba schema tool
     * @param gitSSHService
     *            the git SSH service
     */
    public GitPushService(final PBASchemaTool pbaSchemaTool, final GitSshService gitSSHService) {
        this.pbaSchemaTool = pbaSchemaTool;
        this.gitSSHService = gitSSHService;
    }

    /**
     * Update project pba and push to git repo.
     *
     * @param applicationPath
     *            the publishing application path
     * @param pbaModel
     *            the pba model
     * @param newGitRepo
     *            the new git repo
     * @throws IOException
     *             Thrown when application zip cannot be copied to the local Git repository.
     */
    public void updateAndPush(final Path applicationPath, final PBAInstance pbaModel, final Path newGitRepo) throws IOException {

        final String applicationName = pbaModel.getPba().getApplicationInfo().getName();
        final String applicationVersion = pbaModel.getPba().getApplicationInfo().getVersion();
        final String applicationId = pbaModel.getPba().getApplicationInfo().getId();

        updatePbaInZipFile(applicationPath, pbaSchemaTool.convertToJsonString(pbaModel));

        final String commitMessage = String.format("Publishing Application:%s Version:%s Id:%s", applicationName, applicationVersion, applicationId);
        final AuthorInfo authorInfo = pbaModel.getPba().getAuthorInfo();

        final String author = authorInfo.getAuthor();
        final String email = authorInfo.getEmail();
        final String tag = applicationName + HYPHEN + applicationVersion;

        Files.copy(applicationPath, newGitRepo.resolve(applicationPath.getFileName()), StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES);

        gitSSHService.pushToGitRepo(newGitRepo, author, tag, email, commitMessage);
    }

    /**
     * Update pba in zip file.
     *
     * @param publishingApplicationPath
     *            the publishing application path
     * @param pbaAsString
     *            the pba as string
     */
    private void updatePbaInZipFile(final Path publishingApplicationPath, final String pbaAsString) {
        try (final FileSystem zipfs = FileSystems.newFileSystem(publishingApplicationPath, this.getClass().getClassLoader())) {
            for (final Path root : zipfs.getRootDirectories()) {
                final Optional<Path> possiblePbaPath = AppSdkUtil.findPba(root);

                if (possiblePbaPath.isPresent()) {
                    try (InputStream pbaStream = IOUtils.toInputStream(pbaAsString, UTF_8_ENCODING)) {
                        Files.copy(pbaStream, possiblePbaPath.get(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    return;
                }
            }
        } catch (final IOException exp) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_EXTRACTING_APPLICATION_DATA,
                    "Exception occurred, when trying to extract data from application being published", exp);
        }
        throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_NOT_FOUND, "Unable to locate PBA within zip file");
    }

}
