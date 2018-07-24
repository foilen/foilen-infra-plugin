/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.eventhandler;

import java.util.ArrayList;
import java.util.List;

import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tuple.Tuple2;

public class FinalStateManagedResource {

    private IPResource managedResource;

    private List<Tuple2<String, IPResource>> linksTo = new ArrayList<>();
    private List<String> managedLinksToTypes = new ArrayList<>();

    public void addLinkTo(String linkType, IPResource toResource) {
        linksTo.add(new Tuple2<String, IPResource>(linkType, toResource));
    }

    public void addManagedLinksToType(String... linksToTypes) {
        for (String linksToType : linksToTypes) {
            managedLinksToTypes.add(linksToType);
        }
    }

    public List<Tuple2<String, IPResource>> getLinksTo() {
        return linksTo;
    }

    public List<String> getManagedLinksToTypes() {
        return managedLinksToTypes;
    }

    public IPResource getManagedResource() {
        return managedResource;
    }

    public FinalStateManagedResource setLinksTo(List<Tuple2<String, IPResource>> linksTo) {
        this.linksTo = linksTo;
        return this;
    }

    public FinalStateManagedResource setManagedLinksToTypes(List<String> managedLinksToTypes) {
        this.managedLinksToTypes = managedLinksToTypes;
        return this;
    }

    public FinalStateManagedResource setManagedResource(IPResource managedResource) {
        this.managedResource = managedResource;
        return this;
    }

}
