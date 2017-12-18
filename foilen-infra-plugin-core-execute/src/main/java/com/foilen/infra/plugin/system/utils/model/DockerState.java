/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.system.utils.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerState {

    // Running state
    private Map<String, DockerStateIds> runningContainersByName = new HashMap<>();
    private Map<String, String> ipByName = new HashMap<>();
    private List<String> failedContainerNames = new ArrayList<>();

    // Redirection details
    private int redirectorBridgePort = 11000;
    private List<String> redirectorCaCerts = new ArrayList<>();
    private String redirectorNodeCert;
    private String redirectorNodeKey;
    private Map<String, Integer> redirectPortByMachineContainerEndpoint = new HashMap<>();
    private Map<String, String> redirectIpByMachineContainerEndpoint = new HashMap<>();

    // Executions
    private Map<String, Future<Boolean>> executionsFutures = new HashMap<>();

    public Map<String, Future<Boolean>> getExecutionsFutures() {
        return executionsFutures;
    }

    public List<String> getFailedContainerNames() {
        return failedContainerNames;
    }

    public Map<String, String> getIpByName() {
        return ipByName;
    }

    public Map<String, String> getRedirectIpByMachineContainerEndpoint() {
        return redirectIpByMachineContainerEndpoint;
    }

    public int getRedirectorBridgePort() {
        return redirectorBridgePort;
    }

    public List<String> getRedirectorCaCerts() {
        return redirectorCaCerts;
    }

    public String getRedirectorNodeCert() {
        return redirectorNodeCert;
    }

    public String getRedirectorNodeKey() {
        return redirectorNodeKey;
    }

    public Map<String, Integer> getRedirectPortByMachineContainerEndpoint() {
        return redirectPortByMachineContainerEndpoint;
    }

    public Map<String, DockerStateIds> getRunningContainersByName() {
        return runningContainersByName;
    }

    public void setExecutionsFutures(Map<String, Future<Boolean>> executionsFutures) {
        this.executionsFutures = executionsFutures;
    }

    public void setFailedContainerNames(List<String> failedContainerNames) {
        this.failedContainerNames = failedContainerNames;
    }

    public void setIpByName(Map<String, String> ipByName) {
        this.ipByName = ipByName;
    }

    public void setRedirectIpByMachineContainerEndpoint(Map<String, String> redirectIpByMachineContainerEndpoint) {
        this.redirectIpByMachineContainerEndpoint = redirectIpByMachineContainerEndpoint;
    }

    public void setRedirectorBridgePort(int redirectorBridgePort) {
        this.redirectorBridgePort = redirectorBridgePort;
    }

    public void setRedirectorCaCerts(List<String> redirectorCaCerts) {
        this.redirectorCaCerts = redirectorCaCerts;
    }

    public void setRedirectorNodeCert(String redirectorNodeCert) {
        this.redirectorNodeCert = redirectorNodeCert;
    }

    public void setRedirectorNodeKey(String redirectorNodeKey) {
        this.redirectorNodeKey = redirectorNodeKey;
    }

    public void setRedirectPortByMachineContainerEndpoint(Map<String, Integer> redirectPortByMachineContainerEndpoint) {
        this.redirectPortByMachineContainerEndpoint = redirectPortByMachineContainerEndpoint;
    }

    public void setRunningContainersByName(Map<String, DockerStateIds> runningContainersByName) {
        this.runningContainersByName = runningContainersByName;
    }

}