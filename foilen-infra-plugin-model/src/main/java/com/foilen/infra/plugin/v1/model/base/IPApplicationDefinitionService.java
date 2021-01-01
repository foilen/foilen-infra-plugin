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

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IPApplicationDefinitionService {

    private String name;
    private String workingDirectory = null;
    private String command;
    private Long runAs = null;

    public IPApplicationDefinitionService() {
    }

    public IPApplicationDefinitionService(String name, String command) {
        this.name = name;
        this.command = command;
    }

    public IPApplicationDefinitionService(String name, String command, Long runAs) {
        this.name = name;
        this.command = command;
        this.runAs = runAs;
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
        IPApplicationDefinitionService other = (IPApplicationDefinitionService) obj;
        if (command == null) {
            if (other.command != null) {
                return false;
            }
        } else if (!command.equals(other.command)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (runAs == null) {
            if (other.runAs != null) {
                return false;
            }
        } else if (!runAs.equals(other.runAs)) {
            return false;
        }
        if (workingDirectory == null) {
            if (other.workingDirectory != null) {
                return false;
            }
        } else if (!workingDirectory.equals(other.workingDirectory)) {
            return false;
        }
        return true;
    }

    public String getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }

    public Long getRunAs() {
        return runAs;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((runAs == null) ? 0 : runAs.hashCode());
        result = prime * result + ((workingDirectory == null) ? 0 : workingDirectory.hashCode());
        return result;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRunAs(Long runAs) {
        this.runAs = runAs;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IPApplicationDefinitionService [name=");
        builder.append(name);
        builder.append(", workingDirectory=");
        builder.append(workingDirectory);
        builder.append(", command=");
        builder.append(command);
        builder.append(", runAs=");
        builder.append(runAs);
        builder.append("]");
        return builder.toString();
    }

}
