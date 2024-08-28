package com.ericsson.component.aia.sdk.applicationmanager.maas.mapper;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.component.aia.sdk.applicationmanager.model.MAASInstance;

/**
 * MassMapper is main mapper file which stores in map dynamic configuration placeholders and maas values
 */
public class MassMapper implements SDKMapper {

    //TODO:: move constants to ENUM, as these are valid event types
    private final static String SUPPORTED_EVENT_CTR_DESC = "celltrace";
    private final static String SUPPORTED_EVENT_CTUM = "ctum";
    private final static String SUPPORTED_EVENT_CTR = "CTR";
    private final Map<String, Map<String, String>> replaceValues;
    private final StringBuilder resultParserType = new StringBuilder();

    private MAASInstance maasInstance;
    private MAASFileMapper maasFileMapper;

    /**
     * MassMapper constructor
     *
     * @param replaceValues
     *            replaceValues
     */
    public MassMapper(final Map<String, Map<String, String>> replaceValues) {
        this.replaceValues = replaceValues;
    }

    @Override
    public void mapper(final MAASInstance maasInstance) {

        this.maasInstance = maasInstance;

        //prepare values to replace config file
        prepareConfig();

        maasFileMapper = MAASFileMapper.FLOW_INPUT_SOURCE_DESC;

        if (null != maasInstance.getMaas().getUiDef().getParserDef().getName()) {

            if (maasInstance.getMaas().getUiDef().getParserDef().getName().equalsIgnoreCase(SUPPORTED_EVENT_CTR)) {

                populateReplaceValues(maasFileMapper, SUPPORTED_EVENT_CTR_DESC);
            } else {
                populateReplaceValues(maasFileMapper, SUPPORTED_EVENT_CTUM);
            }
        }

        // ST publisher
        maasFileMapper = MAASFileMapper.STREAM_TERMINATOR_PUBLISHER;
        populateReplaceValues(maasFileMapper, maasInstance.getMaas().getUiDef().getInputAdapter().getTopicName());

        // input adapter
        maasFileMapper = MAASFileMapper.INPUT_ADAPTER_TOPIC_NAME;
        populateReplaceValues(maasFileMapper, maasInstance.getMaas().getUiDef().getInputAdapter().getTopicName());

        // output adapter
        maasFileMapper = MAASFileMapper.OUTPUT_ADAPTER_TOPIC_NAME;
        populateReplaceValues(maasFileMapper, maasInstance.getMaas().getUiDef().getOutputAdapter().getTopicName());

        // MASS UI
        prepareMaasUI();

        // output adapter for pba
        maasFileMapper = MAASFileMapper.PBA_PUBLISHER_TOPIC_NAME;
        populateReplaceValues(maasFileMapper, maasInstance.getMaas().getUiDef().getOutputAdapter().getTopicName());

    }

    private void populateReplaceValues(final MAASFileMapper maasFileMapper, final String value) {

        Map<String, String> map = null;
        final File file = maasFileMapper.getFile();

        if (replaceValues.get(file.getName()) != null) {
            map = replaceValues.get(file.getName());
        } else {
            map = new HashMap<String, String>();
        }

        map.put(maasFileMapper.getPropValue(), value);
        replaceValues.put(file.getName(), map);
    }

    private void prepareConfig() {

        maasFileMapper = MAASFileMapper.CONFIG_INPUT_SOURCE;
        populateReplaceValues(maasFileMapper, maasInstance.getMaas().getUiDef().getInputSource());

        maasFileMapper = MAASFileMapper.CONFIG_PARSER_NAME;
        populateReplaceValues(maasFileMapper, maasInstance.getMaas().getUiDef().getParserDef().getName());

        maasFileMapper = MAASFileMapper.CONFIG_PARSER_TYPES;

        if (maasInstance.getMaas().getUiDef().getParserDef().getTypes().size() != 0) {

            for (final String maasParserType : maasInstance.getMaas().getUiDef().getParserDef().getTypes()) {
                if (resultParserType.length() > 0) {
                    resultParserType.append(",");
                }

                resultParserType.append(maasParserType);

            }
            populateReplaceValues(maasFileMapper, resultParserType.toString());
        }
    }

    private void prepareMaasUI() {

        maasFileMapper = MAASFileMapper.MAAS_UI_SOURCE_INPUT;
        populateReplaceValues(maasFileMapper, maasInstance.getMaas().getUiDef().getInputSource());

        maasFileMapper = MAASFileMapper.MAAS_UI_PARSERDEF_NAME;
        populateReplaceValues(maasFileMapper, maasInstance.getMaas().getUiDef().getParserDef().getName());

        maasFileMapper = MAASFileMapper.MAAS_UI_PARSERDEF_DESC;
        populateReplaceValues(maasFileMapper, resultParserType.toString());

        maasFileMapper = MAASFileMapper.MAAS_UI_INPUT_ADAPTER_TOPIC_NAME;
        populateReplaceValues(maasFileMapper, maasInstance.getMaas().getUiDef().getInputAdapter().getTopicName());

        maasFileMapper = MAASFileMapper.MAAS_UI_OUTPUT_ADAPTER_TOPIC_NAME;
        populateReplaceValues(maasFileMapper, maasInstance.getMaas().getUiDef().getOutputAdapter().getTopicName());
    }

}