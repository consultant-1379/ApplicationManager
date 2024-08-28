# Build a docker image using the command:
mvn clean install -Pbuild-docker-image

# Run the docker image using the command:
docker run -e "GIT_SERVICE_TYPE=GOGS" \
-e "DOCKER_USER_NAME=eappsdk" \
-e "DOCKER_USER_PASSWORD=Kgidwl*fak$9yq8c" \
-e "GIT_SERVICE_URL=http://10.44.149.55/api/v1" \
-e "GIT_SERVICE_ACCESS_TOKEN=b35b6595056573f80c60819a63e0a3947b37137d" \
-e "DOCKER_REPO_URL=armdocker.rnd.ericsson.se" \
-e "DOCKER_REPO_PATH=aia/test" \
-e "ARTIFACTORY_URL=https://arm.epk.ericsson.se/artifactory/" \
-e "DATASTORE_HOST=172.17.0.7" \
-p 6868:6868 \
-v /root/.ssh/:/root/.ssh/ \
-v /var/run/docker.sock:/var/run/docker.sock/ \
-t armdocker.rnd.ericsson.se/aia/application-manager-services