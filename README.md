# About

The plugin development environment for Foilen Infra.

License: The MIT License (MIT)


# Projects

- foilen-infra-plugin-model: The common objects models.
- foilen-infra-plugin-model-outputter: Some helper to output configuration from the models.
- foilen-infra-plugin-core-execute: Some Linux and Docker services for the executor.
- foilen-infra-plugin-core-system: All the services definitions for plugins.
- foilen-infra-plugin-core-system-junits: The junits for testing any system implementation.
- foilen-infra-plugin-core-system-fake: An implementation of the services for pluging used in unit tests and standalone tests.
- foilen-infra-plugin-app-test-docker: An application to generate sample resource files and run applications from files.

# Usage

## Dependency

You can see the latest version and the Maven and Gradle settings here:

https://bintray.com/foilen/maven/com.foilen:foilen-infra-plugin-core

## Plugin

- See docs/plugin_creation.odt

## System

- Use foilen-infra-plugin-core-system-common
- Implement all the services:
    - com.foilen.infra.plugin.v1.core.service.IPPluginService
    - com.foilen.infra.plugin.v1.core.service.IPResourceService
    - com.foilen.infra.plugin.v1.core.service.MessagingService
    - com.foilen.infra.plugin.v1.core.service.TimerService
    - com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService
    - com.foilen.infra.plugin.v1.core.service.internal.InternalIPResourceService
- Init the system with InfraPluginCommonInit.init(commonServicesContext, internalServicesContext);

## System test

- Use foilen-infra-plugin-core-system-junits (can be in the same project, but in the "test" configuration)
- Create a unit test that extends AbstractIPResourceServiceTest

# Process

Versioning:
- The version number is in the format MAJOR.MINOR.BUGFIX (e.g 0.1.0).
- The API in a MAJOR release is stable. Everything that will be removed in the next MAJOR release are marked as deprecated.

For changes/removals in the stable API:
- When something is in the stable API, it will be there for all the releases in the same MAJOR version.
- Everything that will be removed in the next MAJOR version is marked as @deprecated and the Javadoc will explain what to use instead if there is a workaround.

# App Test Docker

## Launch the application for testing in Docker (locally)


```bash
# Compile and create image
./create-local-release.sh

USER_ID=$(id -u)

FOLDER_SAMPLE=$(pwd)/sample-templates
FOLDER_IMPORT=$(pwd)/import-resources
FOLDER_PLUGINS_JARS=$(pwd)/plugins-jars
mkdir -p $FOLDER_SAMPLE $FOLDER_IMPORT $FOLDER_PLUGINS_JARS

# Create sample data
docker run -ti \
  --rm \
  --env PLUGINS_JARS=/plugins \
  --user $USER_ID \
  --volume $FOLDER_SAMPLE:/data \
  --volume $FOLDER_PLUGINS_JARS:/plugins \
  foilen-infra-plugin-app-test-docker:master-SNAPSHOT \
  create-sample \
  /data
  
# Create files in "import-resources"

# Import files and execute applications in Docker
docker run -ti \
  --rm \
  --env HOSTFS=/hostfs/ \
  --env PLUGINS_JARS=/plugins \
  --volume $FOLDER_IMPORT:/data \
  --volume $FOLDER_PLUGINS_JARS:/plugins \
  --volume /etc:/hostfs/etc \
  --volume /home:/hostfs/home \
  --volume /usr/bin/docker:/usr/bin/docker \
  --volume /usr/lib/x86_64-linux-gnu/libltdl.so.7.3.1:/usr/lib/x86_64-linux-gnu/libltdl.so.7 \
  --volume /var/run/docker.sock:/var/run/docker.sock \
  --workdir /data \
  foilen-infra-plugin-app-test-docker:master-SNAPSHOT \
  start-resources \
  /data

```
