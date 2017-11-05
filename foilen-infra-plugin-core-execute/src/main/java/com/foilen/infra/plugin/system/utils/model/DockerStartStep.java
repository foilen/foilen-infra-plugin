/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.system.utils.model;

public enum DockerStartStep {

    BUILD_IMAGE, //
    RESTART_CONTAINER, //
    COPY_AND_EXECUTE_IN_RUNNING_CONTAINER, //
    COMPLETED, //

}
