/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.exception;

import com.foilen.infra.plugin.v1.model.resource.IPResource;

public class ResourcePrimaryKeyCollisionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourcePrimaryKeyCollisionException() {
        super("A resource with the same primary key already exists");
    }

    public ResourcePrimaryKeyCollisionException(IPResource resource) {
        super("A resource with the same primary key already exists : " + resource);
    }

}
