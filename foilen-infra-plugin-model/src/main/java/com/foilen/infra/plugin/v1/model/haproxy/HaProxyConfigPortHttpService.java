/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.haproxy;

import java.util.Set;
import java.util.TreeSet;

public class HaProxyConfigPortHttpService {

    protected Set<HaProxyConfigEndpoint> endpoints = new TreeSet<>();

    public HaProxyConfigPortHttpService addEndpointHostPorts(HaProxyConfigEndpoint... endpoints) {
        for (HaProxyConfigEndpoint endpoint : endpoints) {
            this.endpoints.add(endpoint);
        }
        return this;
    }

    public Set<HaProxyConfigEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<HaProxyConfigEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

}
