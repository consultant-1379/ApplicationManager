# **********************************************************************
# Ericsson Radio Systems AB                                     SCRIPT
# **********************************************************************
#
# (c) Ericsson Radio Systems AB 2016 - All rights reserved.
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Radio Systems AB, Sweden. The programs may be used and/or
# copied only with the written permission from Ericsson Radio Systems AB
# or in accordance with the terms and conditions stipulated in the
# agreement/contract under which the program(s) have been supplied.
#
# **********************************************************************
#   Command Section
#
# **********************************************************************
#
#   Main body of program
#
# ********************************************************************
#
#Pull flink Base image from Docker Repository
FROM armdocker.rnd.ericsson.se/aia/base/flink-base/filnk-1.1.3/0.0.1

#Created flink-app folder so that all flink application dependencies goes into this
CMD mkdir -p  /flink-app

#Copy init start script
COPY config/initialization_script /flink-app/

#Copy flink app jar
ADD target/aia-flink-streaming-deployable.jar /flink-app/

#Copy Config files
COPY config/config.properties /flink-app/

#Run this script when docker images gets initialized 
ENTRYPOINT ["/flink-app/initialization_script"]