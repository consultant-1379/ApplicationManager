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

import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.SDK_DELIMITER;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.ericsson.component.aia.sdk.pba.model.AuthorInfo;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;

/**
 * PbaToFlowXmlConverter is a utility class for doing transformation, extraction, conversion ..etc operations on a Pba Model object.
 */
public class PbaToFlowXmlConverter {

    /** The Constant format. */
    private static final SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

    /** The flow xml. WORKING_DIR + SEPARATOR + */
    private static final String FLOW_XML = "flow.vm";

    /**
     * This is a static class, So constructor is made private. Creation of instance for this class should throw IllegalAccessException
     *
     * @throws IllegalAccessException
     *             the illegal access exception
     */
    private PbaToFlowXmlConverter() throws IllegalAccessException {
        throw new IllegalAccessException("PbaToFlowXmlConverter is a utility class!");
    }

    /**
     * createFlowFromPba helps in creating flow xml file from Pba(present in json format).
     *
     * @param pbaInstance
     *            {@link PBAInstance} represents PBA data
     * @return the string
     */
    public static String createFlowFromPba(final PBAInstance pbaInstance) {

        /* first, get and initialize an engine */
        final VelocityEngine engine = new VelocityEngine();
        final Properties prop = new Properties();
        prop.setProperty("resource.loader", "class");
        prop.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        prop.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
        engine.init(prop);

        /* next, get the Template */
        final Template template = engine.getTemplate(FLOW_XML);
        final VelocityContext context = createTemplateContext(pbaInstance);

        /* now render the template into a StringWriter */
        final StringWriter writer = new StringWriter();

        if (template != null) {
            template.merge(context, writer);
        }
        return writer.toString();
    }

    /**
     * createFlowFromPba helps in creating flow xml file from Pba(present in json format).
     *
     * @param pbaInstance
     *            {@link PBAInstance} represents PBA data
     * @param template
     *            the template
     * @return the string
     */
    public static String createFlowFromPba(final PBAInstance pbaInstance, final String template) {

        /* first, get and initialize an engine */
        final VelocityEngine engine = new VelocityEngine();
        engine.init();

        final VelocityContext context = createTemplateContext(pbaInstance);
        /* now render the template into a StringWriter */
        final StringWriter writer = new StringWriter();
        if (template != null) {
            engine.evaluate(context, writer, "datasource", template);
        }

        return writer.toString();
    }

    private static VelocityContext createTemplateContext(final PBAInstance pbaInstance) {
        /* create a context and add data */
        final VelocityContext context = new VelocityContext();
        context.put("ipAdatperlist", pbaInstance.getPba().getExtensionPoints());
        context.put("opAdatperlist", pbaInstance.getPba().getIntegrationPoints());

        final AuthorInfo authorInfo = pbaInstance.getPba().getAuthorInfo();
        if (authorInfo != null) {
            context.put("author", pbaInstance.getPba().getAuthorInfo().getAuthor());
        }
        context.put("creationDate", format.format(new Date()));

        final PbaInfo applicationInfo = pbaInstance.getPba().getApplicationInfo();
        context.put("componentId", applicationInfo.getName() + SDK_DELIMITER + applicationInfo.getVersion());
        context.put("description", applicationInfo.getDescription());
        context.put("name", applicationInfo.getName());
        context.put("className", AppSdkUtil.convertApplicationName(applicationInfo.getName()));
        return context;
    }
}
