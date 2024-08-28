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

import static java.lang.String.format;
import static org.springframework.http.ResponseEntity.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import javax.ws.rs.core.Application;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ericsson.component.aia.sdk.applicationmanager.api.ApplicationManager;
import com.ericsson.component.aia.sdk.applicationmanager.applications.ApplicationController;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.applicationmanager.factory.ApplicationAbstractFactory;
import com.ericsson.component.aia.sdk.applicationmanager.factory.ApplicationFactory;
import com.ericsson.component.aia.sdk.applicationmanager.factory.ApplicationFactoryProducer;
import com.ericsson.component.aia.sdk.applicationmanager.factory.MaasFactory;
import com.ericsson.component.aia.sdk.applicationmanager.model.MAASInstance;
import com.ericsson.component.aia.sdk.applicationmanager.services.endpoints.response.ServiceResponse;
import com.ericsson.component.aia.sdk.applicationmanager.util.AppSdkUtil;
import com.ericsson.component.aia.sdk.applicationmanager.views.ApplicationVersionView;
import com.ericsson.component.aia.sdk.applicationmanager.views.PublishApplicationView;
import com.ericsson.component.aia.sdk.applicationmanager.views.PublishedApplicationsView;
import com.ericsson.component.aia.sdk.applicationmanager.views.TaskStatusView;
import com.ericsson.component.aia.sdk.pba.exception.InvalidPbaException;
import com.ericsson.component.aia.sdk.pba.model.BuildInfo;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;

import io.swagger.annotations.ApiOperation;

/**
 * This class defines the REST endpoints supported by the Application manager application.
 *
 * @author echchik
 *
 */
@CrossOrigin
@RestController
public class ApplicationManagerEndPoint {

    private static final Logger Log = LoggerFactory.getLogger(ApplicationManagerEndPoint.class);

    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private PBASchemaTool pbaSchemaTool;

    @Autowired
    private ApplicationFactoryProducer applicationFactoryProducer;

    /**
     * This POST endpoint accepts {@link PBAInstance} of application to be published as json string and return {@link HttpStatus.OK} code
     *
     * @param file
     *            {@link MultipartFile} as json of the application to publish
     * @param version
     *            the version of the application being published.
     * @return {@link ResponseEntity} The Id of the publish task.
     * @throws IOException
     *             an exception occurs when reading.
     */
    @ResponseBody
    @RequestMapping(value = "/applications/publish", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "publishApplication accepts PBAInstance of application, version and returns HttpStatus.OK")
    public ResponseEntity<PublishApplicationView> publishApplication(@RequestParam("applicationzip") final MultipartFile file,
                                                                     @RequestParam("version") final String version)
            throws IOException {

        PublishApplicationView publishView = new PublishApplicationView();
        try (final InputStream inputStream = file.getInputStream()) {
            publishView = applicationManager.publishApplication(inputStream, file.getOriginalFilename(), version);
            inputStream.close();
        }

        return status(HttpStatus.OK).body(publishView);
    }

    /**
     * This GET return {@link HttpStatus.OK} code and the status of the task associated with the taskId.
     *
     * @param taskId
     *            the unique id of the task
     * @return {@link ServiceResponse} containing a {@link TaskStatusView} as data.
     *
     */
    @RequestMapping(value = "/applications/publish/{id}/status", method = RequestMethod.GET)
    @ApiOperation(value = "getStatus accepts taskId and returns HTTP Status OK, status of task associated with the taskId")
    public ResponseEntity<TaskStatusView> getStatus(@PathVariable("id") final String taskId) {
        final TaskStatusView taskStatusView = applicationManager.getTaskStatus(taskId);
        return status(HttpStatus.OK).body(taskStatusView);
    }

    /**
     * This DELETE endpoint accepts name and version of the application to un-publish and return HttpStatus.OK if successful.
     *
     * @param pbaId
     *            of the application
     */
    @RequestMapping(value = "/applications/publish/{id:.+}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "unPublishApplication accepts pba id of application to un-publish and return HttpStatus.OK")
    public void unPublishApplication(@PathVariable("id") final String pbaId) {
        applicationManager.unPublishApplication(pbaId);
    }

    /**
     * This DELETE endpoint accepts the id of the application to delete and return {@link HttpStatus.OK} if successful.
     *
     * @param pbaId
     *            of the application
     */
    @RequestMapping(value = "/applications/cleanup/{id:.+}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "cleanupApplication accepts id of application to delete and return  HttpStatus.OK")
    public void cleanupApplication(@PathVariable("id") final String pbaId) {
        applicationManager.cleanupApplication(pbaId);
    }

    /**
     * This POST endpoint accepts {@link PBAInstance} of new application to be created as json string and return {@link HttpStatus.OK} code
     *
     * @param pbaModelAsString
     *            {@link PBAInstance} as json of the application to create.
     * @return {@link InputStreamResource} of the new application to download.
     */
    @RequestMapping(value = "/applications", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "createApplication accepts PBAInstance as json string and return HttpStatus.OK")
    public ResponseEntity<ServiceResponse<String>> createApplication(@RequestBody final String pbaModelAsString) {

        final ServiceResponse<String> serviceResponse = new ServiceResponse<>();

        try {
            final PBAInstance pbaInstance = pbaSchemaTool.getPBAModelInstance(pbaModelAsString);

            if (pbaInstance.getPba() == null || pbaInstance.getPba().getBuildInfo() == null) {
                throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, "Pba buildInfo information is invalid");
            }

            final BuildInfo buildInfo = pbaInstance.getPba().getBuildInfo();
            buildInfo.setDependencies(new ArrayList<>(new HashSet<>(buildInfo.getDependencies())));

            final String applicationId = AppSdkUtil.createApplicationId(pbaInstance.getPba());
            Log.info("Generated Application id is {}", applicationId);
            pbaInstance.getPba().getApplicationInfo().setId(applicationId);

            final Path path = applicationManager.createApplication(pbaInstance);
            //add application for collection in the future
            ApplicationController.instance().addApplication(applicationId, path);

            serviceResponse.setData(ApplicationController.instance().addApplication(applicationId, path));

        } catch (final InvalidPbaException ex) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, ex);
        }

        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * This POST endpoint accepts name and version of the application to be extended and {@link PBAInstance} of new application as json string and
     * return {@link HttpStatus.OK} code
     *
     * @param pbaModelAsString
     *            {@link PBAInstance} as json of the extended application.
     * @return {@link InputStreamResource} of the extended application to download.
     */
    @RequestMapping(value = "/applications/extend", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "extendApplication helps to extend application, accepts PBAInstance of new application and returns HTTPStatus.OK")
    public ResponseEntity<ServiceResponse<String>> extendApplication(@RequestBody final String pbaModelAsString) {

        final ServiceResponse<String> serviceResponse = new ServiceResponse<>();

        try {

            final PBAInstance pbaInstance = pbaSchemaTool.getPBAModelInstance(pbaModelAsString);
            if (pbaInstance.getPba() == null || pbaInstance.getPba().getApplicationInfo() == null) {
                throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, "Pba application information is invalid");
            }
            final Path path = applicationManager.extendApplication(pbaInstance.getPba().getApplicationInfo().getId(), pbaInstance);

            serviceResponse.setData(ApplicationController.instance().addApplication(AppSdkUtil.createApplicationId(pbaInstance.getPba()), path));

        } catch (final InvalidPbaException ex) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, ex);
        }

        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * This GET return {@link HttpStatus.OK} code and collection of {@link Application} published as json.
     *
     * @param templateId
     *            the template id
     * @return {@link ServiceResponse} containing collection of {@link Application} as data.
     */
    @RequestMapping(value = "/applications", method = RequestMethod.GET)
    @ApiOperation(value = "listApplications accepts templateId and returns HTTPStatus.OK and ServiceResponse")
    public ResponseEntity<ServiceResponse<?>> listApplications(@RequestParam(value = "templateId") final Optional<String> templateId) {
        Collection<PublishedApplicationsView> applications = Collections.emptyList();

        if (templateId.isPresent()) {
            applications = applicationManager.listApplicationsMatchingTemplate(templateId.get());
        } else {
            applications = applicationManager.listApplications();
        }

        final ServiceResponse<Collection<PublishedApplicationsView>> serviceResponse = new ServiceResponse<>();
        serviceResponse.setData(applications);
        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * This GET endpoint accepts name and version of the application and return {@link HttpStatus.OK} code and {@link PBAInstance} as json.
     *
     * @param pbaId
     *            of the application
     * @return {@link ServiceResponse} containing {@link PBAInstance} as data
     */
    @RequestMapping(value = "/applications/{id:.+}", method = RequestMethod.GET)
    @ApiOperation(value = "getApplicationPba accepts pbaId and returns HTTPStatus.OK and ServiceResponse")
    public ResponseEntity<ServiceResponse<PBAInstance>> getApplicationPba(@PathVariable("id") final String pbaId) {
        final PBAInstance pbaModel = applicationManager.getPBAInstance(pbaId);
        final ServiceResponse<PBAInstance> serviceResponse = new ServiceResponse<>();
        serviceResponse.setData(pbaModel);
        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * This GET endpoint accepts name of an application and return {@link HttpStatus.OK} code and {@link ApplicationVersionView} as json.
     *
     * @param applicationName
     *            of the application
     * @return {@link ApplicationVersionView} as data
     */
    @RequestMapping(value = "/applications/{applicationName}/version", method = RequestMethod.GET)
    @ApiOperation(value = "Get application versions, accepts applicationName and returns HTTPStatus.OK and pbaModel")
    public ResponseEntity<ApplicationVersionView> getApplicationVersion(@PathVariable("applicationName") final String applicationName) {
        final ApplicationVersionView pbaModel = applicationManager.getVersion(applicationName);
        return status(HttpStatus.OK).body(pbaModel);
    }

    /**
     * This GET return {@link HttpStatus.OK} code and collection of {@link Application} published as json.
     *
     * @param projectId
     *            the unique id of the local zip file
     * @return {@link ServiceResponse} containing collection of {@link Application} as data.
     * @throws FileNotFoundException
     *             if the file to download does not exists.
     */
    @RequestMapping(value = "/applications/{id}/zip", method = RequestMethod.GET)
    @ApiOperation(value = "downloadApplication accepts projectId and returns application zip")
    public ResponseEntity<InputStreamResource> downloadApplication(@PathVariable("id") final String projectId) throws FileNotFoundException {
        Path applicationZip = ApplicationController.instance().getApplication(projectId);
        if (applicationZip != null) {
            if (!applicationZip.toFile().exists()) {
                ApplicationController.instance().removeApplication(projectId);
                applicationZip = null;
            } else {
                return getInputStreamResourceResponseEntity(applicationZip);
            }
        }
        applicationZip = applicationManager.getPublishedApplication(projectId);
        //add application for collection in the future
        ApplicationController.instance().addApplication(projectId, applicationZip);
        return getInputStreamResourceResponseEntity(applicationZip);

    }

    private ResponseEntity<InputStreamResource> getInputStreamResourceResponseEntity(final Path path) throws FileNotFoundException {
        final File file = path.toFile();
        if (!file.exists()) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlAllowHeaders(Arrays.asList("Content-Type", "attachment"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, format("attachment; filename=\"%s\"", file.getName()));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("attachment", file.getName());
        headers.setAccessControlAllowOrigin("*");
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);
        headers.setContentLength(file.length());
        final InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(file));
        final ResponseEntity<InputStreamResource> response = new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
        return response;
    }

    /**
     * This POST endpoint accepts {@link json file} of new application and return {@link HttpStatus.OK} code
     *
     * @param file
     *            as json of the application to create.
     * @return {@link InputStreamResource} of the new application to download.
     */
    @RequestMapping(value = "/maas/applications", method = RequestMethod.POST, consumes = { "multipart/form-data" })
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<ServiceResponse<String>> createMAASApplication(@RequestPart("file") final MultipartFile file) {

        String maasJSONStr = null;
        try {
            maasJSONStr = new String(file.getBytes(), "UTF-8");
        } catch (final IOException e) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.UNKNOWN_APPLICATION_ERROR, e);
        }

        final ServiceResponse<String> serviceResponse = new ServiceResponse<>();

        try {

            final MAASInstance maasInstance = applicationManager.getMAASInstance(maasJSONStr);

            final Path path = applicationManager.createMAASApplication(maasInstance);

            final String applicationId = AppSdkUtil.createMaasApplicationId(maasInstance.getMaas());
            serviceResponse.setData(ApplicationController.instance().addApplication(applicationId, path));
            //add application for collection in the future
            ApplicationController.instance().addApplication(applicationId, path);

        } catch (final InvalidPbaException ex) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.PBA_IS_INVALID, ex);
        }

        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * This POST endpoint accepts {@link PBAInstance} of new application to be created as json string and return {@link HttpStatus.OK} code
     *
     * @param type
     *            {@link String} Application or Service
     * @param template
     *            {@link String} templates
     * @param file
     *            {@link MultipartFile} json as string
     * @return {@link ResponseEntity} of the new generated application.
     */
    @RequestMapping(value = "/applications/{type}/{template}", method = RequestMethod.POST, consumes = { "multipart/form-data" })
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "createApplication accepts as path variables type, template and request body as json string and returns ServiceResponse")
    public ResponseEntity<ServiceResponse<String>> createApplication(@PathVariable("type") final String type,
                                                                     @PathVariable("template") final String template,
                                                                     @RequestPart("file") final MultipartFile file) {

        String jsonStr = null;

        if (!(StringUtils.isNotBlank(type) && StringUtils.isNotBlank(template) && file != null)) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.UNKNOWN_APPLICATION_ERROR,
                    "Validation Error:: requires valid data");
        }

        try {
            jsonStr = new String(file.getBytes(), "UTF-8");
        } catch (final IOException e) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.UNKNOWN_APPLICATION_ERROR, e);
        }

        final ApplicationAbstractFactory genericFactory = applicationFactoryProducer.getFactory(type.toUpperCase(), template.toUpperCase(), jsonStr);

        if (genericFactory != null) {
            if (genericFactory instanceof ApplicationFactory) {
                return genericFactory.getApplication().create(jsonStr);
            } else if (genericFactory instanceof MaasFactory) {
                return genericFactory.getMaas().create(jsonStr);
            }
        } else {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.UNKNOWN_APPLICATION_ERROR,
                    "Validation Error:: Due to invalid data - cannot find factory, hence couldn't create application");
        }

        return null;
    }

}