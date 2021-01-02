/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.outputter.haproxy;

import java.util.Map;
import java.util.Map.Entry;

import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfig;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigEndpoint;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigPort;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigPortHttp;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigPortHttpService;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigPortHttps;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigPortHttpsService;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfigPortTcp;
import com.google.common.base.Strings;

// https://cbonte.github.io/haproxy-dconv/1.7/configuration.html
public class HaProxyConfigOutput {

    private static void appendBackend(StringBuilder backends, int port, String hostname, HaProxyConfigPortHttpService service, boolean isHttps) {
        if (service == null) {
            return;
        }
        backends.append("backend http");
        if (isHttps) {
            backends.append("s");
        }
        backends.append("_").append(port).append("_").append(hostname).append("\n");
        backends.append("  option httpclose\n");
        backends.append("  option forwardfor\n");
        backends.append("  mode http\n");
        int count = 1;
        for (HaProxyConfigEndpoint endpoint : service.getEndpoints()) {
            backends.append("  server http");
            if (isHttps) {
                backends.append("s");
            }
            backends.append("_").append(port).append("_").append(hostname).append("_").append(count++).append(" ").append(endpoint.getHostPort()).append(" check");
            if (endpoint.isSsl()) {
                backends.append(" ssl verify none");
            }
            backends.append("\n");
        }
        backends.append("\n");
    }

    private static void appendBackendDefault(StringBuilder backends, int port, HaProxyConfigPortHttpService defaultService, boolean isHttps) {
        appendBackend(backends, port, "default", defaultService, isHttps);
    }

    private static void appendBackends(StringBuilder backends, int port, Map<String, ? extends HaProxyConfigPortHttpService> serviceByHostname, boolean isHttps) {
        for (Entry<String, ? extends HaProxyConfigPortHttpService> entry : serviceByHostname.entrySet()) {
            if (entry.getValue().getEndpoints().isEmpty()) {
                continue;
            }
            appendBackend(backends, port, entry.getKey(), entry.getValue(), isHttps);
        }
    }

    public static String toConfigFile(HaProxyConfig haProxyConfig) {
        StringBuilder content = new StringBuilder();
        StringBuilder backends = new StringBuilder();
        content.append("global").append("\n");
        String chroot = haProxyConfig.getChroot();
        if (!Strings.isNullOrEmpty(chroot)) {
            content.append("  chroot ").append(chroot).append("\n");
        }
        String user = haProxyConfig.getUser();
        if (!Strings.isNullOrEmpty(user)) {
            content.append("  user ").append(user).append("\n");
        }
        String group = haProxyConfig.getGroup();
        if (!Strings.isNullOrEmpty(group)) {
            content.append("  group ").append(group).append("\n");
        }
        if (haProxyConfig.isDaemon()) {
            content.append("  daemon").append("\n");
        }
        content.append("  tune.ssl.default-dh-param 1024").append("\n");
        String pidfile = haProxyConfig.getPidfile();
        if (!Strings.isNullOrEmpty(pidfile)) {
            content.append("  pidfile ").append(pidfile).append("\n");
        }
        content.append("\n");

        content.append("defaults").append("\n");
        content.append("  timeout connect ").append(haProxyConfig.getTimeoutConnectionMs()).append("\n");
        content.append("  timeout client ").append(haProxyConfig.getTimeoutClientMs()).append("\n");
        content.append("  timeout server ").append(haProxyConfig.getTimeoutServerMs()).append("\n");
        content.append("  timeout tunnel ").append(haProxyConfig.getTimeoutTunnelMs()).append("\n");
        content.append("\n");

        for (Entry<Integer, HaProxyConfigPort> configPerPort : haProxyConfig.getPorts().entrySet()) {
            int port = configPerPort.getKey();
            HaProxyConfigPort value = configPerPort.getValue();
            if (value instanceof HaProxyConfigPortHttps) {
                HaProxyConfigPortHttps portConfig = (HaProxyConfigPortHttps) value;

                // Skip if no services
                if (portConfig.getDefaultService() == null && portConfig.getServiceByHostname().isEmpty()) {
                    continue;
                }

                content.append("frontend port_").append(port).append("\n");
                content.append("  mode http").append("\n");
                content.append("  option forwardfor").append("\n");
                content.append("  option http-server-close").append("\n");
                content.append("  bind ").append(portConfig.getBindHost()).append(":").append(port).append(" ssl crt ").append(portConfig.getCertificatesDirectory()).append("\n");
                content.append("  reqadd X-Forwarded-Proto:\\ https").append("\n");
                content.append("\n");
                for (Entry<String, HaProxyConfigPortHttpsService> entry : portConfig.getServiceByHostname().entrySet()) {
                    if (entry.getValue().getEndpoints().isEmpty()) {
                        continue;
                    }
                    String hostName = entry.getKey();
                    content.append("  acl https_").append(port).append("_").append(hostName).append(" hdr(host) -i ").append(hostName).append("\n");
                }
                content.append("\n");

                boolean gotOne = false;
                for (Entry<String, HaProxyConfigPortHttpsService> entry : portConfig.getServiceByHostname().entrySet()) {
                    if (entry.getValue().getEndpoints().isEmpty()) {
                        continue;
                    }
                    String hostName = entry.getKey();
                    if (entry.getValue().isOriginToHttp()) {
                        content.append("  http-request replace-header Origin https://").append(hostName).append("(.*) http://").append(hostName).append("\\1 if https_").append(port).append("_")
                                .append(hostName).append("\n");
                        gotOne = true;
                    }
                }
                if (gotOne) {
                    content.append("\n");
                }

                if (portConfig.getDefaultService() != null && !portConfig.getDefaultService().getEndpoints().isEmpty()) {
                    content.append("  default_backend https_").append(port).append("_default\n");
                }
                for (Entry<String, HaProxyConfigPortHttpsService> entry : portConfig.getServiceByHostname().entrySet()) {
                    if (entry.getValue().getEndpoints().isEmpty()) {
                        continue;
                    }
                    String hostName = entry.getKey();
                    content.append("  use_backend https_").append(port).append("_").append(hostName).append(" if https_").append(port).append("_").append(hostName).append("\n");
                }
                appendBackendDefault(backends, port, portConfig.getDefaultService(), true);
                appendBackends(backends, port, portConfig.getServiceByHostname(), true);
            } else if (value instanceof HaProxyConfigPortHttp) {
                HaProxyConfigPortHttp portConfig = (HaProxyConfigPortHttp) value;

                // Skip if no services
                if (portConfig.getDefaultService() == null && portConfig.getServiceByHostname().isEmpty()) {
                    continue;
                }

                content.append("frontend port_").append(port).append("\n");
                content.append("  mode http").append("\n");
                content.append("  option forwardfor").append("\n");
                content.append("  option http-server-close").append("\n");
                content.append("  bind ").append(portConfig.getBindHost()).append(":").append(port).append("\n");
                content.append("  reqadd X-Forwarded-Proto:\\ http").append("\n");
                content.append("\n");
                for (Entry<String, HaProxyConfigPortHttpService> entry : portConfig.getServiceByHostname().entrySet()) {
                    if (entry.getValue().getEndpoints().isEmpty()) {
                        continue;
                    }
                    String hostName = entry.getKey();
                    content.append("  acl http_").append(port).append("_").append(hostName).append(" hdr(host) -i ").append(hostName).append("\n");
                }
                content.append("\n");
                if (portConfig.getDefaultService() != null && !portConfig.getDefaultService().getEndpoints().isEmpty()) {
                    content.append("  default_backend http_").append(port).append("_default\n");
                }
                for (Entry<String, HaProxyConfigPortHttpService> entry : portConfig.getServiceByHostname().entrySet()) {
                    if (entry.getValue().getEndpoints().isEmpty()) {
                        continue;
                    }
                    String hostName = entry.getKey();
                    content.append("  use_backend http_").append(port).append("_").append(hostName).append(" if http_").append(port).append("_").append(hostName).append("\n");
                }
                appendBackendDefault(backends, port, portConfig.getDefaultService(), false);
                appendBackends(backends, port, portConfig.getServiceByHostname(), false);
            } else {
                HaProxyConfigPortTcp portConfig = (HaProxyConfigPortTcp) value;
                if (portConfig.getEndpointHostPorts().isEmpty()) {
                    continue;
                }
                content.append("listen port_").append(port).append("\n");
                content.append("  bind ").append(portConfig.getBindHost()).append(":").append(port).append("\n");
                content.append("  mode tcp").append("\n");
                int count = 1;
                for (String endpoint : portConfig.getEndpointHostPorts()) {
                    content.append("  server port_").append(port).append("_").append(count++).append(" ").append(endpoint).append(" check\n");
                }
            }

            content.append("\n");
        }

        content.append(backends);

        return content.toString();
    }

    public static String toRun(HaProxyConfig haProxyConfig, String configFilePath) {
        StringBuilder command = new StringBuilder();
        command.append(haProxyConfig.getCommand() + " -f ");
        command.append(configFilePath);

        String pidfile = haProxyConfig.getPidfile();
        if (!Strings.isNullOrEmpty(pidfile)) {
            command.append(" -p $(<").append(haProxyConfig.getPidfile()).append(") -st $(<").append(haProxyConfig.getPidfile()).append(")");
        }

        return command.toString();
    }

    private HaProxyConfigOutput() {
    }

}
