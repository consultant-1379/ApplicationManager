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
package com.ericsson.component.aia.sdk.applicationmanager.applications;

import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.SDK_DELIMITER;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration.applicationCatalogName;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration.templateCatalogName;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConstants.APPLICATION_PBA_NAME;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.aia.metadata.model.MetaData;
import com.ericsson.component.aia.sdk.applicationmanager.api.ApplicationManager;
import com.ericsson.component.aia.sdk.applicationmanager.api.CompletionStatus;
import com.ericsson.component.aia.sdk.applicationmanager.applications.generator.ApplicationGenerator;
import com.ericsson.component.aia.sdk.applicationmanager.cleanup.ApplicationCleanup;
import com.ericsson.component.aia.sdk.applicationmanager.cleanup.ApplicationCleanupBuilder;
import com.ericsson.component.aia.sdk.applicationmanager.common.FileExtensions;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConstants;
import com.ericsson.component.aia.sdk.applicationmanager.converters.PbaToListViewConverter;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.applicationmanager.maas.util.MAASSchemaTool;
import com.ericsson.component.aia.sdk.applicationmanager.model.MAASInstance;
import com.ericsson.component.aia.sdk.applicationmanager.publisher.ApplicationPublisher;
import com.ericsson.component.aia.sdk.applicationmanager.publisher.ApplicationPublisherBuilder;
import com.ericsson.component.aia.sdk.applicationmanager.service.views.ApplicationPath;
import com.ericsson.component.aia.sdk.applicationmanager.util.AppSdkUtil;
import com.ericsson.component.aia.sdk.applicationmanager.views.ApplicationVersionView;
import com.ericsson.component.aia.sdk.applicationmanager.views.PublishApplicationView;
import com.ericsson.component.aia.sdk.applicationmanager.views.PublishedApplicationsView;
import com.ericsson.component.aia.sdk.applicationmanager.views.TaskStatusView;
import com.ericsson.component.aia.sdk.git.exceptions.SdkGitException;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;
import com.ericsson.component.aia.sdk.pba.model.ScmInfo;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;

/**
 * The <code>ApplicationManagerImpl</code> was implementation class for all the ApplicationManager's related operations.
 */
public class ApplicationManagerImpl implements ApplicationManager {

    private static final Logger Log = LoggerFactory.getLogger(ApplicationManagerImpl.class);
    private static final MAASSchemaTool maasSchemaTool = new MAASSchemaTool();
    private static final String MAAS_TEMPLATE_IN_CLASSPATH = "config/MAASBlankTemplate.zip";
    private static final long CHECK_SESSION = 300L;
    private static final long APP_EXPIRATION = 86400L;
    private final MetaDataServiceIfc metaDataServiceManager;
    private final PBASchemaTool pbaSchemaTool;

    private final ApplicationGenerator applicationGenerator;
    private final ApplicationPublisher applicationPublisher;
    private final ApplicationCleanup applicationCleanup;

    private final Path cachedApplicationPath;

    private final GitSshService gitSshService;

    private final ScheduledThreadPoolExecutor applicationScannerScheduler;

    /**
     * Instantiates a new application manager impl.
     *
     * @param templateManager
     *            the template manager
     * @param metaDataServiceManager
     *            the meta data service manager
     * @param pbaSchemaTool
     *            the pba schema tool
     * @param dockerService
     *            the docker repo client
     * @param gitSshService
     *            the git read only SSH repository service
     */
    ApplicationManagerImpl(final TemplateManager templateManager, final MetaDataServiceIfc metaDataServiceManager, final PBASchemaTool pbaSchemaTool,
                           final SdkDockerService dockerService, final GitSshService gitSshService) {

        this.metaDataServiceManager = metaDataServiceManager;
        this.pbaSchemaTool = pbaSchemaTool;
        this.gitSshService = gitSshService;
        this.applicationGenerator = new ApplicationGenerator(templateManager, pbaSchemaTool);

        this.applicationPublisher = ApplicationPublisherBuilder.builder().pbaSchemaTool(pbaSchemaTool).dockerRepoClient(dockerService)
                .gitSshService(gitSshService).metaDataServiceManager(metaDataServiceManager).build();

        this.applicationCleanup = ApplicationCleanupBuilder.builder().pbaSchemaTool(pbaSchemaTool).sdkDockerService(dockerService)
                .metaDataServiceManager(metaDataServiceManager).build();

        this.cachedApplicationPath = AppSdkUtil.createFolderIfNotExists(ApplicationManagerConstants.SDK_APPLICATION_CACHE_FOLDER);

        this.applicationScannerScheduler = new ScheduledThreadPoolExecutor(1);

        this.applicationScannerScheduler.scheduleWithFixedDelay(() -> checkExpiredApplications(), CHECK_SESSION, CHECK_SESSION, TimeUnit.SECONDS);

    }

    @Override
    public Path createApplication(final PBAInstance pbaInstance) {
        Log.info("Creating application for pba with application name:: {}", pbaInstance.getPba().getApplicationInfo().getName());
        final Path newApplication = applicationGenerator.createApplication(pbaInstance);

        Log.info("Successfully created application at {}", newApplication.toAbsolutePath());
        return newApplication;
    }

    @Override
    public PBAInstance getPBAInstance(final String pbaId) {
        Log.trace("Retrieving pbaInstance for application with ID {} ", pbaId);
        try {
            final String pbaAsString = metaDataServiceManager.get(applicationCatalogName, pbaId);
            return pbaSchemaTool.getPBAModelInstance(pbaAsString);
        } catch (final MetaDataServiceException exp) {
            final String errMsg = String.format("Application with ID %s not available in catalog %s", pbaId, applicationCatalogName);
            Log.error(errMsg, exp);
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.APPLICATION_NOT_FOUND, errMsg, exp);
        }
    }

    @Override
    public Collection<PublishedApplicationsView> listApplications() {
        Log.trace("Retrieving all applications from metadata model service");
        final PbaToListViewConverter pbaToListViewConverter = new PbaToListViewConverter();

        try {
            for (final MetaData metaData : metaDataServiceManager.findByPropertyValue(applicationCatalogName, "pba.status", "ACTIVE")) {
                if (metaData.getValue() == null || metaData.getValue().trim().isEmpty()) {
                    continue;
                }
                pbaToListViewConverter.addPbaInstance(pbaSchemaTool.getPBAModelInstance(metaData.getValue()));
            }

        } catch (final MetaDataServiceException e) {
            Log.error("exception occurred, when trying to retrieve application list from metadata service", e);
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_LIST, e);
        }

        Log.trace("Successfully retrieved list of all applications");
        return pbaToListViewConverter.getPbaListViews();
    }

    @Override
    public Collection<PublishedApplicationsView> listApplicationsMatchingTemplate(final String templateId) {
        Log.trace("Retrieving all applications from metadata model service");
        final PbaToListViewConverter pbaToListViewConverter = new PbaToListViewConverter();
        try {
            final PBAInstance pbaInstance = pbaSchemaTool.getPBAModelInstance(metaDataServiceManager.get(templateCatalogName, templateId));
            final Set<String> allowedTechnology = getSupportedIntegrationTech(pbaInstance);

            metaDataServiceManager.findByPropertyValue(applicationCatalogName, "pba.status", "ACTIVE").forEach(metaData -> {
                if (metaData.getValue() != null && !metaData.getValue().trim().isEmpty()) {
                    pbaToListViewConverter.addPbaInstance(pbaSchemaTool.getPBAModelInstance(metaData.getValue()), allowedTechnology);
                }
            });

        } catch (final MetaDataServiceException e) {
            Log.error("exception occurred, when trying to retrieve application list from metadata service", e);
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_LIST, e);
        }

        Log.trace("Successfully retrieved list of all applications");
        return pbaToListViewConverter.getPbaListViews();
    }

    private Set<String> getSupportedIntegrationTech(final PBAInstance pbaInstance) {
        final Set<String> supportedTechnology = new HashSet<>();
        pbaInstance.getPba().getExtensionPoints().forEach(extensionPoint -> {
            supportedTechnology.add(extensionPoint.getTechnology());
        });
        return supportedTechnology;
    }

    @Override
    public PublishApplicationView publishApplication(final InputStream input, final String fileName, final String applicationVersion) {
        final String taskId = applicationPublisher.publishApplication(input, fileName, applicationVersion);
        return new PublishApplicationView(taskId);
    }

    @Override
    public void deleteApplication(final String pbaId) {
        Log.info("Delete Application method invoked  with ID {} ", pbaId);
        updatePbaStatusToInactive(pbaId);
    }

    @Override
    public boolean unPublishApplication(final String pbaId) {
        Log.trace("Un-Publishing Application method invoked  with ID {} ", pbaId);
        updatePbaStatusToInactive(pbaId);
        return true;
    }

    private PBAInstance updatePbaStatusToInactive(final String pbaId) {
        Log.trace("Update PBA status to inactive Application method invoked  with ID {} ", pbaId);
        final PBAInstance pbaModel = getPBAInstance(pbaId);
        pbaModel.getPba().setStatus("INACTIVE");
        final String pbaModelString = pbaSchemaTool.convertToJsonString(pbaModel);

        try {
            metaDataServiceManager.update(applicationCatalogName, pbaId, pbaModelString);
        } catch (final MetaDataServiceException exp) {
            final String errMsg = String.format("Delete operation failed for template with ID %s ", pbaId);
            Log.error(errMsg, exp);
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_DELETE, errMsg, exp);
        }
        return pbaModel;
    }

    @Override
    public CompletionStatus cleanupApplication(final String pbaId) {
        return applicationCleanup.cleanupApplication(pbaId);
    }

    // TODO move function out of this class
    @Override
    public Path extendApplication(final String pbaId, final PBAInstance extendedPbaInstance) {
        Log.trace("Extending application for ID {} ", pbaId);
        try {
            final String pbaAsString = metaDataServiceManager.get(applicationCatalogName, pbaId);
            if (pbaAsString == null) {
                throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_NOT_FOUND,
                        String.format("Pba not found for application with ID %s", pbaId));
            }
            final PBAInstance pbaInstance = pbaSchemaTool.getPBAModelInstance(pbaAsString);
            final Path baseApplicationRepo = checkoutRepo(pbaInstance);
            final Path baseApplicationZip = Files.walk(baseApplicationRepo).filter(path -> (path.toString().toLowerCase().endsWith(".zip")))
                    .findFirst().get();

            final Path tempExtendedApplicationZip = createExtendedApplicationZip(extendedPbaInstance.getPba(), baseApplicationZip);

            final PbaInfo extendedApplicationInfo = extendedPbaInstance.getPba().getApplicationInfo();
            final String applicationName = WordUtils.capitalize(extendedApplicationInfo.getTitle()).replaceAll("\\s+", "");
            extendedApplicationInfo.setName(applicationName);
            extendedApplicationInfo.setVersion(ApplicationManagerConstants.DEFAULT_STARTING_VERSION);

            applicationGenerator.addExtendedApplicationConfigurations(pbaInstance, extendedPbaInstance, tempExtendedApplicationZip);

            Log.trace("Successfully created application at {} location", tempExtendedApplicationZip);
            return tempExtendedApplicationZip;
        } catch (final MetaDataServiceException e) {
            Log.error("Exception occurred, when trying to retrieve application list from metadata service", e);
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_LIST_APPLICATION, e);
        } catch (final IOException e) {
            Log.error("Exception occurred, when trying to access application files", e);
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.APPLICATION_FILE_IS_CORRUPTED, e);
        }

    }

    private Path createExtendedApplicationZip(final Pba extendedApplicationPba, final Path applicationToExtend) {
        final String applicationName = extendedApplicationPba.getApplicationInfo().getName();
        try {

            final Path tempDirectory = Files.createTempDirectory(applicationName + SDK_DELIMITER + System.currentTimeMillis());
            final String extendedApplicationZipFileName = applicationName + FileExtensions.ZIP.getExtensionType();
            final Path extendedApplication = tempDirectory.resolve(extendedApplicationZipFileName);
            Files.copy(applicationToExtend, extendedApplication);
            return extendedApplication;

        } catch (final IOException e) {
            Log.error("Exception occurred, when trying to create copy of base application zip file during extend operation for application::{} ",
                    applicationName, e);
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_CLONING_APPLICATION_ON_FILESYSTEM, e);
        }
    }

    @Override
    public TaskStatusView getTaskStatus(final String taskId) {
        return applicationPublisher.getStatus(taskId);
    }

    @Override
    public Path getPublishedApplication(final String pbaId) {

        Path checkout = null;

        try {
            final String pbaAsString = metaDataServiceManager.get(applicationCatalogName, pbaId);
            if (pbaAsString == null) {
                throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_NOT_FOUND,
                        String.format("Pba not found for application with ID %s", pbaId));
            }

            final PBAInstance pbaInstance = pbaSchemaTool.getPBAModelInstance(pbaAsString);
            checkout = checkoutRepo(pbaInstance);
            final Path file = Files.walk(checkout).filter(path -> (path.toString().toLowerCase().endsWith(".zip"))).findFirst().get();
            if (file != null) {
                try {
                    FileUtils.copyFileToDirectory(file.toFile(), cachedApplicationPath.toFile());
                    return Paths.get(cachedApplicationPath.toFile().getAbsolutePath() + "/" + file.toFile().getName());
                } catch (final IOException e) {
                    Log.error("exception occurred, when trying to copy zip to temp directory", e);
                    throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_CLONING_APPLICATION_ON_FILESYSTEM, e);
                }

            }

        } catch (final IOException | MetaDataServiceException e) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_PUBLISHED, e);
        } finally {
            if (checkout != null) {
                try {
                    FileUtils.forceDelete(checkout.toFile());
                } catch (final Exception ex) {
                    Log.error(ex.getMessage(), ex);
                }
            }
        }

        return null;

    }

    @Override
    public ApplicationVersionView getVersion(final String applicationName) {
        try {
            final ApplicationVersionView applicationVersionView = new ApplicationVersionView();
            final List<MetaData> applicationVersions = metaDataServiceManager.findByPropertyValue(applicationCatalogName, APPLICATION_PBA_NAME,
                    applicationName);

            final List<String> versions = new ArrayList<>();
            for (final MetaData applicationVersion : applicationVersions) {
                versions.add(getVersion(applicationVersion));
            }

            Collections.sort(versions, Collections.reverseOrder());
            if (!versions.isEmpty()) {
                applicationVersionView.setMaxVersion(versions.get(0));
                applicationVersionView.setVersions(versions);
                applicationVersionView.setNewApplication(false);
            }

            return applicationVersionView;

        } catch (final MetaDataServiceException e) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_GET, e);
        }
    }

    private String getVersion(final MetaData applicationVersion) {
        final PbaInfo applicationInfo = pbaSchemaTool.getPBAModelInstance(applicationVersion.getValue()).getPba().getApplicationInfo();
        return applicationInfo.getVersion();
    }

    /**
     * Get checkout path of the cloned repository.
     *
     * @param pbaInstance
     *            the pba instance
     * @return the path
     */
    private Path checkoutRepo(final PBAInstance pbaInstance) {
        try {
            final ScmInfo scmInfo = pbaInstance.getPba().getScmInfo();
            final Path applicationRepo = gitSshService.clone(scmInfo.getScm(), pbaInstance.getPba().getApplicationInfo().getName());
            gitSshService.checkout(applicationRepo, scmInfo.getScmTag());
            return applicationRepo;
        } catch (final SdkGitException e) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_ACCESSING_GIT_REPOSITORY,
                    "exception occurred, when trying to extendApplication " + pbaInstance.getPba().getApplicationInfo().getName() + SDK_DELIMITER
                            + pbaInstance.getPba().getApplicationInfo().getVersion() + " template",
                    e);
        }
    }

    /**
     * Builder for ApplicationManagerImpl class.
     *
     * @return the application manager builder
     */
    public static ApplicationManagerImplBuilder builder() {
        return new ApplicationManagerImplBuilder();
    }

    /**
     * createMAASApplication method used to generate MAAS based on input json file passed from UI/CLI
     *
     * @param maasInstance
     *            maas instance as an input
     * @return path of the application
     */
    public Path createMAASApplication(final MAASInstance maasInstance) {

        Path newApplication = null;
        Log.info("Start Creating createMAASApplication");

        try {

            //TODO:: validation logic - check whether all mandatory values are present or not (for e.g. only allowed CTR/CTUM) will be handled next
            if (null != maasInstance
                    && (maasInstance.getMaas().getUiDef().getInputSource().equalsIgnoreCase(ApplicationManagerConstants.MAAS_INPUT_SOURCE))) {

                final File maasTemplateFile = new File(ApplicationManagerConstants.MAAS_BLANK_TEMPLATE_PATH);
                maasTemplateFile.getParentFile().mkdirs();

                OutputStream out = null;
                try {
                    out = new FileOutputStream(maasTemplateFile, true);
                    IOUtils.copy(this.getClass().getClassLoader().getResourceAsStream(MAAS_TEMPLATE_IN_CLASSPATH), out);

                } catch (final IOException e) {
                    throw new ApplicationManagerException(ApplicationManagerExceptionCodes.MAAS_TEMPLATE_NOT_FOUND,
                            String.format("error loading maas template %s", "template not found in classpath:: " + e.getMessage()));
                } finally {
                    if (out != null) {
                        IOUtils.closeQuietly(out);
                    }
                }

                newApplication = applicationGenerator.createMAASApplication(maasInstance);
            } else {
                throw new ApplicationManagerException(ApplicationManagerExceptionCodes.MAAS_INVALID_DATA,
                        String.format("error generating maas application %s", "Invalid MASS data, supports only Stream"));
            }
        } catch (final Exception e) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_CREATING_APPLICATION,
                    String.format("error generating maas application %s", e.getMessage()));
        }

        Log.info("end - createMAASApplication - Successfully created application at {}", newApplication.toAbsolutePath());

        return newApplication;
    }

    /**
     * @param jsonStr
     *            - maas json
     * @return instance
     */
    public MAASInstance getMAASInstance(final String jsonStr) {
        Log.debug("Start getMAASInstance");
        return maasSchemaTool.getMAASModelInstance(jsonStr);
    }

    private void checkExpiredApplications() {
        Log.debug("Running ApplicationScannerRunnable Timer for thread: {}", Thread.currentThread().getName());

        try {

            for (final ApplicationPath app : ApplicationController.instance().getApplications()) {

                final long elapsedTime = (System.currentTimeMillis() - app.getTimestamp()) / 1000;

                if (elapsedTime > APP_EXPIRATION) {
                    // this application is old and should be removed
                    Log.debug("Application elapsed time: {}, removing it: {} elapsed time {} ", elapsedTime, app.getPbaId(), elapsedTime);

                    if (app.getPath().toFile().exists()) {
                        FileUtils.forceDelete(app.getPath().toFile());
                    }
                    ApplicationController.instance().removeApplication(app.getPbaId());

                }

            }

        } catch (final Exception e) {
            Log.error("Error while running ApplicationScannerRunnable timer", e);
        }
    }

}