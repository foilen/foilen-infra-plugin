/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.haproxy;

public class HaProxyConfigEndpoint implements Comparable<HaProxyConfigEndpoint> {

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HaProxyConfigEndpoint other = (HaProxyConfigEndpoint) obj;
        if (hostPort == null) {
            if (other.hostPort != null) {
                return false;
            }
        } else if (!hostPort.equals(other.hostPort)) {
            return false;
        }
        if (isSsl != other.isSsl) {
            return false;
        }
        return true;
    }

    public String getHostPort() {
        return hostPort;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hostPort == null) ? 0 : hostPort.hashCode());
        result = prime * result + (isSsl ? 1231 : 1237);
        return result;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HaProxyConfigEndpoint [hostPort=");
        builder.append(hostPort);
        builder.append(", isSsl=");
        builder.append(isSsl);
        builder.append("]");
        return builder.toString();
    }

}
