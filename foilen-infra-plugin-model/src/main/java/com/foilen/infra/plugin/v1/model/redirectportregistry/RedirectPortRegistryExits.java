/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.redirectportregistry;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedirectPortRegistryExits {

    private List<RedirectPortRegistryExit> exits = new ArrayList<>();

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
        RedirectPortRegistryExits other = (RedirectPortRegistryExits) obj;
        if (exits == null) {
            if (other.exits != null) {
                return false;
            }
        } else if (!exits.equals(other.exits)) {
            return false;
        }
        return true;
    }

    public List<RedirectPortRegistryExit> getExits() {
        return exits;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((exits == null) ? 0 : exits.hashCode());
        return result;
    }

    public void setExits(List<RedirectPortRegistryExit> exits) {
        this.exits = exits;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RedirectPortRegistryExits [exits=");
        builder.append(exits);
        builder.append("]");
        return builder.toString();
    }

}
