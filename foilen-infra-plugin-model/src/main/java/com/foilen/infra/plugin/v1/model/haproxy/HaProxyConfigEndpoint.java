/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.haproxy;

import com.foilen.smalltools.tools.AbstractBasics;

public class HaProxyConfigEndpoint extends AbstractBasics implements Comparable<HaProxyConfigEndpoint> {

    private String hostPort;
    private boolean isSsl;

    public HaProxyConfigEndpoint() {
    }

    public HaProxyConfigEndpoint(String hostPort) {
        this.hostPort = hostPort;
    }

    /**
     * An endpoint.
     *
     * @param host
     *            (optional) the hostname
     * @param port
     *            the port
     */
    public HaProxyConfigEndpoint(String host, int port) {
        if (host == null) {
            host = "192.168.255.1";
        }
        this.hostPort = host + ":" + port;
    }

    @Override
    public int compareTo(HaProxyConfigEndpoint o) {
        int result = hostPort.compareTo(o.hostPort);
        if (result != 0) {
            return result;
        }

        if (isSsl == o.isSsl) {
            return 0;
        }
        if (!isSsl) {
            return -1;
        }
        return 1;
    }

    public String getHostPort() {
        return hostPort;
    }

    public boolean isSsl() {
        return isSsl;
    }

    public HaProxyConfigEndpoint setHostPort(String hostPort) {
        this.hostPort = hostPort;
        return this;
    }

    public HaProxyConfigEndpoint setSsl(boolean isSsl) {
        this.isSsl = isSsl;
        return this;
    }

}
