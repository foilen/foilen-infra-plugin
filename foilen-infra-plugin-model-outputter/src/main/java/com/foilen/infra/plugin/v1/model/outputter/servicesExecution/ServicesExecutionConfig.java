/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.outputter.servicesExecution;

import java.util.ArrayList;
import java.util.List;

import com.foilen.smalltools.tools.AbstractBasics;

public class ServicesExecutionConfig extends AbstractBasics {

    private List<ServicesExecutionServiceConfig> services = new ArrayList<>();

    public List<ServicesExecutionServiceConfig> getServices() {
        return services;
    }

    public void setServices(List<ServicesExecutionServiceConfig> services) {
        this.services = services;
    }

}
