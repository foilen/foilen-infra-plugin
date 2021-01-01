/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.foilen.smalltools.JavaEnvironmentValues;

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IPApplicationDefinitionPortRedirect {

    public static final String LOCAL_MACHINE = "localhost";

    private Integer localPort;
    private String toMachine;
    private String toContainerName;
    private String toEndpoint;

    public IPApplicationDefinitionPortRedirect() {
    }

    public IPApplicationDefinitionPortRedirect(Integer localPort, String toMachine, String toContainerName, String toEndpoint) {
        this.localPort = localPort;
        this.toMachine = toMachine;
        this.toContainerName = toContainerName;
        this.toEndpoint = toEndpoint;
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
        IPApplicationDefinitionPortRedirect other = (IPApplicationDefinitionPortRedirect) obj;
        if (localPort == null) {
            if (other.localPort != null) {
                return false;
            }
        } else if (!localPort.equals(other.localPort)) {
            return false;
        }
        if (toContainerName == null) {
            if (other.toContainerName != null) {
                return false;
            }
        } else if (!toContainerName.equals(other.toContainerName)) {
            return false;
        }
        if (toEndpoint == null) {
            if (other.toEndpoint != null) {
                return false;
            }
        } else if (!toEndpoint.equals(other.toEndpoint)) {
            return false;
        }
        if (toMachine == null) {
            if (other.toMachine != null) {
                return false;
            }
        } else if (!toMachine.equals(other.toMachine)) {
            return false;
        }
        return true;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public String getMachineContainerEndpoint() {
        return toMachine + "/" + toContainerName + "/" + toEndpoint;
    }

    public String getToContainerName() {
        return toContainerName;
    }

    public String getToEndpoint() {
        return toEndpoint;
    }

    public String getToMachine() {
        return toMachine;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((localPort == null) ? 0 : localPort.hashCode());
        result = prime * result + ((toContainerName == null) ? 0 : toContainerName.hashCode());
        result = prime * result + ((toEndpoint == null) ? 0 : toEndpoint.hashCode());
        result = prime * result + ((toMachine == null) ? 0 : toMachine.hashCode());
        return result;
    }

    public boolean isToLocalMachine() {
        return IPApplicationDefinitionPortRedirect.LOCAL_MACHINE.equals(toMachine) || JavaEnvironmentValues.getHostName().equals(toMachine);
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public void setToContainerName(String toInstanceName) {
        this.toContainerName = toInstanceName;
    }

    public void setToEndpoint(String toEndpoint) {
        this.toEndpoint = toEndpoint;
    }

    public void setToMachine(String toMachine) {
        this.toMachine = toMachine;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IPApplicationDefinitionPortRedirect [localPort=");
        builder.append(localPort);
        builder.append(", toMachine=");
        builder.append(toMachine);
        builder.append(", toContainerName=");
        builder.append(toContainerName);
        builder.append(", toEndpoint=");
        builder.append(toEndpoint);
        builder.append("]");
        return builder.toString();
    }

}
