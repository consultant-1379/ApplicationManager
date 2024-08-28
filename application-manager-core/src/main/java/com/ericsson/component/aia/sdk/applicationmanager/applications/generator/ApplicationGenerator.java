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
package com.ericsson.component.aia.sdk.applicationmanager.applications.generator;

import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.CHAR_HYPEN;
import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.PBA_DESCRIPTION;
import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.PBA_NAME;
import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.PBA_NAME_CAMELCASE;
import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.PBA_VERSION;
import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.ROOT_PATH;
import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.SDK_DELIMITER;
import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.UTF_8_ENCODING;
import static org.apache.commons.lang3.text.WordUtils.capitalize;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.sdk.applicationmanager.applications.maas.generator.ReplaceWithMAASApplicationPropertiesVisitor;
import com.ericsson.component.aia.sdk.applicationmanager.common.FileExtensions;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConstants;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.applicationmanager.maas.mapper.MassMapper;
import com.ericsson.component.aia.sdk.applicationmanager.model.MAASInstance;
import com.ericsson.component.aia.sdk.applicationmanager.util.AppSdkUtil;
import com.ericsson.component.aia.sdk.applicationmanager.util.AppTemplateConnector;
import com.ericsson.component.aia.sdk.applicationmanager.util.EntryPointMapping;
import com.ericsson.component.aia.sdk.applicationmanager.util.FilePropertyCache;
import com.ericsson.component.aia.sdk.applicationmanager.util.KafkaConnectMapping;
import com.ericsson.component.aia.sdk.applicationmanager.util.PbaToFlowXmlConverter;
import com.ericsson.component.aia.sdk.pba.model.Arg;
import com.ericsson.component.aia.sdk.pba.model.ExtensionPoint;
import com.ericsson.component.aia.sdk.pba.model.IntegrationPoint;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * ApplicationGenerator class is responsible for generating new application by downloading a template from GIT and updating its contents with the
 * information contained within the new applications PBA.
 *
 */
public class ApplicationGenerator {

    private static final Logger Log = LoggerFactory.getLogger(ApplicationGenerator.class);

    private static final String CLEANUP_FILE = "CLEANUP";
    private static final String GLOBAL_TRUE = "TRUE";
    private static final String PROPERTY_CACHE = "PROPERTY_CACHE";
    private static final String MAAS_APPLICATION = "MAASApplication";

    private final String TMP_DIR;

    private final Path cachedTemplatePath;
    private final Path cachedApplicationPath;

    private final PBASchemaTool pbaSchemaTool;
    private final TemplateManager templateManager;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final JsonParser jsonParser = new JsonParser();

    private FilePropertyCache filePropertyCache;
    private Map<String, List<String>> filePropertyMap;

    /**
     * Instantiates a new application generator.
     *
     * @param templateManager
     *            a template manager instance
     * @param pbaSchemaTool
     *            a pba schema tool instance
     */
    public ApplicationGenerator(final TemplateManager templateManager, final PBASchemaTool pbaSchemaTool) {
        this.pbaSchemaTool = pbaSchemaTool;
        this.templateManager = templateManager;
        final Path rootLocation = Paths.get(ApplicationManagerConfiguration.storageLocation);
        AppSdkUtil.recreatePath(rootLocation);

        this.cachedTemplatePath = AppSdkUtil.createFolderIfNotExists(ApplicationManagerConstants.SDK_TEMPLATE_CACHE_FOLDER);
        this.cachedApplicationPath = AppSdkUtil.createFolderIfNotExists(ApplicationManagerConstants.SDK_APPLICATION_CACHE_FOLDER);

        String tmp = System.getProperty("java.io.tmpdir");
        if (tmp == null) {
            tmp = "";
        }
        this.TMP_DIR = tmp;
    }

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
    public Path createApplication(final PBAInstance pbaInstance) {

        // Template details
        final String templateId = pbaInstance.getPba().getTemplateInfo().getId();

        // Application details
        final PbaInfo applicationInfo = pbaInstance.getPba().getApplicationInfo();
        final String title = applicationInfo.getTitle();
        final String applicationName = (title != null ? title.replaceAll("\\s+", "-").toLowerCase() : "");

        applicationInfo.setName(applicationName);

        // Download template from template manager if cached file not exists
        final Path downloadedTemplatePath = getTemplate(templateId);

        // create a new application zip file from the existing copy of template zip file
        final Path templatePath = copyAndRenameTemplateZipFile(applicationName, downloadedTemplatePath);

        addApplicationConfigurations(pbaInstance, templatePath);
        return templatePath;
    }

    /**
     * This method create a temp directory and places all the to be updated/modified files in it(like pba, json ..etc).
     *
     * @param pbaInstance
     *            the pba model
     * @param extendedPbaInstance
     *            the extended pba model
     * @param zipPath
     *            the template path
     */
    public void addExtendedApplicationConfigurations(final PBAInstance pbaInstance, final PBAInstance extendedPbaInstance, final Path zipPath) {
        addCommonApplicationConfigurations(extendedPbaInstance, zipPath);
        replaceWithExtendedApplicationProperties(zipPath, pbaInstance, extendedPbaInstance);
    }

    private Path getTemplate(final String templatePbaId) {
        final String zipFileName = templatePbaId + FileExtensions.ZIP.getExtensionType();
        final Path cacheTemplate = getFilePathIfExists(zipFileName, cachedTemplatePath);

        if (cacheTemplate != null) {
            return cacheTemplate;
        } else {
            Path downloadedTemplatePath = null;
            try {
                // Download template from template manager
                downloadedTemplatePath = templateManager.downloadTemplate(templatePbaId);

                try {
                    FileUtils.copyFileToDirectory(downloadedTemplatePath.toFile(), cachedTemplatePath.toFile());
                } catch (final IOException e) {
                    Log.error("exception occurred, when trying to copy zip to temp directory", e);
                    throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_CLONING_APPLICATION_ON_FILESYSTEM, e);
                }
                return getFilePathIfExists(downloadedTemplatePath.toFile().getName(), cachedTemplatePath);
            } finally {
                if (downloadedTemplatePath != null && downloadedTemplatePath.toFile().toString().contains(TMP_DIR)) {
                    try {
                        FileUtils.forceDelete(downloadedTemplatePath.toFile().getParentFile());
                    } catch (final Exception ex) {
                        Log.error(ex.getMessage(), ex);
                    }
                }
            }
        }

    }

    private Path copyAndRenameTemplateZipFile(final String applicationName, final Path downloadedTemplatePath) {
        try {
            final String applicationZipName = applicationName + SDK_DELIMITER + System.currentTimeMillis() + FileExtensions.ZIP.getExtensionType();
            final Path pathToNewApplication = this.cachedApplicationPath.resolve(applicationZipName);
            FileUtils.copyFile(downloadedTemplatePath.toFile(), pathToNewApplication.toFile());
            return pathToNewApplication;

        } catch (final IOException e) {
            Log.error("exception occurred, when trying to create a new copy of {} template", applicationName, e);
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_CLONING_APPLICATION_ON_FILESYSTEM, e);
        }

    }

    private void addApplicationConfigurations(final PBAInstance pbaInstance, final Path zipTemplatePath) {

        addCommonApplicationConfigurations(pbaInstance, zipTemplatePath);

        final Optional<AppTemplateConnector> templateConnector = AppTemplateConnector
                .findAppTemplateConnector(pbaInstance.getPba().getTemplateInfo().getName());

        if (templateConnector.isPresent() && templateConnector.get() == AppTemplateConnector.KAFKA) {
            handleEntryPoints(zipTemplatePath, pbaInstance);
        } else {
            replacePlaceHolderProperties(zipTemplatePath, pbaInstance);
        }

    }

    private void addCommonApplicationConfigurations(final PBAInstance pbaInstance, final Path zipTemplatePath) {

        final Optional<String> flowTemplate = extractTemplateFromZip(zipTemplatePath);

        replaceDuplicatedInformationOnPba(pbaInstance);

        processCommonApplicationConfiguration(pbaInstance, zipTemplatePath, flowTemplate);

    }

    private void processCommonApplicationConfiguration(final PBAInstance pbaInstance, final Path zipTemplatePath,
                                                       final Optional<String> flowTemplate) {
        String flowXml = null;
        final JsonElement jsonElement;
        final Optional<AppTemplateConnector> templateConnector = AppTemplateConnector
                .findAppTemplateConnector(pbaInstance.getPba().getTemplateInfo().getName());

        Log.info("flowTemplate Available::{}, templateConnector available::{}", flowTemplate.isPresent(), templateConnector.isPresent());
        if (templateConnector.isPresent()) {
            Log.info("templateConnector value::{}", templateConnector.get());
            String prettyJsonString = null;

            try (FileSystem zipfs = FileSystems.newFileSystem(zipTemplatePath, this.getClass().getClassLoader())) {

                jsonElement = jsonParser.parse(pbaSchemaTool.convertToJsonString(pbaInstance));
                prettyJsonString = gson.toJson(jsonElement);

                switch (templateConnector.get()) {

                    case SPARK:
                    case FLINK:
                    case BEAM:

                        if (flowTemplate.isPresent()) {
                            flowXml = PbaToFlowXmlConverter.createFlowFromPba(pbaInstance, flowTemplate.get());
                        } else {
                            flowXml = PbaToFlowXmlConverter.createFlowFromPba(pbaInstance);
                        }

                        addFolderToZip(flowXml, zipfs, ROOT_PATH + ApplicationManagerConstants.FLOW_XML);

                        // Template chosen was KAFKA (bypass generation of flow xml, addfiletoZip is
                        // common to all technologies)
                    default: {
                        addFolderToZip(prettyJsonString, zipfs, ROOT_PATH + ApplicationManagerConstants.PBA_JSON);
                        break;
                    }
                }

            } catch (final IOException exp) {
                final PbaInfo applicationInfo = pbaInstance.getPba().getApplicationInfo();
                throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_CREATING_APPLICATION,
                        "Exception occurred, when trying to add application configuration for application with name " + applicationInfo.getName()
                                + " and version " + applicationInfo.getVersion(),
                        exp);
            }
        }
    }

    private void replaceDuplicatedInformationOnPba(final PBAInstance pbaInstance) {
        // changing duplicated features names and descriptions
        final AtomicInteger nameSequenceSuffix = new AtomicInteger(0);
        if (pbaInstance.getPba().getExtensionPoints() != null && pbaInstance.getPba().getExtensionPoints().size() > 1) {
            for (final ExtensionPoint extensionPoint : pbaInstance.getPba().getExtensionPoints()) {
                final String suffix = String.valueOf(nameSequenceSuffix.incrementAndGet());
                extensionPoint.setName(AppSdkUtil.appendToFeatureName(extensionPoint.getName(), suffix));
                extensionPoint.setDescription(AppSdkUtil.appendToFeatureName(extensionPoint.getDescription(), suffix));
            }
        }
        if (pbaInstance.getPba().getIntegrationPoints() != null && pbaInstance.getPba().getIntegrationPoints().size() > 1) {
            for (final IntegrationPoint integrationPoint : pbaInstance.getPba().getIntegrationPoints()) {
                final String suffix = String.valueOf(nameSequenceSuffix.incrementAndGet());
                integrationPoint.setName(AppSdkUtil.appendToFeatureName(integrationPoint.getName(), suffix));
                integrationPoint.setDescription(AppSdkUtil.appendToFeatureName(integrationPoint.getDescription(), suffix));
            }
        }

    }

    private void addFolderToZip(final String fileAsString, final FileSystem zipfs, final String zipDestPath) {
        try {
            final Path pathInZipfile = zipfs.getPath(zipDestPath);

            Files.copy(IOUtils.toInputStream(fileAsString, UTF_8_ENCODING), pathInZipfile.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_CREATING_APPLICATION,
                    String.format("Exception occurred, when trying to add %s file to the %s zip file", fileAsString, zipDestPath), e);
        }
    }

    private Path getFilePathIfExists(final String fileName, final Path pathToCheck) {
        if (pathToCheck != null && pathToCheck.toFile().exists()) {
            final File[] listFiles = pathToCheck.toFile().listFiles();

            for (final File subFile : listFiles) {
                if (subFile.getName().equals(fileName)) {
                    return subFile.toPath();
                }
            }
        }
        return null;
    }

    /**
     * handleEntryPoints method used to process extension and integration points
     *
     * @param zipFilePath
     * @param pbaInstance
     */
    private void handleEntryPoints(final Path zipFilePath, final PBAInstance pbaInstance) {

        filePropertyMap = getFilePropertyCache(zipFilePath);

        final List<Map<String, String>> sourceReplaceValueList = new ArrayList<Map<String, String>>();
        final List<Map<String, String>> sinkReplaceValueList = new ArrayList<Map<String, String>>();
        final List<Map<String, String>> kafkaValueList = new ArrayList<Map<String, String>>();

        final Map<String, String> selectedFileValues = new HashMap<String, String>();

        handleExtensionPoints(pbaInstance, selectedFileValues, kafkaValueList, sourceReplaceValueList);

        handleIntegrationPoints(pbaInstance, selectedFileValues, kafkaValueList, sinkReplaceValueList);

        final PbaInfo applicationInfo1 = pbaInstance.getPba().getApplicationInfo();
        final String name1 = applicationInfo1.getName();

        // celanup remaining files
        selectedFileValues.put(CLEANUP_FILE, GLOBAL_TRUE);
        selectedFileValues.put("pba", "pba");
        selectedFileValues.put("README", "README");

        replaceProperties(zipFilePath, pbaInstance, new ReplaceWithApplicationPropertiesVisitor(PBA_NAME, name1, selectedFileValues));

        validateKafkaTechnologyEntryPoint(kafkaValueList);

        replaceProperties(zipFilePath, pbaInstance, new ReplaceKafkaApplicationProperties(sourceReplaceValueList, sinkReplaceValueList,
                kafkaValueList, selectedFileValues, filePropertyMap));

    }

    private void handleExtensionPoints(final PBAInstance pbaInstance, final Map<String, String> selectedFileValues,
                                       final List<Map<String, String>> kafkaValueList, final List<Map<String, String>> sourceReplaceValueList) {

        List<Arg> args = null;
        String technology = "";
        String entryPoint = "";

        for (final ExtensionPoint exPoint : pbaInstance.getPba().getExtensionPoints()) {
            Log.info(" technology::{}, name::{}", exPoint.getTechnology(), exPoint.getName());

            technology = exPoint.getTechnology();
            args = null;

            // reset

            args = exPoint.getAttributes();

            entryPoint = "source";
            populateEntryPoint(technology, entryPoint, args, selectedFileValues, kafkaValueList, sourceReplaceValueList);
        }
    }

    private void handleIntegrationPoints(final PBAInstance pbaInstance, final Map<String, String> selectedFileValues,
                                         final List<Map<String, String>> kafkaValueList, final List<Map<String, String>> sinkReplaceValueList) {
        // sink
        List<Arg> args = null;
        String technology = "";
        String entryPoint = "";
        for (final IntegrationPoint inPoint : pbaInstance.getPba().getIntegrationPoints()) {

            entryPoint = "";
            Log.info("point=sink" + "\ttech:: " + inPoint.getTechnology() + "\tname:: " + inPoint.getName());
            technology = inPoint.getTechnology().toLowerCase();

            args = inPoint.getAttributes();

            entryPoint = "sink";
            populateEntryPoint(technology, entryPoint, args, selectedFileValues, kafkaValueList, sinkReplaceValueList);
        }
    }

    private Map<String, List<String>> getFilePropertyCache(final Path zipFilePath) {
        if (filePropertyCache == null) {
            filePropertyCache = new FilePropertyCache(zipFilePath, 10);
        }

        Map<String, List<String>> filePropertyMap = null;

        try {
            filePropertyMap = filePropertyCache.get(PROPERTY_CACHE);
        } catch (final ExecutionException e) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_ACCESSING_CACHE_SYSTEM,
                    "Exception occurred, when trying retrieve cache values of kafka file properties", e);
        }
        return filePropertyMap;
    }

    private void validateKafkaTechnologyEntryPoint(final List<Map<String, String>> kafkaValueList) {

        if (kafkaValueList.isEmpty()) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.KAFKA_TECHNOLOGY_IS_MANDATORY,
                    "Kafka technology is mandatory in either integration or extension point");
        }

        if (kafkaValueList.size() == 2) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.DUPLICATED_KAFKA_TECHNOLOGY,
                    "Kafa technology selected in both integration and extension point");
        }
    }

    private void populateEntryPoint(final String technology, String entryPoint, final List<Arg> args, final Map<String, String> selectedFileValues,
                                    final List<Map<String, String>> kafkaValueList, final List<Map<String, String>> sourceReplaceValueList) {

        if ("kafka-connect-kafka".equalsIgnoreCase(technology)) {
            entryPoint = "";
            populateEntryPointDetails(technology, entryPoint, args, selectedFileValues, kafkaValueList);
        } else {
            populateEntryPointDetails(technology, entryPoint, args, selectedFileValues, sourceReplaceValueList);
        }
    }

    private void populateEntryPointDetails(final String technology, String entryPoint, final List<Arg> args,
                                           final Map<String, String> selectedFileValues, final List<Map<String, String>> replaceValueList) {

        // reset
        String deployMode = "";
        String fileName = "";
        // require deploy mode only for kafka
        if ("kafka-connect-kafka".equalsIgnoreCase(technology)) {
            deployMode = findKafkaDeployMode(args);
            entryPoint = "";
        }

        final EntryPointMapping entryPointMapping = EntryPointMapping.getEntryPointMapping(deployMode + entryPoint);
        final KafkaConnectMapping kafkaConnectMapping = KafkaConnectMapping.getKafkaConnectInnerMapping(technology + entryPointMapping);

        if (kafkaConnectMapping != null) {

            fileName = kafkaConnectMapping.getKafkaConnectFileMapping().getFileName();

            Log.info("technology:" + "::\tfileName::" + fileName);

            // add selectedfile to map
            selectedFileValues.put(fileName, fileName);

            final Map<String, String> replaceValues = new HashMap<>();

            for (final Map.Entry<String, List<String>> entry : filePropertyMap.entrySet()) {
                Log.debug(entry.getKey());
                if (entry.getKey().contains(fileName)) {

                    for (final String prop : entry.getValue()) {
                        Log.debug(prop);
                        for (final Arg arg : args) {
                            Log.debug("key:: " + arg.getKey() + "\tvalue::" + arg.getValue());
                            if (prop.contains(arg.getKey() + "=")) {

                                final String value = arg.getKey() + "=" + arg.getValue();
                                Log.debug(value);
                                replaceValues.put(prop, value);
                                break;
                            }
                        }
                    }
                }
            }
            // add sinkReplaceValues to list
            replaceValueList.add(replaceValues);

        }
    }

    private String findKafkaDeployMode(final List<Arg> args) {

        String deployMode = null;

        for (final Arg arg : args) {

            if (arg.getKey().equalsIgnoreCase("kafka.mode")) {
                deployMode = arg.getValue().toString();
                Log.debug("kafka-connect-kafa  key:: " + arg.getKey() + "\tvalue::" + arg.getValue());
                break;
            }

        }
        return deployMode;
    }

    private void replacePlaceHolderProperties(final Path zipFilePath, final PBAInstance pbaInstance) {
        final PbaInfo applicationInfo = pbaInstance.getPba().getApplicationInfo();
        final String name = applicationInfo.getName();

        final Map<String, String> replaceValues = new HashMap<>();
        replaceValues.put(PBA_NAME, name);
        replaceValues.put(PBA_NAME_CAMELCASE, AppSdkUtil.convertApplicationName(name));
        replaceValues.put(PBA_DESCRIPTION, applicationInfo.getDescription());
        replaceValues.put(PBA_VERSION, applicationInfo.getVersion());

        replaceProperties(zipFilePath, pbaInstance, new ReplaceWithApplicationPropertiesVisitor(PBA_NAME, name, replaceValues));
    }

    private void replaceWithExtendedApplicationProperties(final Path zipFilePath, final PBAInstance pbaInstance,
                                                          final PBAInstance extendedPbaInstance) {

        final PbaInfo applicationInfo = pbaInstance.getPba().getApplicationInfo();
        final PbaInfo extendedApplicationInfo = extendedPbaInstance.getPba().getApplicationInfo();

        final String pbaName = applicationInfo.getName();
        final String extendedPbaName = extendedApplicationInfo.getName();

        final Map<String, String> replaceValues = new HashMap<>();
        replaceValues.put(pbaName, extendedApplicationInfo.getName());
        replaceValues.put(capitalize(pbaName, CHAR_HYPEN), capitalize(extendedPbaName, CHAR_HYPEN));
        replaceValues.put(applicationInfo.getDescription(), extendedApplicationInfo.getDescription());

        replaceProperties(zipFilePath, pbaInstance, new ReplaceWithApplicationPropertiesVisitor(pbaName, extendedPbaName, replaceValues));
    }

    private void replaceProperties(final Path zipFilePath, final PBAInstance pbaInstance, final SimpleFileVisitor<Path> fileVisitor) {

        try (final FileSystem zipfs = FileSystems.newFileSystem(zipFilePath, this.getClass().getClassLoader())) {
            final Iterable<Path> iterable = zipfs.getRootDirectories();
            for (final Path path : iterable) {
                Files.walkFileTree(path, fileVisitor);
            }
        } catch (final IOException exp) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_CREATING_APPLICATION,
                    String.format("Exception occurred, when trying to add application configuration for application with name %s and version %s",
                            pbaInstance.getPba().getApplicationInfo().getName(), pbaInstance.getPba().getApplicationInfo().getVersion()),
                    exp);
        }
    }

    private Optional<String> extractTemplateFromZip(final Path templatePath) {
        try (final FileSystem zipfs = FileSystems.newFileSystem(templatePath, this.getClass().getClassLoader())) {
            for (final Path root : zipfs.getRootDirectories()) {
                final Optional<Path> flowPath = AppSdkUtil.findFileInPath("flow.vm", root);
                if (flowPath.isPresent()) {
                    final Path flow = flowPath.get();
                    try (InputStream pbaInputStream = Files.newInputStream(flow)) {
                        final String flowContents = IOUtils.toString(pbaInputStream, "UTF-8");
                        Files.delete(flow);
                        return Optional.of(flowContents);
                    }
                }
            }
        } catch (final IOException exp) {
            Log.error("Exception occurred, when trying to extract data from application being published", exp);
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_EXTRACTING_APPLICATION_DATA, exp);
        }
        return Optional.empty();
    }

    /**
     * createMAASApplication used for creating application.
     *
     * @param maasInstance
     *            a MAAS instance
     * @return massApplicationPath a maas application path
     */
    public Path createMAASApplication(final MAASInstance maasInstance) {

        final String maasTemplatePathStr = ApplicationManagerConstants.MAAS_BLANK_TEMPLATE_PATH;
        Log.info("MAASBlankTemplate.zip file Path is {}", maasTemplatePathStr);

        final Path maasTemplatePath = Paths.get(maasTemplatePathStr);

        final Path massApplicationPath = copyAndRenameTemplateZipFile(MAAS_APPLICATION, maasTemplatePath);

        addMAASApplicationConfigurations(maasInstance, massApplicationPath);

        return massApplicationPath;
    }

    private void addMAASApplicationConfigurations(final MAASInstance maasInstance, final Path templatePath) {

        final Map<String, Map<String, String>> replaceValues = new HashMap<String, Map<String, String>>();

        new MassMapper(replaceValues).mapper(maasInstance);
        replaceProperties(templatePath, new ReplaceWithMAASApplicationPropertiesVisitor(replaceValues));
    }

    private void replaceProperties(final Path zipFilePath, final SimpleFileVisitor<Path> fileVisitor) {

        try (final FileSystem zipfs = FileSystems.newFileSystem(zipFilePath, this.getClass().getClassLoader())) {
            final Iterable<Path> iterable = zipfs.getRootDirectories();
            for (final Path path : iterable) {
                Files.walkFileTree(path, fileVisitor);
            }
        } catch (final IOException exp) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_CREATING_APPLICATION, String.format(
                    "Exception occurred, when trying to add application configuration for application with name %s and version %s", "", ""), exp);
        }
    }

}