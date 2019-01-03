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

public class HaProxyConfigPortHttp extends HaProxyConfigPort {

    protected HaProxyConfigPortHttpService defaultService;
    protected Map<String, HaProxyConfigPortHttpService> serviceByHostname = new TreeMap<>();

    public void addService(Collection<String> hostnames, HaProxyConfigEndpoint... endpoints) {
        for (String hostname : hostnames) {
            CollectionsTools.getOrCreateEmpty(serviceByHostname, hostname, HaProxyConfigPortHttpService.class).addEndpointHostPorts(endpoints);
        }
    }

    public void addService(String hostname, HaProxyConfigEndpoint... endpoints) {
        CollectionsTools.getOrCreateEmpty(serviceByHostname, hostname, HaProxyConfigPortHttpService.class).addEndpointHostPorts(endpoints);
    }

    public HaProxyConfigPortHttpService getDefaultService() {
        return defaultService;
    }

    public Map<String, HaProxyConfigPortHttpService> getServiceByHostname() {
        return serviceByHostname;
    }

    public void setDefaultService(HaProxyConfigPortHttpService defaultService) {
        this.defaultService = defaultService;
    }

    public void setServiceByHostname(Map<String, HaProxyConfigPortHttpService> serviceByHostname) {
        this.serviceByHostname = serviceByHostname;
    }

}
