/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.haproxy;

public class HaProxyConfigPortHttps extends AbstractHaProxyConfigPortHttp<HaProxyConfigPortHttpsService> {

    private String certificatesDirectory;

    public HaProxyConfigPortHttps() {
    }

    public HaProxyConfigPortHttps(String certificatesDirectory) {
        this.certificatesDirectory = certificatesDirectory;
    }

    @Override
    public HaProxyConfigPortHttpsService createConfig() {
        return new HaProxyConfigPortHttpsService();
    }

    public String getCertificatesDirectory() {
        return certificatesDirectory;
    }

    @Override
    public Class<HaProxyConfigPortHttpsService> getConfigType() {
        return HaProxyConfigPortHttpsService.class;
    }

    public void setCertificatesDirectory(String certificatesDirectory) {
        this.certificatesDirectory = certificatesDirectory;
    }

}
