To run the application locally use:
java -jar ./target/application-manager-services-1.0.68-SNAPSHOT.jar \
--git.service.url=http://10.44.149.55/api/v1 \
--git.service.access.token=5702924bc16245b020ddf4b31eff4b021ee3599d 
--docker.repo.server.url=armdocker.rnd.ericsson.se" \
--docker.repo.application.path=aia/test \
--artifactory.server.url=https://arm.epk.ericsson.se/artifactory/ \
--artifactory.server.path=docker-v2-global-local &

-----------------------------------------------------------------------------------------------------

To run the application on production as a jar use:
java -jar application-manager-services-1.0.68-SNAPSHOT.jar \
--spring.profiles.active=production &



-----------------------------------------------------------------------------------------------------

To run the application within a container use:
java -jar application-manager-services-1.0.68-SNAPSHOT.jar --spring.profiles.active=container

This profile will match spring properties to the environmental properties:
docker.client.username = ${DOCKER_USER_NAME}
docker.client.password = ${DOCKER_USER_PASSWORD}

git.service.type = ${GIT_SERVICE_TYPE}
git.service.url = ${GIT_SERVICE_URL}
git.service.access.token = ${GIT_SERVICE_ACCESS_TOKEN}
git.service.default.template = ${GIT_SERVICE_DEFAULT_TEMPLATE}
git.service.default.template.name = ${GIT_SERVICE_DEFAULT_TEMPLATE_NAME}

git.service.access.token = ${GIT_SERVICE_ACCESS_TOKEN}

docker.repo.server.url = ${DOCKER_REPO_URL}
docker.repo.application.path = ${DOCKER_REPO_PATH}
artifactory.server.url = ${ARTIFACTORY_URL}

datastore.host = ${DATASTORE_HOST}
