/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.eventhandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.exception.IllegalUpdateException;
import com.foilen.infra.plugin.v1.core.exception.NoChangeNeededException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.smalltools.tuple.Tuple3;

/**
 * A simple way to handle events by specifying the desired final state of some managed resources.
 */
public abstract class AbstractFinalStateManagedResourcesEventHandler<R extends IPResource> extends AbstractUpdateEventHandler<R> {

    @Override
    public void addHandler(CommonServicesContext services, ChangesContext changes, R resource) {
        FinalStateManagedResourcesUpdateEventHandlerContext<R> context = new FinalStateManagedResourcesUpdateEventHandlerContext<>();
        context.setResource(resource);
        commonHandler(services, changes, context);
    }

    @Override
    public void checkAndFix(CommonServicesContext services, ChangesContext changes, R resource) {
        FinalStateManagedResourcesUpdateEventHandlerContext<R> context = new FinalStateManagedResourcesUpdateEventHandlerContext<>();
        context.setResource(resource);
        commonHandler(services, changes, context);
    }

    private void commonHandler(CommonServicesContext services, ChangesContext changes, FinalStateManagedResourcesUpdateEventHandlerContext<R> context) {

        try {
            // Get information
            commonHandlerExecute(services, context);
        } catch (NoChangeNeededException e) {
            logger.info("No Change Needed. Skip");
            return;
        }

        // Update the updated resource if needed
        if (context.isRequestUpdateResource()) {
            logger.debug("Updating the resource was requested");
            changes.resourceUpdate(context.getResource());
        }

        // Get the previously managed resources
        IPResourceService resourceService = services.getResourceService();
        IPResource resource = context.getResource();
        List<IPResource> previousManagedResources = new ArrayList<>();
        logger.debug("Getting the previously managed resources");
        for (Class<? extends IPResource> managedResourceType : context.getManagedResourceTypes()) {
            previousManagedResources.addAll(resourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(resource, LinkTypeConstants.MANAGES, managedResourceType));
        }
        logger.debug("Got {} previously managed resources", previousManagedResources.size());

        // Find or create the desired resources
        Map<IPResource, FinalStateManagedResource> finalStateByResource = new HashMap<>();
        List<IPResource> desiredManagedResources = new ArrayList<>();
        logger.debug("Find or create the desired resources");
        context.getManagedResources().forEach(it -> {
            IPResource retrievedOrCreatedResource = retrieveAndUpdateOrCreateResource(resourceService, changes, it.getManagedResource());
            desiredManagedResources.add(retrievedOrCreatedResource);
            finalStateByResource.put(retrievedOrCreatedResource, it);
        });
        logger.debug("Got {} desired managed resources", desiredManagedResources.size());

        // Check only one resource is managing each
        for (IPResource desiredManagedResource : desiredManagedResources) {
            if (desiredManagedResource.getInternalId() == null) {
                continue;
            }

            List<? extends IPResource> managers = resourceService.linkFindAllByLinkTypeAndToResource(LinkTypeConstants.MANAGES, desiredManagedResource);
            if (managers.size() > 1) {
                throw new IllegalUpdateException(
                        "The resource " + desiredManagedResource.getResourceName() + " of type " + desiredManagedResource.getClass() + " is already managed by another resource");
            }
            if (managers.size() == 1) {
                if (resource.getInternalId() == null || managers.get(0).getInternalId() != resource.getInternalId()) {
                    throw new IllegalUpdateException(
                            "The resource " + desiredManagedResource.getResourceName() + " of type " + desiredManagedResource.getClass() + " is already managed by another resource");
                }
            }
        }

        // Update the links
        logger.debug("Updating the links");
        List<Long> desiredManagedResourceIds = new ArrayList<>();
        for (Entry<IPResource, FinalStateManagedResource> entry : finalStateByResource.entrySet()) {
            IPResource desiredManagedResource = entry.getKey();
            FinalStateManagedResource finalStateManagedResource = entry.getValue();

            // Manages
            changes.linkAdd(resource, LinkTypeConstants.MANAGES, desiredManagedResource);
            if (desiredManagedResource.getInternalId() != null) {
                desiredManagedResourceIds.add(desiredManagedResource.getInternalId());
            }

            // Links To
            for (String linkType : finalStateManagedResource.getManagedLinksToTypes()) {
                List<? extends IPResource> previousLinks;
                if (desiredManagedResource.getInternalId() == null) {
                    previousLinks = Collections.emptyList();
                } else {
                    previousLinks = resourceService.linkFindAllByFromResourceAndLinkType(desiredManagedResource, linkType);
                }
                List<? extends IPResource> desiredLinks = finalStateManagedResource.getLinksTo().stream() //
                        .filter(it -> linkType.equals(it.getA())) //
                        .map(it -> it.getB()) //
                        .collect(Collectors.toList());
                logger.debug("Previous links to of type {} : {}", linkType, previousLinks.size());
                logger.debug("Desired links to of type {} : {}", linkType, desiredLinks.size());

                // Remove those in previousLinks, but not in desiredLinks
                previousLinks.stream() //
                        .filter(previous -> !desiredLinks.stream().anyMatch(desired -> resourceService.resourceEqualsPk(previous, desired))) //
                        .forEach(previous -> {
                            logger.debug("Removing link {}/{}/{}", desiredManagedResource, linkType, previous);
                            changes.linkDelete(desiredManagedResource, linkType, previous);
                        });

                // Add those in desiredLinks, but not in previousLinks
                desiredLinks.stream() //
                        .filter(desired -> !previousLinks.stream().anyMatch(previous -> resourceService.resourceEqualsPk(previous, desired))) //
                        .forEach(desired -> {
                            logger.debug("Adding link {}/{}/{}", desiredManagedResource, linkType, desired);
                            changes.linkAdd(desiredManagedResource, linkType, desired);
                        });
            }

            // Links From
            for (String linkType : finalStateManagedResource.getManagedLinksFromTypes()) {
                List<? extends IPResource> previousLinks;
                if (desiredManagedResource.getInternalId() == null) {
                    previousLinks = Collections.emptyList();
                } else {
                    previousLinks = resourceService.linkFindAllByLinkTypeAndToResource(linkType, desiredManagedResource);
                }
                List<? extends IPResource> desiredLinks = finalStateManagedResource.getLinksFrom().stream() //
                        .filter(it -> linkType.equals(it.getB())) //
                        .map(it -> it.getA()) //
                        .collect(Collectors.toList());
                logger.debug("Previous links from of type {} : {}", linkType, previousLinks.size());
                logger.debug("Desired links from of type {} : {}", linkType, desiredLinks.size());

                // Remove those in previousLinks, but not in desiredLinks
                previousLinks.stream() //
                        .filter(previous -> !desiredLinks.stream().anyMatch(desired -> resourceService.resourceEqualsPk(previous, desired))) //
                        .forEach(previous -> {
                            logger.debug("Removing link {}/{}/{}", previous, linkType, desiredManagedResource);
                            changes.linkDelete(previous, linkType, desiredManagedResource);
                        });

                // Add those in desiredLinks, but not in previousLinks
                desiredLinks.stream() //
                        .filter(desired -> !previousLinks.stream().anyMatch(previous -> resourceService.resourceEqualsPk(previous, desired))) //
                        .forEach(desired -> {
                            logger.debug("Adding link {}/{}/{}", desired, linkType, desiredManagedResource);
                            changes.linkAdd(desired, linkType, desiredManagedResource);
                        });
            }

        }
        logger.debug("Completed to update the links");

        // Remove the managed resources that are no more needed
        logger.debug("Remove the managed resources that are no more needed");
        for (IPResource previousManagedResource : previousManagedResources) {
            if (!desiredManagedResourceIds.contains(previousManagedResource.getInternalId())) {
                removeManagedLinkAndDeleteIfNotManagedByAnyoneElse(resourceService, changes, previousManagedResource, resource);
            }
        }

        logger.debug("Process completed");

    }

    /**
     * Set the desired managed resources and their links.
     *
     * @param services
     *            the services you can use
     * @param context
     *            the context of the current update
     * @throws NoChangeNeededException
     *             if nothing needs to be done, throw that exception
     */
    protected abstract void commonHandlerExecute(CommonServicesContext services, FinalStateManagedResourcesUpdateEventHandlerContext<R> context) throws NoChangeNeededException;

    @Override
    public void deleteHandler(CommonServicesContext services, ChangesContext changes, R resource, List<Tuple3<IPResource, String, IPResource>> previousLinks) {
        detachManagedResources(services, changes, resource, previousLinks);
    }

    @Override
    public void updateHandler(CommonServicesContext services, ChangesContext changes, R previousResource, R newResource) {
        FinalStateManagedResourcesUpdateEventHandlerContext<R> context = new FinalStateManagedResourcesUpdateEventHandlerContext<>();
        context.setOldResource(previousResource);
        context.setResource(newResource);
        commonHandler(services, changes, context);
    }

}
