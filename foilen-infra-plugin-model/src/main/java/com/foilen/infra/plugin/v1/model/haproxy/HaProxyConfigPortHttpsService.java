/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.haproxy;

public class HaProxyConfigPortHttpsService extends HaProxyConfigPortHttpService {

    private boolean originToHttp;

    public boolean isOriginToHttp() {
        return originToHttp;
    }

    public void setOriginToHttp(boolean originToHttp) {
        this.originToHttp = originToHttp;
    }

}
