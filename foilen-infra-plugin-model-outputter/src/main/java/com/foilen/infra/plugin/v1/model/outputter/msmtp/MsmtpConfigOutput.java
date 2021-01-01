/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.outputter.msmtp;

import com.foilen.infra.plugin.v1.model.msmtp.MsmtpConfig;

public class MsmtpConfigOutput {

    static public String toConfig(MsmtpConfig msmtpConfig) {
        StringBuilder content = new StringBuilder();
        content.append("account default\n");
        content.append("host ").append(msmtpConfig.getHostname()).append("\n");
        content.append("port ").append(msmtpConfig.getPort()).append("\n");
        if (msmtpConfig.getUsername() != null) {
            content.append("auth on\n");
            content.append("user ").append(msmtpConfig.getUsername()).append("\n");
        }
        if (msmtpConfig.getPassword() != null) {
            content.append("password ").append(msmtpConfig.getPassword()).append("\n");
        }
        if (msmtpConfig.isTls()) {
            content.append("tls on\n");
            content.append("tls_certcheck off\n");
        }
        return content.toString();
    }

}
