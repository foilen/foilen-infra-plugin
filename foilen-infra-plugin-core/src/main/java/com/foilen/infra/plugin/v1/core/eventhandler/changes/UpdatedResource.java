/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.eventhandler.changes;

import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tools.AbstractBasics;

public class UpdatedResource extends AbstractBasics {

    private IPResource previous;
    private IPResource next;

    public UpdatedResource() {
    }

    public UpdatedResource(IPResource previous, IPResource next) {
        this.previous = previous;
        this.next = next;
    }

    public IPResource getNext() {
        return next;
    }

    public IPResource getPrevious() {
        return previous;
    }

    public void setNext(IPResource next) {
        this.next = next;
    }

    public void setPrevious(IPResource previous) {
        this.previous = previous;
    }

}
