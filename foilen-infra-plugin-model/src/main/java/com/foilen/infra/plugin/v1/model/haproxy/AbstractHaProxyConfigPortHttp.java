/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.haproxy;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.foilen.smalltools.tools.CollectionsTools;

public abstract class AbstractHaProxyConfigPortHttp<T extends HaProxyConfigPortHttpService> extends HaProxyConfigPort {

    private T defaultService;
    private Map<String, T> serviceByHostname = new TreeMap<>();

    public void addService(Collection<String> hostnames, HaProxyConfigEndpoint... endpoints) {
        for (String hostname : hostnames) {
            CollectionsTools.getOrCreateEmpty(serviceByHostname, hostname, getConfigType()).addEndpointHostPorts(endpoints);
        }
    }

    public void addService(String hostname, HaProxyConfigEndpoint... endpoints) {
        CollectionsTools.getOrCreateEmpty(serviceByHostname, hostname, getConfigType()).addEndpointHostPorts(endpoints);
    }

    public abstract T createConfig();

    public abstract Class<T> getConfigType();

    public T getDefaultService() {
        return defaultService;
    }

    public Map<String, T> getServiceByHostname() {
        return serviceByHostname;
    }

    public void setDefaultService(T defaultService) {
        this.defaultService = defaultService;
    }

    public void setServiceByHostname(Map<String, T> serviceByHostname) {
        this.serviceByHostname = serviceByHostname;
    }

}
