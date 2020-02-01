/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.eventhandler;

import java.util.List;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;

public interface ChangesEventHandler {

    /**
     * Given the list of all the changes and the changes from the last step, provide a list of actions to execute.
     *
     * @param services
     *            the services
     * @param changesInTransactionContext
     *            the list of all the changes and the changes from the last step
     * @return the list of actions to execute
     */
    List<ActionHandler> computeActionsToExecute(CommonServicesContext services, ChangesInTransactionContext changesInTransactionContext);

}
