/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.eventhandler;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tuple.Tuple3;

/**
 * To handle updates to any resources update event.
 */
public interface UpdateEventHandler<R extends IPResource> {

    /**
     * Triggered when a new resource is created.
     *
     * @param services
     *            the services you can use
     * @param changes
     *            any changes you want to do
     * @param resource
     *            the new resource
     */
    void addHandler(CommonServicesContext services, ChangesContext changes, R resource);

    /**
     * Triggered when maybe an update would be required (e.g after upgrading a plugin).
     *
     * @param services
     *            the services you can use
     * @param changes
     *            any changes you want to do
     * @param resource
     *            the resource to check and update
     */
    void checkAndFix(CommonServicesContext services, ChangesContext changes, R resource);

    /**
     * Triggered when another resource directly linked to this one was updated.
     *
     * @param services
     *            the services you can use
     * @param changes
     *            any changes you want to do
     * @param resource
     *            the resource to check and update
     */
    default void checkDirectLinkChanged(CommonServicesContext services, ChangesContext changes, R resource) {
        checkAndFix(services, changes, resource);
    }

    /**
     * Triggered when another resource that is not directly linked to this one, but that is still linked by more depth, was updated.
     *
     * @param services
     *            the services you can use
     * @param changes
     *            any changes you want to do
     * @param resource
     *            the resource to check and update
     */
    default void checkFarLinkChanged(CommonServicesContext services, ChangesContext changes, R resource) {
        checkAndFix(services, changes, resource);
    }

    /**
     * Triggered when a resource is deleted.
     *
     * @param services
     *            the services you can use
     * @param changes
     *            any changes you want to do
     * @param resource
     *            the deleted resource
     * @param previousLinks
     *            the links that were on the deleted resource
     */
    void deleteHandler(CommonServicesContext services, ChangesContext changes, R resource, List<Tuple3<IPResource, String, IPResource>> previousLinks);

    Class<R> supportedClass();

    /**
     * Triggered when a resource is updated, a tag is changed or a link is changed.
     *
     * @param services
     *            the services you can use
     * @param changes
     *            any changes you want to do
     * @param previousResource
     *            the previous value of the resource
     * @param newResource
     *            the updated resource
     */
    void updateHandler(CommonServicesContext services, ChangesContext changes, R previousResource, R newResource);
}
