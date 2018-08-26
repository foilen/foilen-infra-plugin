/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.resource;

import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.foilen.smalltools.tools.JsonTools;

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractIPResource implements IPResource {

    private Long internalId;
    private String resourceEditorName;
    private SortedMap<String, String> meta = new TreeMap<>();

    @Override
    public IPResource deepClone() {
        IPResource cloned = JsonTools.clone(this);
        cloned.setInternalId(this.internalId);
        return cloned;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, "internalId");
    }

    @JsonIgnore
    @Override
    public Long getInternalId() {
        return internalId;
    }

    @Override
    public SortedMap<String, String> getMeta() {
        return meta;
    }

    @Override
    public String getResourceEditorName() {
        return resourceEditorName;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "internalId");
    }

    @Override
    public void setInternalId(Long internalId) {
        this.internalId = internalId;
    }

    @Override
    public void setMeta(SortedMap<String, String> meta) {
        this.meta = meta;
    }

    @Override
    public void setResourceEditorName(String resourceEditorName) {
        this.resourceEditorName = resourceEditorName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
