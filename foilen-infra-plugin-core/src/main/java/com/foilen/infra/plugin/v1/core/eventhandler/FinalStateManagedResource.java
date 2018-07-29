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

    private List<Tuple2<IPResource, String>> linksFrom = new ArrayList<>();
    private List<String> managedLinksFromTypes = new ArrayList<>();

    public void addLinkFrom(IPResource fromResource, String linkType) {
        linksFrom.add(new Tuple2<>(fromResource, linkType));
    }

    public void addLinkTo(String linkType, IPResource toResource) {
        linksTo.add(new Tuple2<>(linkType, toResource));
    }

    public void addManagedLinksFromType(String... linksFromTypes) {
        for (String linksFromType : linksFromTypes) {
            managedLinksFromTypes.add(linksFromType);
        }
    }

    public void addManagedLinksToType(String... linksToTypes) {
        for (String linksToType : linksToTypes) {
            managedLinksToTypes.add(linksToType);
        }
    }

    public List<Tuple2<IPResource, String>> getLinksFrom() {
        return linksFrom;
    }

    public List<Tuple2<String, IPResource>> getLinksTo() {
        return linksTo;
    }

    public List<String> getManagedLinksFromTypes() {
        return managedLinksFromTypes;
    }

    public List<String> getManagedLinksToTypes() {
        return managedLinksToTypes;
    }

    public IPResource getManagedResource() {
        return managedResource;
    }

    public FinalStateManagedResource setLinksFrom(List<Tuple2<IPResource, String>> linksFrom) {
        this.linksFrom = linksFrom;
        return this;
    }

    public FinalStateManagedResource setLinksTo(List<Tuple2<String, IPResource>> linksTo) {
        this.linksTo = linksTo;
        return this;
    }

    public FinalStateManagedResource setManagedLinksFromTypes(List<String> managedLinksFromTypes) {
        this.managedLinksFromTypes = managedLinksFromTypes;
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
