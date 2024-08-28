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

import java.nio.file.Path;
import java.util.Optional;

import com.ericsson.component.aia.sdk.pba.model.Docker;

/**
 * This class is used to maintain data about regarding a publish operation. It is used by the revert functionality to revert the appropriate
 * operations if a publish operation fails.
 */
public class PublishOperationResult {

    private Optional<Path> newGitRepo = Optional.empty();
    private Optional<String> possibleScmUrl = Optional.empty();
    private Optional<Docker> publishedDockerImage = Optional.empty();
    private Optional<String> applicationPbaId = Optional.empty();

    /**
     * This method is used to specify the path of a remote Git repository has been cloned
     *
     * @param gitRepo
     *            The path of the local copy of the Git repository.
     */
    public void localGitRepo(final Path gitRepo) {
        this.newGitRepo = Optional.of(gitRepo);
    }

    /**
     * Method used to specify the URL of new remote Git repository.
     *
     * @param scmUrl
     *            The ssh scm URL of the created Git repository
     */
    public void createdGitRepoUrl(final String scmUrl) {
        this.possibleScmUrl = Optional.of(scmUrl);
    }

    /**
     * Method used to specify the ID of a new PBA which was added to meta data store.
     *
     * @param pbaId
     *            The ID of the data in meta store
     */
    public void pbaAddedToMetaStore(final String pbaId) {
        this.applicationPbaId = Optional.of(pbaId);
    }

    /**
     * Method used to specify the details of a new docker image.
     *
     * @param dockerImage
     *            The {@link Docker } which represents the image.
     */
    public void publishedDockerImage(final Docker dockerImage) {
        this.publishedDockerImage = Optional.of(dockerImage);
    }

    /**
     * This method returns an Optional object a value will be present if a local Git repository was created.
     *
     * @return {@link Optional<Path>}
     */
    public Optional<Path> getLocalGitRepo() {
        return newGitRepo;
    }

    /**
     * This method returns an Optional object a value will be present if a remote Git repository was created.
     *
     * @return {@link Optional<String>}
     */
    public Optional<String> getRemoteScmUrl() {
        return possibleScmUrl;
    }

    /**
     * This method returns an Optional object a value will be present if a docker image was pushed to Artifactory.
     *
     * @return {@link Optional<Docker>}
     */
    public Optional<Docker> getPublishedDockerImage() {
        return publishedDockerImage;
    }

    /**
     * This method returns an Optional object a value will be present if a PBA was added to meta data store.
     *
     * @return {@link Optional<String>}
     */
    public Optional<String> getPbaId() {
        return applicationPbaId;
    }

}
