package com.ericsson.component.aia.sdk.applicationmanager.maas.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.sdk.applicationmanager.model.MAASInstance;
import com.ericsson.component.aia.sdk.pba.exception.InvalidPbaException;
import com.ericsson.component.aia.sdk.pba.report.ReportLevel;
import com.ericsson.component.aia.sdk.pba.report.ValidatedMessage;
import com.ericsson.component.aia.sdk.pba.report.ValidationReport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.Dereferencing;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfigurationBuilder;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * MAAS utility that validates the maas schema against the maas ui
 */
public class MAASSchemaTool {

    private static final String EXCEPTION_MESSAGE = "Exception occurred while creating PBAInstance";

    /**
     * Logger for schema tool.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MAASSchemaTool.class);

    private static final String MAAS_SCHEMA_IN_CLASSPATH = "config/maas-schema.json";

    /**
     * Used to denote if the schema has to be loaded from the classpath.
     */
    private final boolean loadSchemaFromClasspath;

    /**
     * The json pba schema as {@code String} that this tool works on.
     */
    private String maasSchema;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The default constructor to use if the schema has to be loaded from the classpath. The file "maas-schema.json" is the file resource expected in
     * the classpath.
     */
    public MAASSchemaTool() {
        loadSchemaFromClasspath = true;
    }

    /**
     * Constructor that instantiates the schema tool with jsonSchema supplied as String.
     *
     * @param maasSchema
     *            - the maas schema as String
     */
    public MAASSchemaTool(final String maasSchema) {
        loadSchemaFromClasspath = false;
        this.maasSchema = maasSchema;
    }

    /**
     * Method returns instance of the model from the maas json data supplied.
     *
     * @param maasData
     *            - the maas data as json.
     * @return {@code MAASInstance} if data is valid against the schema, null - if the data contains errors.
     *
     * @throws InvalidMAASException
     *             - if the MAAS could not be processed.
     */
    public MAASInstance getMAASModelInstance(final String maasData) {
        LOG.info("Start getMAASModelInstance");
        try {
            final ValidationReport report = validateMAASAgainstSchema(maasData);
            final Set<ValidatedMessage> errorMessages = report.getLogLevelMessages(ReportLevel.ERROR);
            if (!errorMessages.isEmpty()) {
                throw new InvalidMAASException(errorMessages.toString());
            }
            return objectMapper.readValue(maasData, MAASInstance.class);
        } catch (final IOException exp) {
            throw new InvalidMAASException("could not parse maasData", exp);
        }
    }

    /**
     * Method validates the json maas data against the maas schema.
     *
     * @param maasData
     *            - the pba data as json.
     * @return - A Set of {@code ValidatedMessage} if this pba data contains invalid attributes, otherwise an empty set.
     * @throws InvalidPbaException
     *             - if the PBA could not be processed.
     */
    public ValidationReport validateMAASAgainstSchema(final String maasData) {
        LOG.info("Start validateMAASAgainstSchema");
        try {
            final String pbaSchema = getSchema();
            final JsonNode schemaNode = objectMapper.readTree(pbaSchema);
            final LoadingConfigurationBuilder builder = LoadingConfiguration.newBuilder();
            builder.dereferencing(Dereferencing.INLINE).freeze();
            final JsonSchemaFactory schemaFact = JsonSchemaFactory.newBuilder().setLoadingConfiguration(builder.freeze()).freeze();
            final JsonNode jsonNode = objectMapper.readTree(maasData);
            final ProcessingReport processingReport = schemaFact.getJsonSchema(schemaNode).validate(jsonNode, true);
            LOG.debug(processingReport.isSuccess() + "" + processingReport);
            return getValidationReport(processingReport);
        } catch (final IOException | ProcessingException exp) {
            throw new InvalidMAASException(EXCEPTION_MESSAGE, exp);
        }
    }

    /**
     * This method is used to get a String as a JSON Schema.
     *
     * @return The JSON Scheme as a String.
     * @throws IOException
     *             Exception thrown if there is a problem read the schema file.
     */
    public String getSchema() throws IOException {
        if (loadSchemaFromClasspath) {
            this.maasSchema = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(MAAS_SCHEMA_IN_CLASSPATH), "UTF-8");
        }
        return this.maasSchema;
    }

    private ValidationReport getValidationReport(final ProcessingReport processingReport) {
        final Iterator<ProcessingMessage> messagesIter = processingReport.iterator();

        final Set<ValidatedMessage> validationMessages = new HashSet<>();
        while (messagesIter.hasNext()) {
            final ProcessingMessage message = messagesIter.next();
            final ValidatedMessage vMessage = new ValidatedMessage();
            vMessage.setMessage(message.getMessage());
            vMessage.setDetailedMessage(message.toString());

            validationMessages.add(vMessage);
        }
        return new ValidationReport(validationMessages);
    }

    /**
     * File Conversion utility. Reads a file and converts it into {@code String}
     *
     * @param fileName
     *            - absolute path to the file.
     * @return - String representation of the contents of the file.
     * @throws IOException
     *             - If the file could not be read.
     */
    public static String readStreamAsStringFronFile(final Path fileName) throws IOException {
        return new String(Files.readAllBytes(fileName), StandardCharsets.UTF_8);
    }

}
