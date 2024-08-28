FROM openjdk:8-jdk-alpine
RUN mkdir -p /root/.ssh

COPY application-manager-services-${project.version}.jar /opt/ericsson/application_manager/application-manager-services-${project.version}.jar

ENV JAVA_OPTS="-Xmx2g -verbose:gc -Xloggc:/logs/gc.log -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7070 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/ericsson/application_manager/application-manager-services-${project.version}.jar --spring.profiles.active=container"]