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
package com.ericsson.component.aia.sdk.applicationmanager.api;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;

import com.ericsson.component.aia.sdk.applicationmanager.model.MAASInstance;
import com.ericsson.component.aia.sdk.applicationmanager.views.ApplicationVersionView;
import com.ericsson.component.aia.sdk.applicationmanager.views.PublishApplicationView;
import com.ericsson.component.aia.sdk.applicationmanager.views.PublishedApplicationsView;
import com.ericsson.component.aia.sdk.applicationmanager.views.TaskStatusView;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;

/**
 * The <code>ApplicationManager</code> interface is a generic interface providing all applications related operations like creating, extending,
 * deleting, publishing ..etc.
 */
public interface ApplicationManager {

    /**
     * <p>
     * This method will create a new application and provide a ZipFile to download the application.
     * </p>
     *
     * @param pbaInstance
     *            <p>
     *            of the application containing metadata of the application to be created.
     *            </p>
     * @return
     *         <p>
     *         return path to the ZipFile to download the application
     *         </p>
     */
    Path createApplication(PBAInstance pbaInstance);

    /**
     * <p>
     * This method will extend an existing application and provide the link to download the application.
     * </p>
     *
     * @param pbaId
     *            <p>
     *            ID of the application to be extended
     *            </p>
     *            </p>
     * @param pbaInstance
     *            <p>
     *            of the application containing metadata of the extended application to be created.
     *            </p>
     * @return
     *         <p>
     *         return the ZipFile to download the application
     *         </p>
     */
    Path extendApplication(String pbaId, PBAInstance pbaInstance);

    /**
     * <p>
     * This method will return PBAInstance for requested application name and version.
     * </p>
     *
     * @param pbaId
     *            <p>
     *            ID of the application to fetch from application catalog
     *            </p>
     * @return
     *         <p>
     *         PBAInstance of the application published in the application catalog
     *         </p>
     */
    PBAInstance getPBAInstance(String pbaId);

    /**
     * <p>
     * This method will return collection of published applications.
     * </p>
     *
     * @return
     *         <p>
     *         This method will return a collection of applications published in application catalog
     *         </p>
     */
    Collection<PublishedApplicationsView> listApplications();

    /**
     * List applications matching template.
     *
     * @param string
     *            the string
     * @return the collection
     */
    Collection<PublishedApplicationsView> listApplicationsMatchingTemplate(String string);

    /**
     * Publish a new application zip file.
     *
     * @param input
     *            An InputStream of the application project
     * @param fileName
     *            the name of the application being published
     * @param version
     *            the version of the application being published
     * @return the path
     */
    PublishApplicationView publishApplication(InputStream input, String fileName, String version);

    /**
     * Unpublish the application, delete the docker image of the application and mark the pba in metadata store as inactive.
     *
     * @param pbaId
     *            ID of an application PBA.
     * @return true, if successful
     */
    boolean unPublishApplication(String pbaId);

    /**
     * Delete the application by marking the applications pba as inactive in metadata store .
     *
     * @param pbaId
     *            ID of an application PBA.
     */
    void deleteApplication(String pbaId);

    /**
     * This method will find and return a TaskStatusView containing the current stage of the task associated to the passed in ID.
     *
     * @param taskId
     *            The ID of the task to retrieve.
     * @return TaskStatusView.
     */
    TaskStatusView getTaskStatus(String taskId);

    /**
     * This method will return a path to a zip file of an existing application based on the specified uuid of the application.
     *
     * @param uuid
     *            of the application.
     * @return path to a zip file of an existing application.
     */
    Path getPublishedApplication(String uuid);

    /**
     * <p>
     * This method will remove an application and its related artifacts. It is only for use in development and testing. It is not intended for use by
     * clients of the App SDK. It performs the following cleanup duties:: - Removes the git repo - Removes entries in the Metastore relating to this
     * application - Deletes the Docker image associated with this application
     * </p>
     *
     * @param pbaId
     *            <p>
     *            of the application ID to be deleted
     *            </p>
     * @return
     *         <p>
     *         status of operation
     *         </p>
     */
    CompletionStatus cleanupApplication(String pbaId);

    /**
     * Gets the current version of an application
     *
     * @param applicationName
     *            The name of the application PBA
     * @return The ApplicationVersionView of the current application
     */
    ApplicationVersionView getVersion(String applicationName);

    /**
     * Creates MAAS (mediation as a service) application
     *
     * @param maasInstance
     *            maas instance
     * @return path to a zip file to download application
     */
    Path createMAASApplication(MAASInstance maasInstance);

    /**
     * @param maasJsonStr
     *            - maas json
     * @return - maas instance
     */
    MAASInstance getMAASInstance(String maasJsonStr);

}
