/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IPApplicationDefinitionBuildStep {

    private IPApplicationDefinitionBuildStepType type;
    private String step;

    public IPApplicationDefinitionBuildStep() {
    }

    public IPApplicationDefinitionBuildStep(IPApplicationDefinitionBuildStepType type, String step) {
        this.type = type;
        this.step = step;
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
        IPApplicationDefinitionBuildStep other = (IPApplicationDefinitionBuildStep) obj;
        if (step == null) {
            if (other.step != null) {
                return false;
            }
        } else if (!step.equals(other.step)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    public String getStep() {
        return step;
    }

    public IPApplicationDefinitionBuildStepType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((step == null) ? 0 : step.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public void setType(IPApplicationDefinitionBuildStepType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IPApplicationDefinitionBuildStep [type=");
        builder.append(type);
        builder.append(", step=");
        builder.append(step);
        builder.append("]");
        return builder.toString();
    }

}
