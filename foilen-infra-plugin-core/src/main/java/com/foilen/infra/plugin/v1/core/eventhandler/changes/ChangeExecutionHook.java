/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.eventhandler.changes;

import com.foilen.infra.plugin.v1.model.resource.IPResource;

public interface ChangeExecutionHook {

    default void failureInfinite(ChangesInTransactionContext changesInTransactionContext) {
    }

    default void fillApplyChangesContext(ChangesInTransactionContext changesInTransactionContext) {
    }

    default void linkAdded(ChangesInTransactionContext changesInTransactionContext, IPResource fromResource, String linkType, IPResource toResource) {
    }

    default void linkDeleted(ChangesInTransactionContext changesInTransactionContext, IPResource fromResource, String linkType, IPResource toResource) {
    }

    default void resourceAdded(ChangesInTransactionContext changesInTransactionContext, IPResource resource) {
    }

    default void resourceDeleted(ChangesInTransactionContext changesInTransactionContext, IPResource resource) {
    }

    default void resourceUpdated(ChangesInTransactionContext changesInTransactionContext, IPResource previousResource, IPResource updatedResource) {
    }

    default void success(ChangesInTransactionContext changesInTransactionContext) {
    }

    default void tagAdded(ChangesInTransactionContext changesInTransactionContext, IPResource resource, String tagName) {
    }

    default void tagDeleted(ChangesInTransactionContext changesInTransactionContext, IPResource resource, String tagName) {
    }

}
