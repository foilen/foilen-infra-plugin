/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.eventhandler.changes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.eventhandler.ActionHandler;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.foilen.smalltools.tuple.Tuple3;

public class ChangesInTransactionContext extends AbstractBasics {

    private String txId;

    private AuditUserType userType = AuditUserType.SYSTEM;
    private String userName;

    private boolean explicitChange;

    private Map<String, String> vars = new HashMap<>();

    // All changes in this transaction
    private List<IPResource> allAddedResources = new ArrayList<>();
    private List<UpdatedResource> allUpdatedResources = new ArrayList<>();
    private List<IPResource> allDeletedResources = new ArrayList<>();
    private List<IPResource> allRefreshedResources = new ArrayList<>();
    private List<Tuple3<IPResource, String, IPResource>> allAddedLinks = new ArrayList<>();
    private List<Tuple3<IPResource, String, IPResource>> allDeletedLinks = new ArrayList<>();
    private Map<IPResource, Set<String>> allAddedTags = new HashMap<>();
    private Map<IPResource, Set<String>> allDeletedTags = new HashMap<>();

    // Changes in the last step
    private List<IPResource> lastAddedResources = new ArrayList<>();
    private List<UpdatedResource> lastUpdatedResources = new ArrayList<>();
    private List<IPResource> lastDeletedResources = new ArrayList<>();
    private List<IPResource> lastRefreshedResources = new ArrayList<>();
    private List<Tuple3<IPResource, String, IPResource>> lastAddedLinks = new ArrayList<>();
    private List<Tuple3<IPResource, String, IPResource>> lastDeletedLinks = new ArrayList<>();
    private Map<IPResource, Set<String>> lastAddedTags = new HashMap<>();
    private Map<IPResource, Set<String>> lastDeletedTags = new HashMap<>();

    // Reporting
    private Map<String, AtomicInteger> updateCountByResourceId = new HashMap<>();
    private Map<String, AtomicLong> executionTimeInMsByActionHandler = new HashMap<>();

    public void addAddedLink(Tuple3<IPResource, String, IPResource> link) {
        allAddedLinks.add(link);
        lastAddedLinks.add(link);
    }

    public void addAddedResource(IPResource resource) {
        allAddedResources.add(resource);
        lastAddedResources.add(resource);
    }

    public void addAddedTag(IPResource resource, String tag) {
        CollectionsTools.getOrCreateEmptyHashSet(allAddedTags, resource, String.class).add(tag);
        CollectionsTools.getOrCreateEmptyHashSet(lastAddedTags, resource, String.class).add(tag);
    }

    public void addDeletedLink(Tuple3<IPResource, String, IPResource> link) {
        allDeletedLinks.add(link);
        lastDeletedLinks.add(link);
    }

    public void addDeletedLinks(List<Tuple3<IPResource, String, IPResource>> links) {
        allDeletedLinks.addAll(links);
        lastDeletedLinks.addAll(links);
    }

    public void addDeletedResource(IPResource resource) {
        allDeletedResources.add(resource);
        lastDeletedResources.add(resource);
    }

    public void addDeletedTag(IPResource resource, String tag) {
        CollectionsTools.getOrCreateEmptyHashSet(allDeletedTags, resource, String.class).add(tag);
        CollectionsTools.getOrCreateEmptyHashSet(lastDeletedTags, resource, String.class).add(tag);
    }

    public void addDeletedTags(IPResource resource, Set<String> tags) {
        CollectionsTools.getOrCreateEmptyHashSet(allDeletedTags, resource, String.class).addAll(tags);
        CollectionsTools.getOrCreateEmptyHashSet(lastDeletedTags, resource, String.class).addAll(tags);
    }

    public void addExecutionTime(ActionHandler actionHandler, long time) {
        CollectionsTools.getOrCreateEmpty(executionTimeInMsByActionHandler, actionHandler.getClass().getSimpleName(), AtomicLong.class).addAndGet(time);
    }

    public void addRefreshedResource(IPResource resource) {
        allRefreshedResources.add(resource);
        lastRefreshedResources.add(resource);
    }

    public void addUpdatedResource(IPResource previousResource, IPResource updatedResource) {
        updateOrAddUpdatedResource(allUpdatedResources, previousResource, updatedResource);
        updateOrAddUpdatedResource(lastUpdatedResources, previousResource, updatedResource);
    }

    public void clearLast() {
        lastAddedResources.clear();
        lastUpdatedResources.clear();
        lastDeletedResources.clear();
        lastRefreshedResources.clear();
        lastAddedLinks.clear();
        lastDeletedLinks.clear();
        lastAddedTags.clear();
        lastDeletedTags.clear();
    }

    public List<String> generateTop10UpdateCountReport() {
        List<String> report = updateCountByResourceId.entrySet().stream() //
                .map(entry -> new Tuple2<>(entry.getKey(), entry.getValue().get())) //
                .sorted((a, b) -> a.getB().compareTo(b.getB()) * -1) //
                .limit(10) //
                .map(it -> it.getA() + " : " + it.getB()) //
                .collect(Collectors.toCollection(() -> new ArrayList<>()));
        if (updateCountByResourceId.size() > 10) {
            report.add("...");
        }
        return report;
    }

    public List<String> generateTop10UpdateEventHandlerExecutionTimeReport() {
        List<String> report = executionTimeInMsByActionHandler.entrySet().stream() //
                .map(entry -> new Tuple2<>(entry.getKey(), entry.getValue().get())) //
                .sorted((a, b) -> a.getB().compareTo(b.getB()) * -1) //
                .limit(10) //
                .map(it -> it.getA() + " : " + it.getB() + " ms") //
                .collect(Collectors.toCollection(() -> new ArrayList<>()));
        if (updateCountByResourceId.size() > 10) {
            report.add("...");
        }
        return report;
    }

    public List<Tuple3<IPResource, String, IPResource>> getAllAddedLinks() {
        return allAddedLinks;
    }

    public List<IPResource> getAllAddedResources() {
        return allAddedResources;
    }

    public Map<IPResource, Set<String>> getAllAddedTags() {
        return allAddedTags;
    }

    public List<Tuple3<IPResource, String, IPResource>> getAllDeletedLinks() {
        return allDeletedLinks;
    }

    public List<IPResource> getAllDeletedResources() {
        return allDeletedResources;
    }

    public Map<IPResource, Set<String>> getAllDeletedTags() {
        return allDeletedTags;
    }

    public List<IPResource> getAllRefreshedResources() {
        return allRefreshedResources;
    }

    public List<UpdatedResource> getAllUpdatedResources() {
        return allUpdatedResources;
    }

    public Map<String, AtomicLong> getExecutionTimeInMsByActionHandler() {
        return executionTimeInMsByActionHandler;
    }

    public List<Tuple3<IPResource, String, IPResource>> getLastAddedLinks() {
        return lastAddedLinks;
    }

    public List<IPResource> getLastAddedResources() {
        return lastAddedResources;
    }

    public Map<IPResource, Set<String>> getLastAddedTags() {
        return lastAddedTags;
    }

    public List<Tuple3<IPResource, String, IPResource>> getLastDeletedLinks() {
        return lastDeletedLinks;
    }

    public List<IPResource> getLastDeletedResources() {
        return lastDeletedResources;
    }

    public Map<IPResource, Set<String>> getLastDeletedTags() {
        return lastDeletedTags;
    }

    public List<IPResource> getLastRefreshedResources() {
        return lastRefreshedResources;
    }

    public List<UpdatedResource> getLastUpdatedResources() {
        return lastUpdatedResources;
    }

    public String getTxId() {
        return txId;
    }

    public Map<String, AtomicInteger> getUpdateCountByResourceId() {
        return updateCountByResourceId;
    }

    public String getUserName() {
        return userName;
    }

    public AuditUserType getUserType() {
        return userType;
    }

    public Map<String, String> getVars() {
        return vars;
    }

    public boolean hasChangesInLastRun() {
        return !lastAddedResources.isEmpty() || //
                !lastUpdatedResources.isEmpty() || //
                !lastDeletedResources.isEmpty() || //
                !lastRefreshedResources.isEmpty() || //
                !lastAddedLinks.isEmpty() || //
                !lastDeletedLinks.isEmpty() || //
                !lastAddedTags.isEmpty() || //
                !lastDeletedTags.isEmpty();
    }

    public boolean isExplicitChange() {
        return explicitChange;
    }

    public void setExecutionTimeInMsByActionHandler(Map<String, AtomicLong> executionTimeInMsByActionHandler) {
        this.executionTimeInMsByActionHandler = executionTimeInMsByActionHandler;
    }

    public void setExplicitChange(boolean explicitChange) {
        this.explicitChange = explicitChange;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public void setUpdateCountByResourceId(Map<String, AtomicInteger> updateCountByResourceId) {
        this.updateCountByResourceId = updateCountByResourceId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserType(AuditUserType userType) {
        this.userType = userType;
    }

    private void updateOrAddUpdatedResource(List<UpdatedResource> list, IPResource previousResource, IPResource updatedResource) {
        // Get the entry if already present
        Optional<UpdatedResource> existing = list.stream().filter(it -> StringTools.safeComparisonNullFirst(it.getPrevious().getInternalId(), previousResource.getInternalId()) == 0).findAny();
        if (existing.isPresent()) {
            // Update
            existing.get().setNext(updatedResource);
        } else {
            // Create
            list.add(new UpdatedResource(previousResource, updatedResource));
        }

    }

}
