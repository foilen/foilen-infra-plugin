/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.eventhandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.foilen.infra.plugin.v1.model.resource.IPResource;

/**
 * The description of the desired final state of some managed resources.
 */
public class FinalStateManagedResourcesUpdateEventHandlerContext<R extends IPResource> {

    private R oldResource;
    private R resource;
    private boolean requestUpdateResource;

    private List<FinalStateManagedResource> managedResources = new ArrayList<>();
    private List<Class<? extends IPResource>> managedResourceTypes = new ArrayList<>();

    public void addManagedResources(FinalStateManagedResource... resources) {
        for (FinalStateManagedResource resource : resources) {
            managedResources.add(resource);
        }
    }

    @SafeVarargs
    public final void addManagedResourceTypes(Class<? extends IPResource>... types) {
        managedResourceTypes.addAll(Arrays.asList(types));
    }

    public List<FinalStateManagedResource> getManagedResources() {
        return managedResources;
    }

    public List<Class<? extends IPResource>> getManagedResourceTypes() {
        return managedResourceTypes;
    }

    public R getOldResource() {
        return oldResource;
    }

    public R getResource() {
        return resource;
    }

    public boolean isRequestUpdateResource() {
        return requestUpdateResource;
    }

    public void setManagedResources(List<FinalStateManagedResource> managedResources) {
        this.managedResources = managedResources;
    }

    public void setManagedResourceTypes(List<Class<? extends IPResource>> managedResourceTypes) {
        this.managedResourceTypes = managedResourceTypes;
    }

    public void setOldResource(R oldResource) {
        this.oldResource = oldResource;
    }

    public void setRequestUpdateResource(boolean requestUpdateResource) {
        this.requestUpdateResource = requestUpdateResource;
    }

    public void setResource(R resource) {
        this.resource = resource;
    }

}
