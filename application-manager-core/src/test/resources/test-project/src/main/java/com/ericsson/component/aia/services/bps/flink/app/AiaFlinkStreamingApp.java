/**
 *
 * (C) Copyright LM Ericsson System Expertise AT/LMI, 2016
 *
 * The copyright to the computer program(s) herein is the property of Ericsson  System Expertise EEI, Sweden.
 * The program(s) may be used and/or copied only with the written permission from Ericsson System Expertise
 * AT/LMI or in  * accordance with the terms and conditions stipulated in the agreement/contract under which
 * the program(s) have been supplied.
 *
 */
package com.ericsson.component.aia.services.bps.flink.app;


import org.apache.avro.generic.GenericRecord;
import org.apache.flink.streaming.api.datastream.DataStream;
import com.ericsson.component.aia.services.bps.core.service.streams.BpsStream;
import com.ericsson.component.aia.services.bps.flink.jobrunner.BpsFlinkStreamingJobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class extends {@link BpsFlinkStreamingJobRunner} and provides logic for reading {@link DataStream} from data sources and applying
 * transformation and writing the result to configured data sinks
 */
public class AiaFlinkStreamingApp extends BpsFlinkStreamingJobRunner{

private static final long serialVersionUID=1L;

private static final Logger LOGGER=LoggerFactory.getLogger(AiaFlinkStreamingApp.class);

@SuppressWarnings("unchecked")
@Override
public void executeJob(){

//Gets Kafka Stream as defined in flow.xml
final BpsStream<DataStream<GenericRecord>>flinkStream=getBpsInputStreams().<DataStream<GenericRecord>>getStreams(
    "input-stream");
final DataStream<GenericRecord>dataStream=flinkStream.getStreamRef();

        /*
         * To save output to kakfa, Jdbc use persistDataStream which will write the data on the external source specified
         * in flow.xml
         */
    //persistDataStream(dataStream)
    LOGGER.info("EpsFlinkStreamingHandler executeJob successfully completed");

    }
@Override
public void cleanUp(){
    }
    }
