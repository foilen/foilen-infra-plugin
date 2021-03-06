/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.resource;

import java.util.SortedMap;

/**
 * A resource manageable by the plugin system. Better to extend {@link AbstractIPResource}.
 *
 * You must implements {@link Object#hashCode()} and {@link Object#equals(Object)}. Using {@link AbstractIPResource}, it will automatically provide an implementation that uses reflection.
 */
public interface IPResource {

    IPResource deepClone();

    /**
     * The internal id. (do not touch it)
     *
     * @return the id
     */
    String getInternalId();

    /**
     * Some meta information.
     *
     * @return some meta information
     */
    SortedMap<String, String> getMeta();

    InfraPluginResourceCategory getResourceCategory();

    /**
     * The description of this resource (for display purpose).
     *
     * @return the description to display
     */
    String getResourceDescription();

    /**
     * The name of the editor for that resource.
     *
     * @return the name of the editor
     */
    String getResourceEditorName();

    /**
     * The name of this resource (for display purpose).
     *
     * @return the name to display
     */
    String getResourceName();

    /**
     * The internal id. (do not touch it)
     *
     * @param internalId
     *            the id
     */
    void setInternalId(String internalId);

    /**
     * Some meta information.
     *
     * @param meta
     *            some meta information
     */
    void setMeta(SortedMap<String, String> meta);

    /**
     * The name of the editor for that resource.
     *
     * @param resourceEditorName
     *            the name of the editor
     */
    void setResourceEditorName(String resourceEditorName);

}
