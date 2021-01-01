/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.eventhandler.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.UpdatedResource;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tuple.Tuple3;

public class ChangesEventHandlerResourceStream<R extends IPResource> {

    private List<R> resources = new ArrayList<>();
    private Class<R> resourceType;

    public ChangesEventHandlerResourceStream(Class<R> resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Get the stream of all the resources.
     *
     * @return the resources stream
     */
    public Stream<R> getResourcesStream() {
        return resources.stream();
    }

    @SuppressWarnings("unchecked")
    public <T extends IPResource> ChangesEventHandlerResourceStream<R> linksAddFrom(List<Tuple3<IPResource, String, IPResource>> links, String linkType, Class<T> toResourceType) {

        links.stream() //
                .filter(l -> resourceType.isAssignableFrom(l.getA().getClass()) && (StringTools.safeEquals(linkType, l.getB()) && toResourceType.isAssignableFrom(l.getC().getClass()))) //
                .forEach(r -> resources.add((R) r.getA()));

        return this;
    }

    /**
     * Add all the from and to resources from the links list that are of the needed type.
     *
     * @param links
     *            the links
     * @return this
     */
    @SuppressWarnings("unchecked")
    public ChangesEventHandlerResourceStream<R> linksAddFromAndTo(List<Tuple3<IPResource, String, IPResource>> links) {

        links.stream() //
                .filter(l -> resourceType.isAssignableFrom(l.getA().getClass()) || resourceType.isAssignableFrom(l.getC().getClass())) //
                .flatMap(l -> {
                    List<R> elements = new ArrayList<>();
                    if (resourceType.isAssignableFrom(l.getA().getClass())) {
                        elements.add((R) l.getA());
                    }
                    if (resourceType.isAssignableFrom(l.getC().getClass())) {
                        elements.add((R) l.getC());
                    }
                    return elements.stream();
                }) //
                .forEach(r -> resources.add(r));

        return this;

    }

    @SuppressWarnings("unchecked")
    public ChangesEventHandlerResourceStream<R> linksAddTo(List<Tuple3<IPResource, String, IPResource>> links, String linkType) {

        links.stream() //
                .filter(l -> (StringTools.safeEquals(linkType, l.getB()) && resourceType.isAssignableFrom(l.getC().getClass()))) //
                .forEach(r -> resources.add((R) r.getC()));

        return this;
    }

    @SuppressWarnings("unchecked")
    public ChangesEventHandlerResourceStream<R> linksAddTo(List<Tuple3<IPResource, String, IPResource>> links, String[] linkTypes) {

        List<String> linkTypesAsList = Arrays.asList(linkTypes);
        links.stream() //
                .filter(l -> linkTypesAsList.contains(l.getB()) && resourceType.isAssignableFrom(l.getC().getClass())) //
                .forEach(r -> resources.add((R) r.getC()));

        return this;
    }

    public ChangesEventHandlerResourceStream<R> resourcesAdd(ChangesEventHandlerResourceStream<R> resourceStream) {
        resources.addAll(resourceStream.resources);
        return this;
    }

    public ChangesEventHandlerResourceStream<R> resourcesAdd(List<R> list) {
        resources.addAll(list);
        return this;
    }

    public ChangesEventHandlerResourceStream<R> resourcesAdd(Stream<R> stream) {
        stream.forEach(r -> resources.add(r));
        return this;
    }

    /**
     * Add all the next resources in the update list that are of the needed type.
     *
     * @param list
     *            the update resources list
     * @return this
     */
    @SuppressWarnings("unchecked")
    public ChangesEventHandlerResourceStream<R> resourcesAddNextOfType(List<UpdatedResource> list) {

        list.stream() //
                .filter(pAndN -> resourceType.isAssignableFrom(pAndN.getNext().getClass())) //
                .map(it -> (R) it.getNext()) //
                .forEach(r -> resources.add(r));

        return this;
    }

    /**
     * Add all the resources in the list that are of the needed type.
     *
     * @param list
     *            the resources list
     * @return this
     */
    @SuppressWarnings("unchecked")
    public ChangesEventHandlerResourceStream<R> resourcesAddOfType(List<IPResource> list) {

        list.stream() //
                .filter(r -> resourceType.isAssignableFrom(r.getClass())) //
                .map(r -> (R) r) //
                .forEach(r -> resources.add(r));

        return this;

    }

    /**
     * Sort and undup the resources. Ensure the resources are comparable before calling this method.
     *
     * @return this
     */
    public ChangesEventHandlerResourceStream<R> sortedAndDistinct() {
        resources = resources.stream().sorted().distinct().collect(Collectors.toCollection(() -> new ArrayList<>()));
        return this;

    }

    public <N extends IPResource> ChangesEventHandlerResourceStream<N> streamFromResourceAndLinkTypeAndToResourceClass(CommonServicesContext services, String linkType, Class<N> toResourceClass) {

        ChangesEventHandlerResourceStream<N> newStream = new ChangesEventHandlerResourceStream<>(toResourceClass);

        resources.forEach(resource -> {
            newStream.resourcesAdd(services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(resource, linkType, toResourceClass));
        });

        return newStream;
    }

    public <N extends IPResource> ChangesEventHandlerResourceStream<N> streamFromResourceAndLinkTypesAndToResourceClass(CommonServicesContext services, String[] linkTypes, Class<N> toResourceClass) {

        ChangesEventHandlerResourceStream<N> newStream = new ChangesEventHandlerResourceStream<>(toResourceClass);

        resources.forEach(resource -> {
            for (String linkType : linkTypes) {
                newStream.resourcesAdd(services.getResourceService().linkFindAllByFromResourceAndLinkTypeAndToResourceClass(resource, linkType, toResourceClass));
            }
        });

        return newStream;
    }

    public <N extends IPResource> ChangesEventHandlerResourceStream<N> streamFromResourceClassAndLinkType(CommonServicesContext services, Class<N> fromResourceClass, String linkType) {

        ChangesEventHandlerResourceStream<N> newStream = new ChangesEventHandlerResourceStream<>(fromResourceClass);

        resources.forEach(resource -> {
            newStream.resourcesAdd(services.getResourceService().linkFindAllByFromResourceClassAndLinkTypeAndToResource(fromResourceClass, linkType, resource));
        });

        return newStream;
    }

}
