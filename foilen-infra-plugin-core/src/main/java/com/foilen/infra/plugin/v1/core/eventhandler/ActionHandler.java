/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.eventhandler;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;

/**
 * The action to execute after some specific changes happened.
 */
public interface ActionHandler {

    /**
     * Execute the action after some changes were applied.
     *
     * @param services
     *            the services
     * @param changes
     *            the changes you want to apply as a next step
     */
    void executeAction(CommonServicesContext services, ChangesContext changes);

}
