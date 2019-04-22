/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.eventhandler.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.foilen.infra.plugin.v1.core.eventhandler.changes.UpdatedResource;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tuple.Tuple3;

public class ChangesEventHandlerUtils {

    public static <E extends IPResource> List<E> getFromAndToResources(List<Tuple3<IPResource, String, IPResource>> links, Class<E> resourceType) {

        return getFromAndToResourcesStream(links, resourceType) //
                .collect(Collectors.toList());

    }

    @SuppressWarnings("unchecked")
    public static <E extends IPResource> Stream<E> getFromAndToResourcesStream(List<Tuple3<IPResource, String, IPResource>> links, Class<E> resourceType) {

        return links.stream() //
                .filter(l -> resourceType.isAssignableFrom(l.getA().getClass()) || resourceType.isAssignableFrom(l.getC().getClass())) //
                .flatMap(l -> {
                    List<E> elements = new ArrayList<>();
                    if (resourceType.isAssignableFrom(l.getA().getClass())) {
                        elements.add((E) l.getA());
                    }
                    if (resourceType.isAssignableFrom(l.getC().getClass())) {
                        elements.add((E) l.getC());
                    }
                    return elements.stream();
                }); //

    }

    public static <F extends IPResource, T extends IPResource> List<F> getFromResources(List<Tuple3<IPResource, String, IPResource>> links, Class<F> fromResourceType, String linkType,
            Class<T> toResourceType) {

        return getFromResourcesStream(links, fromResourceType, linkType, toResourceType) //
                .collect(Collectors.toList());

    }

    @SuppressWarnings("unchecked")
    public static <F extends IPResource, T extends IPResource> Stream<F> getFromResourcesStream(List<Tuple3<IPResource, String, IPResource>> links, Class<F> fromResourceType, String linkType,
            Class<T> toResourceType) {

        return links.stream() //
                .filter(l -> StringTools.safeEquals(linkType, l.getB()) && //
                        fromResourceType.isAssignableFrom(l.getA().getClass()) && //
                        toResourceType.isAssignableFrom(l.getC().getClass())) //
                .map(l -> (F) l.getA()); //

    }

    public static Stream<IPResource> getFromResourcesStream(List<Tuple3<IPResource, String, IPResource>> links, String linkType) {
        return links.stream() //
                .filter(l -> StringTools.safeEquals(linkType, l.getB())) //
                .map(l -> l.getA());
    }

    @SuppressWarnings("unchecked")
    public static <T extends IPResource> List<T> getNextResourcesOfType(List<UpdatedResource> list, Class<T> type) {
        return getNextResourcesOfTypeStream(list, type) //
                .map(pAndN -> (T) pAndN.getNext()) //
                .collect(Collectors.toList());
    }

    public static <T extends IPResource> Stream<UpdatedResource> getNextResourcesOfTypeStream(List<UpdatedResource> list, Class<T> type) {
        return list.stream() //
                .filter(pAndN -> type.isAssignableFrom(pAndN.getNext().getClass()));
    }

    public static <T extends IPResource> List<T> getResourcesOfType(List<IPResource> list, Class<T> type) {
        return getResourcesOfTypeStream(list, type) //
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <T extends IPResource> Stream<T> getResourcesOfTypeStream(List<IPResource> list, Class<T> type) {
        return list.stream() //
                .filter(r -> type.isAssignableFrom(r.getClass())) //
                .map(r -> (T) r);
    }

    public static <F extends IPResource, T extends IPResource> List<T> getToResources(List<Tuple3<IPResource, String, IPResource>> links, Class<F> fromResourceType, String linkType,
            Class<T> toResourceType) {

        return getToResourcesStream(links, fromResourceType, linkType, toResourceType) //
                .collect(Collectors.toList());

    }

    @SuppressWarnings("unchecked")
    public static <F extends IPResource, T extends IPResource> Stream<T> getToResourcesStream(List<Tuple3<IPResource, String, IPResource>> links, Class<F> fromResourceType, String linkType,
            Class<T> toResourceType) {

        return links.stream() //
                .filter(l -> StringTools.safeEquals(linkType, l.getB()) && //
                        fromResourceType.isAssignableFrom(l.getA().getClass()) && //
                        toResourceType.isAssignableFrom(l.getC().getClass())) //
                .map(l -> (T) l.getC());

    }

    public static Stream<IPResource> getToResourcesStream(List<Tuple3<IPResource, String, IPResource>> links, String linkType) {
        return links.stream() //
                .filter(l -> StringTools.safeEquals(linkType, l.getB())) //
                .map(l -> l.getC());
    }

}
