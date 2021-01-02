/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.context;

import com.foilen.infra.plugin.v1.core.eventhandler.ChangesEventHandler;
import com.foilen.smalltools.tools.AbstractBasics;

public class ChangesEventContext extends AbstractBasics {

    private ChangesEventHandler changesEventHandler;
    private String changesHandlerName;

    public ChangesEventContext(ChangesEventHandler changesEventHandler, String changesHandlerName) {
        this.changesEventHandler = changesEventHandler;
        this.changesHandlerName = changesHandlerName;
    }

    public ChangesEventHandler getChangesEventHandler() {
        return changesEventHandler;
    }

    public String getChangesHandlerName() {
        return changesHandlerName;
    }

    public void setChangesEventHandler(ChangesEventHandler changesEventHandler) {
        this.changesEventHandler = changesEventHandler;
    }

    public void setChangesHandlerName(String changesHandlerName) {
        this.changesHandlerName = changesHandlerName;
    }

}
