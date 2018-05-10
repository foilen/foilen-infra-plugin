# About

The plugin development environment for Foilen Infra.

License: The MIT License (MIT)


# Projects

- foilen-infra-plugin-model: The common objects models.
- foilen-infra-plugin-model-outputter: Some helper to output configuration from the models.
- foilen-infra-plugin-core: Some Linux and Docker services for the executor.

# Usage

## Dependency

You can see the latest version and the Maven and Gradle settings here:

https://bintray.com/foilen/maven/com.foilen:foilen-infra-plugin-core

## Plugin

- See docs/plugin_creation.odt

# Process

Versioning:
- The version number is in the format MAJOR.MINOR.BUGFIX (e.g 0.1.0).
- The API in a MAJOR release is stable. Everything that will be removed in the next MAJOR release are marked as deprecated.

For changes/removals in the stable API:
- When something is in the stable API, it will be there for all the releases in the same MAJOR version.
- Everything that will be removed in the next MAJOR version is marked as @deprecated and the Javadoc will explain what to use instead if there is a workaround.
