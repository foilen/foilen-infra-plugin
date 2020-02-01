/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.outputter.docker;

import java.util.HashMap;
import java.util.Map;

import com.foilen.smalltools.hash.HashSha256;

public class DockerContainerOutputContext {

    private String imageName;
    private String containerName;
    private String hostName;

    private String outputDirectory;

    // Networking
    private String networkName;
    private String networkIp;

    // Log details
    private Integer dockerLogsMaxSizeMB;

    // Redirection details
    private Map<String, Integer> redirectPortByMachineContainerEndpoint = new HashMap<>();
    private Map<String, String> redirectIpByMachineContainerEndpoint = new HashMap<>();

    public DockerContainerOutputContext(String imageName, String containerName) {
        this.imageName = imageName;
        this.containerName = containerName;
    }

    public DockerContainerOutputContext(String imageName, String containerName, String hostName) {
        this.imageName = imageName;
        this.containerName = containerName;
        this.hostName = hostName;
    }

    public DockerContainerOutputContext(String imageName, String containerName, String hostName, String outputDirectory) {
        this.imageName = imageName;
        this.containerName = containerName;
        this.hostName = hostName;
        this.outputDirectory = outputDirectory;
    }

    public String getContainerName() {
        return containerName;
    }

    public Integer getDockerLogsMaxSizeMB() {
        return dockerLogsMaxSizeMB;
    }

    public String getHostName() {
        return hostName;
    }

    public String getImageName() {
        return imageName;
    }

    public String getNetworkIp() {
        return networkIp;
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public Map<String, String> getRedirectIpByMachineContainerEndpoint() {
        return redirectIpByMachineContainerEndpoint;
    }

    public Map<String, Integer> getRedirectPortByMachineContainerEndpoint() {
        return redirectPortByMachineContainerEndpoint;
    }

    public DockerContainerOutputContext setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    public DockerContainerOutputContext setDockerLogsMaxSizeMB(Integer dockerLogsMaxSizeMB) {
        this.dockerLogsMaxSizeMB = dockerLogsMaxSizeMB;
        return this;
    }

    public DockerContainerOutputContext setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public DockerContainerOutputContext setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public DockerContainerOutputContext setNetworkIp(String networkIp) {
        this.networkIp = networkIp;
        return this;
    }

    public DockerContainerOutputContext setNetworkName(String networkName) {
        this.networkName = networkName;
        return this;
    }

    public DockerContainerOutputContext setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public DockerContainerOutputContext setRedirectIpByMachineContainerEndpoint(Map<String, String> redirectIpByMachineContainerEndpoint) {
        this.redirectIpByMachineContainerEndpoint = redirectIpByMachineContainerEndpoint;
        return this;
    }

    public DockerContainerOutputContext setRedirectPortByMachineContainerEndpoint(Map<String, Integer> redirectPortByMachineContainerEndpoint) {
        this.redirectPortByMachineContainerEndpoint = redirectPortByMachineContainerEndpoint;
        return this;
    }

    /**
     * Gives a unique ID depending on the run command fields.
     *
     * @return the unique id
     */
    public String toContainerRunUniqueId() {
        StringBuilder concat = new StringBuilder();
        concat.append(imageName);
        concat.append(containerName);
        concat.append(hostName);
        concat.append(networkName);
        concat.append(networkIp);
        concat.append(dockerLogsMaxSizeMB);
        return HashSha256.hashString(concat.toString());
    }

}
