/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.haproxy;

public class HaProxyConfigPortHttp extends AbstractHaProxyConfigPortHttp<HaProxyConfigPortHttpService> {

    @Override
    public HaProxyConfigPortHttpService createConfig() {
        return new HaProxyConfigPortHttpService();
    }

    @Override
    public Class<HaProxyConfigPortHttpService> getConfigType() {
        return HaProxyConfigPortHttpService.class;
    }

}
