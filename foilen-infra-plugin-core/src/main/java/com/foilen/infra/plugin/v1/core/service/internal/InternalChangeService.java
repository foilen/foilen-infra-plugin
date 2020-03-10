/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.service.internal;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangeExecutionHook;
import com.foilen.infra.plugin.v1.model.resource.IPResource;

/**
 * To manage the changes.
 */
public interface InternalChangeService {

    /**
     * Execute the changes and clear the changes context.
     *
     * @param changes
     *            the changes
     */
    void changesExecute(ChangesContext changes);

    /**
     * Execute the changes and clear the changes context.
     *
     * @param changes
     *            the changes
     * @param extraChangeExecutionHooks
     *            some hooks to add
     */
    void changesExecute(ChangesContext changes, List<ChangeExecutionHook> extraChangeExecutionHooks);

    List<ChangeExecutionHook> getDefaultChangeExecutionHooks();

    /**
     * Add a link.
     *
     * @param fromResourceId
     *            the from resource id
     * @param linkType
     *            the link type
     * @param toResourceId
     *            the to resource id
     */
    void linkAdd(String fromResourceId, String linkType, String toResourceId);

    /**
     * Delete a link.
     *
     * @param fromResourceId
     *            the from resource id
     * @param linkType
     *            the link type
     * @param toResourceId
     *            the to resource id
     * @return true if existed and was removed ; false if already inexistent
     */
    boolean linkDelete(String fromResourceId, String linkType, String toResourceId);

    /**
     * Check if a link exists.
     *
     * @param fromResourceId
     *            the from resource id
     * @param linkType
     *            the link type
     * @param toResourceId
     *            the to resource id
     * @return true if exists
     */
    boolean linkExists(String fromResourceId, String linkType, String toResourceId);

    /**
     * Add a new resource.
     *
     * @param resource
     *            the resource to add
     * @return the persisted resource
     */
    IPResource resourceAdd(IPResource resource);

    /**
     * Delete a resource and its other metadata.
     *
     * @param resourceId
     *            the resource id
     * @return true if existed and was removed ; false if already inexistent
     */
    boolean resourceDelete(String resourceId);

    /**
     * Update a resource.
     *
     * @param previousResource
     *            the resource to update
     * @param updatedResource
     *            the new values
     */
    void resourceUpdate(IPResource previousResource, IPResource updatedResource);

    void setDefaultChangeExecutionHooks(List<ChangeExecutionHook> changeExecutionHooks);

    void setInfiniteLoopTimeoutInMs(long infiniteLoopTimeoutInMs);

    /**
     * Add a tag.
     *
     * @param resourceId
     *            the resource id
     * @param tagName
     *            the tag name
     */
    void tagAdd(String resourceId, String tagName);

    /**
     * Delete a tag.
     *
     * @param resourceId
     *            the resource id
     * @param tagName
     *            the tag name
     * @return true if existed and was removed ; false if already inexistent
     */
    boolean tagDelete(String resourceId, String tagName);

    /**
     * Check if a tag exists.
     *
     * @param resourceId
     *            the resource id
     * @param tagName
     *            the tag name
     * @return true if exists
     */
    boolean tagExists(String resourceId, String tagName);

}
