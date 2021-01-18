/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.outputter.docker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionBuildStep;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionPortRedirect;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionService;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.haproxy.HaProxyConfig;
import com.foilen.infra.plugin.v1.model.outputter.ModelException;
import com.foilen.infra.plugin.v1.model.outputter.haproxy.HaProxyConfigOutput;
import com.foilen.infra.plugin.v1.model.outputter.servicesExecution.ServicesExecutionConfig;
import com.foilen.infra.plugin.v1.model.outputter.servicesExecution.ServicesExecutionServiceConfig;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.FreemarkerTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Strings;

/**
 * To get different outputs from a {@link IPApplicationDefinition}.
 */
public class DockerContainerOutput {

    private static final Logger logger = LoggerFactory.getLogger(DockerContainerOutput.class);

    // TODO Make sure users cannot take these names
    public static final String REDIRECTOR_ENTRY_CONTAINER_NAME = "infra_redirector_entry";
    public static final String REDIRECTOR_EXIT_CONTAINER_NAME = "infra_redirector_exit";

    public static IPApplicationDefinition addInfrastructure(IPApplicationDefinition applicationDefinition, DockerContainerOutputContext ctx) {
        String imageName = ctx.getImageName();
        logger.info("[{}] Transform the application", imageName);

        // Clone
        IPApplicationDefinition transformedApplicationDefinition = JsonTools.clone(applicationDefinition);

        List<IPApplicationDefinitionService> services = transformedApplicationDefinition.getServices();
        List<Tuple2<IPApplicationDefinitionService, Boolean>> serviceAndIsInfras = services.stream().map(it -> {
            return new Tuple2<>(it, false);
        }).collect(Collectors.toList());

        // Use a single assetsBundle for (services execution if more than 1 service ; if infra)
        List<IPApplicationDefinitionPortRedirect> portsRedirect = transformedApplicationDefinition.getPortsRedirect();
        IPApplicationDefinitionAssetsBundle assetsBundle = transformedApplicationDefinition.addAssetsBundle();
        boolean infraChmodAdded = false;
        if (!portsRedirect.isEmpty() || services.size() > 1 || (!Strings.isNullOrEmpty(transformedApplicationDefinition.getCommand()) && !services.isEmpty())) {
            logger.info("[{}] Has multiple services to run or some port redirects", imageName);

            // Make sure there is at least one known service/command to run
            if (transformedApplicationDefinition.getCommand() == null && services.isEmpty()) {
                throw new ModelException("We need to move the current command in a service, but you are using the default image's command which we do not know. Please specify the command name");
            }

            // Add volume for infra-apps
            ctx.getInfraVolumes().forEach(volume -> {
                logger.info("[{}] Adding infra volume {}", imageName, volume);
                transformedApplicationDefinition.addVolume(volume);
            });

            // Move the current command to a service
            Long initialRunAs = transformedApplicationDefinition.getRunAs();
            if (transformedApplicationDefinition.getCommand() != null) {
                logger.info("[{}] There is one main command. Moving it to a service", imageName);
                IPApplicationDefinitionService commandToService = new IPApplicationDefinitionService("_main", transformedApplicationDefinition.getCommand(),
                        transformedApplicationDefinition.getRunAs());
                commandToService.setWorkingDirectory(transformedApplicationDefinition.getWorkingDirectory());
                transformedApplicationDefinition.getServices().add(commandToService);
                serviceAndIsInfras.add(new Tuple2<>(commandToService, false));
                transformedApplicationDefinition.setCommand(null);
                transformedApplicationDefinition.setWorkingDirectory(null);
                transformedApplicationDefinition.setRunAs(0L);
            }
            transformedApplicationDefinition.setEntrypoint(new ArrayList<>());

            // Add infra if needed
            if (!portsRedirect.isEmpty()) {

                HaProxyConfig haProxyConfig = new HaProxyConfig();
                if (ctx.getHaProxyCommand() != null) {
                    haProxyConfig.setCommand(ctx.getHaProxyCommand());
                }
                haProxyConfig.setUser(null);
                haProxyConfig.setGroup(null);
                haProxyConfig.setTimeoutTunnelMs(10L * 60L * 1000L); // 10 minutes
                for (IPApplicationDefinitionPortRedirect portRedirect : portsRedirect) {
                    String machineContainerEndpoint = portRedirect.getMachineContainerEndpoint();

                    logger.info("[{}] Adding infra {} ; Key {}", imageName, portRedirect, machineContainerEndpoint);

                    String host = ctx.getRedirectIpByMachineContainerEndpoint().get(machineContainerEndpoint);
                    Integer port = ctx.getRedirectPortByMachineContainerEndpoint().get(machineContainerEndpoint);

                    if (host == null || port == null) {
                        logger.error("[{}] Infra {} -> Missing dependency to {}", imageName, portRedirect.getLocalPort(), machineContainerEndpoint);
                        logger.debug("Known dependencies: {}", getKnownDependencies(ctx));
                        haProxyConfig.addPortTcp(portRedirect.getLocalPort(), new Tuple2<>("127.0.0.1", 1));
                    } else {
                        logger.info("[{}] Infra {} -> {}:{}", imageName, portRedirect.getLocalPort(), host, port);
                        haProxyConfig.addPortTcp(portRedirect.getLocalPort(), new Tuple2<>(host, port));
                    }

                }
                assetsBundle.addAssetContent("_infra/_infra_ha_proxy.cfg", HaProxyConfigOutput.toConfigFile(haProxyConfig));

                IPApplicationDefinitionService service = new IPApplicationDefinitionService("_infra_ha_proxy", HaProxyConfigOutput.toRun(haProxyConfig, "/_infra/_infra_ha_proxy.cfg"));
                Tuple2<IPApplicationDefinitionService, Boolean> serviceAndIsInfra = new Tuple2<>(service, true);
                serviceAndIsInfras.add(serviceAndIsInfra);
            }

            // Change the container to run as root, the haproxy as root and all the other services as the container user
            if (initialRunAs != null && initialRunAs != 0) {
                logger.info("[{}] Changing the instance user to root and all the services to {}", imageName, initialRunAs);
                transformedApplicationDefinition.setRunAs(0L);
                for (IPApplicationDefinitionService service : transformedApplicationDefinition.getServices()) {
                    if (service.getRunAs() == null) {
                        service.setRunAs(initialRunAs);
                    }
                }
            }

            // Services command
            ServicesExecutionConfig servicesExecutionConfig = new ServicesExecutionConfig();
            for (Tuple2<IPApplicationDefinitionService, Boolean> serviceAndIsInfra : serviceAndIsInfras) {
                IPApplicationDefinitionService service = serviceAndIsInfra.getA();
                ServicesExecutionServiceConfig serviceConfig = new ServicesExecutionServiceConfig();
                servicesExecutionConfig.getServices().add(serviceConfig);
                if (service.getRunAs() != null) {
                    serviceConfig.setUserID(service.getRunAs());
                    serviceConfig.setGroupID(service.getRunAs());
                }
                if (service.getWorkingDirectory() == null) {
                    serviceConfig.setWorkingDirectory("/tmp");
                } else {
                    serviceConfig.setWorkingDirectory(service.getWorkingDirectory());
                }
                serviceConfig.setCommand("/_infra/program_" + service.getName() + ".sh");

                Map<String, Object> model = new HashMap<>();
                model.put("portsRedirect", portsRedirect);
                model.put("command", service.getCommand());
                String serviceScriptContent;
                if (serviceAndIsInfra.getB() || portsRedirect.isEmpty()) {
                    // Just run the command
                    serviceScriptContent = FreemarkerTools.processTemplate("/com/foilen/infra/plugin/v1/model/outputter/docker/justRun.sh.ftl", model);
                } else {
                    serviceScriptContent = generateWaitInfraScript(portsRedirect, service.getCommand());
                }

                assetsBundle.addAssetContent("_infra/program_" + service.getName() + ".sh", serviceScriptContent);
            }

            // Main script
            transformedApplicationDefinition.setCommand(ctx.getServicesExecuteCommand() + " /_infra/services.json");
            transformedApplicationDefinition.setWorkingDirectory("/_infra/");
            transformedApplicationDefinition.setRunAs(0L);

            if (!infraChmodAdded) {
                transformedApplicationDefinition.addBuildStepCommand("chmod -R 777 /_infra/");
                infraChmodAdded = true;
            }

            assetsBundle.addAssetContent("_infra/services.json", JsonTools.prettyPrint(servicesExecutionConfig));

        } else {
            logger.info("[{}] Single command and no infra", imageName);

            if (!services.isEmpty()) {
                transformedApplicationDefinition.setCommand(services.get(0).getCommand());
            }
        }

        // If there are users to fix, add the script
        if (!transformedApplicationDefinition.getContainerUsersToChangeId().isEmpty()) {
            assetsBundle.addAssetResource("_infra/fixUserPermissions.sh", "/com/foilen/infra/plugin/v1/model/outputter/docker/fixUserPermissions.sh");
            if (!infraChmodAdded) {
                transformedApplicationDefinition.addBuildStepCommand("chmod -R 777 /_infra/");
                infraChmodAdded = true;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[{}] Final application definition\n{}", imageName, JsonTools.prettyPrint(transformedApplicationDefinition));
        }
        return transformedApplicationDefinition;
    }

    static protected String generateWaitInfraScript(List<IPApplicationDefinitionPortRedirect> portsRedirect, String command) {
        Map<String, Object> model = new HashMap<>();
        model.put("portsRedirect", portsRedirect.stream().map(it -> {
            return new IPApplicationDefinitionPortRedirectVisualWrapper(it);
        }).collect(Collectors.toList()));
        model.put("command", command);
        return FreemarkerTools.processTemplate("/com/foilen/infra/plugin/v1/model/outputter/docker/waitInfra.sh.ftl", model);
    }

    private static String getKnownDependencies(DockerContainerOutputContext ctx) {
        StringBuilder result = new StringBuilder();
        result.append("\n");
        for (String name : ctx.getRedirectIpByMachineContainerEndpoint().keySet()) {
            result.append("\t").append(name).append(" -> ");
            result.append(ctx.getRedirectIpByMachineContainerEndpoint().get(name)).append(":").append(ctx.getRedirectPortByMachineContainerEndpoint().get(name)).append("\n");
        }
        return result.toString();
    }

    private static void outputScript(String scriptName, String[] arguments) {
        logger.info("Creating script {}", scriptName);

        // Prepare content
        StringBuilder content = new StringBuilder();
        content.append("#!/bin/bash").append("\n");
        content.append("set -e").append("\n");
        content.append("RUN_PATH=\"$( cd \"$( dirname \"${BASH_SOURCE[0]}\" )\" && pwd )\"").append("\n");
        content.append("cd $RUN_PATH").append("\n");
        content.append("/usr/bin/docker ");
        for (String argument : arguments) {
            content.append(" '").append(argument).append("'");
        }
        content.append("\n");

        // Output
        FileTools.writeFile(content.toString(), new File(scriptName), "755");

    }

    protected static String sanitize(String text) {
        text = text.replaceAll("'", "\\\\'");
        text = text.replaceAll("\\\"", "\\\\\"");
        return text;
    }

    /**
     * Takes an application definition, creates a directory with all the starting scripts and the build directory.
     *
     * @param applicationDefinition
     *            the application to build
     * @param ctx
     *            the context where to build that application
     */
    static public void toDockerBuildDirectory(IPApplicationDefinition applicationDefinition, DockerContainerOutputContext ctx) {
        String imageName = ctx.getImageName();
        if (Strings.isNullOrEmpty(imageName)) {
            throw new ModelException("The instance name is not specified");
        }
        String outputDirectory = ctx.getOutputDirectory();
        if (Strings.isNullOrEmpty(outputDirectory)) {
            try {
                outputDirectory = Files.createTempDirectory(null).toFile().getAbsolutePath();
            } catch (IOException e) {
                throw new ModelException("Cannot create a temporary build folder", e);
            }
            logger.info("[{}] The build output is not specified. Will use a temporary one {}", imageName, outputDirectory);
        }
        outputDirectory = DirectoryTools.pathTrailingSlash(outputDirectory);
        ctx.setOutputDirectory(outputDirectory);

        String buildDirectory = outputDirectory + "build/";
        logger.info("[{}] Preparing build folder {}", imageName, buildDirectory);

        List<Tuple2<String, String>> assetsPathAndContent = applicationDefinition.getAssetsPathAndContent();
        logger.info("[{}] Copying {} asset files", imageName, assetsPathAndContent.size());
        for (Tuple2<String, String> assetPathAndContent : assetsPathAndContent) {
            String assetPath = assetPathAndContent.getA();
            String absoluteAssetPath = buildDirectory + assetPath;
            String content = assetPathAndContent.getB();
            DirectoryTools.createPathToFile(absoluteAssetPath);
            FileTools.writeFile(content, absoluteAssetPath);
        }

        List<IPApplicationDefinitionAssetsBundle> assetsBundles = applicationDefinition.getAssetsBundles();
        logger.info("[{}] Copying {} assets bundles", imageName, assetsBundles.size());
        for (IPApplicationDefinitionAssetsBundle assetBundles : assetsBundles) {
            String assetsFolderPath = assetBundles.getAssetsFolderPath();

            // Create the directory
            DirectoryTools.createPath(buildDirectory + assetsFolderPath);

            // Text files
            List<Tuple2<String, String>> assetsRelativePathAndTextContent = assetBundles.getAssetsRelativePathAndTextContent();
            for (Tuple2<String, String> assetRelativePathAndContent : assetsRelativePathAndTextContent) {
                String assetPath = assetsFolderPath + assetRelativePathAndContent.getA();
                String absoluteAssetPath = buildDirectory + assetPath;
                String content = assetRelativePathAndContent.getB();
                DirectoryTools.createPathToFile(absoluteAssetPath);
                FileTools.writeFile(content, absoluteAssetPath);
            }

            // Binary files
            List<Tuple2<String, byte[]>> assetsRelativePathAndBinaryContent = assetBundles.getAssetsRelativePathAndBinaryContent();
            for (Tuple2<String, byte[]> assetRelativePathAndContent : assetsRelativePathAndBinaryContent) {
                String assetPath = assetsFolderPath + assetRelativePathAndContent.getA();
                String absoluteAssetPath = buildDirectory + assetPath;
                byte[] content = assetRelativePathAndContent.getB();
                DirectoryTools.createPathToFile(absoluteAssetPath);
                FileTools.writeFile(content, absoluteAssetPath);
            }

        }

        logger.info("[{}] Creating DockerBuild file", imageName);
        String dockerFileContent = DockerContainerOutput.toDockerfile(applicationDefinition, ctx);
        FileTools.writeFile(dockerFileContent, buildDirectory + "Dockerfile");

        logger.info("[{}] Creating scripts", imageName);
        outputScript(outputDirectory + "build.sh", new String[] { "build", "-t", imageName, "build" });
        outputScript(outputDirectory + "start-single-pass-attached.sh", toRunArgumentsSinglePassAttached(applicationDefinition, ctx));
        outputScript(outputDirectory + "start-single-pass-detached.sh", toRunArgumentsSinglePassDetached(applicationDefinition, ctx, true));
        outputScript(outputDirectory + "start-restart.sh", toRunArgumentsWithRestart(applicationDefinition, ctx));

        logger.info("[{}] Preparing build folder {} completed", imageName, buildDirectory);

    }

    static protected String toDockerfile(IPApplicationDefinition applicationDefinition, DockerContainerOutputContext ctx) {
        StringBuilder content = new StringBuilder();
        // From
        content.append("FROM ").append(applicationDefinition.getFrom()).append("\n\n");

        // Steps
        for (IPApplicationDefinitionBuildStep step : applicationDefinition.getBuildSteps()) {
            switch (step.getType()) {
            case COMMAND:
                content.append("RUN ");
                break;
            case COPY:
                content.append("COPY ");
                break;
            }

            content.append(step.getStep()).append("\n");
        }

        // Fix permissions
        content.append("\n");
        for (Tuple2<String, Long> containerUserAndId : applicationDefinition.getContainerUsersToChangeId()) {
            content.append("RUN /_infra/fixUserPermissions.sh");
            content.append(" ").append(containerUserAndId.getA());
            content.append(" ").append(containerUserAndId.getA());
            content.append(" ").append(containerUserAndId.getB());
            content.append(" ").append(containerUserAndId.getB());
            content.append("\n");
        }
        content.append("\n");

        // Exposed ports
        if (!applicationDefinition.getPortsExposed().isEmpty()) {
            content.append("EXPOSE");
            for (Integer next : applicationDefinition.getPortsExposed().values()) {
                content.append(" ").append(next);
            }
            content.append("\n");
        }
        if (!applicationDefinition.getUdpPortsExposed().isEmpty()) {
            content.append("EXPOSE");
            for (Integer next : applicationDefinition.getUdpPortsExposed().values()) {
                content.append(" ").append(next).append("/udp");
            }
            content.append("\n");
        }

        if (!applicationDefinition.getPortsExposed().isEmpty() || !applicationDefinition.getUdpPortsExposed().isEmpty()) {
            content.append("\n");
        }

        // Volumes
        if (!applicationDefinition.getVolumes().isEmpty()) {
            List<String> containerVolumes = applicationDefinition.getVolumes().stream().map(it -> it.getContainerFsFolder()).collect(Collectors.toList());
            content.append("VOLUME ").append(JsonTools.compactPrint(containerVolumes)).append("\n");
            content.append("\n");
        }

        // Environment
        if (!applicationDefinition.getEnvironments().isEmpty()) {
            for (Entry<String, String> environment : applicationDefinition.getEnvironments().entrySet()) {
                content.append("ENV ").append(environment.getKey()).append("=").append(environment.getValue()).append("\n");
            }
            content.append("\n");
        }

        // User
        content.append("USER ").append(applicationDefinition.getRunAs()).append("\n\n");

        // Working directory
        String workingDirectory = applicationDefinition.getWorkingDirectory();
        if (!Strings.isNullOrEmpty(workingDirectory)) {
            content.append("WORKDIR ").append(workingDirectory).append("\n\n");
        }

        // Entrypoint
        if (applicationDefinition.getEntrypoint() != null) {
            content.append("ENTRYPOINT ");
            content.append(JsonTools.compactPrint(applicationDefinition.getEntrypoint())).append("\n");
        }

        return content.toString();
    }

    public static String[] toRunArgumentsSinglePassAttached(IPApplicationDefinition applicationDefinition, DockerContainerOutputContext ctx) {
        List<String> arguments = new ArrayList<>();

        arguments.add("run");

        arguments.add("-i");

        arguments.add("--rm");

        // Volumes
        for (IPApplicationDefinitionVolume volume : applicationDefinition.getVolumes()) {
            if (Strings.isNullOrEmpty(volume.getHostFolder())) {
                continue;
            }
            arguments.add("--volume");
            String volumeLine = sanitize(volume.getHostFolder() + ":" + sanitize(volume.getContainerFsFolder()));
            if (volume.isReadOnly()) {
                volumeLine += ":ro";
            }
            arguments.add(volumeLine);
        }

        // Exposed ports
        if (!applicationDefinition.getPortsExposed().isEmpty()) {
            for (Entry<Integer, Integer> entry : applicationDefinition.getPortsExposed().entrySet()) {
                arguments.add("--publish");
                arguments.add(entry.getKey() + ":" + entry.getValue());
            }
        }
        if (!applicationDefinition.getUdpPortsExposed().isEmpty()) {
            for (Entry<Integer, Integer> entry : applicationDefinition.getUdpPortsExposed().entrySet()) {
                arguments.add("--publish");
                arguments.add(entry.getKey() + ":" + entry.getValue() + "/udp");
            }
        }

        // Host to IP mapping
        for (Tuple2<String, String> hostToIp : applicationDefinition.getHostToIpMapping()) {
            arguments.add("--add-host");
            arguments.add(hostToIp.getA() + ":" + hostToIp.getB());
        }

        // Log
        if (ctx.getDockerLogsMaxSizeMB() != null) {
            arguments.add("--log-driver");
            arguments.add("json-file");
            arguments.add("--log-opt");
            arguments.add("max-size=" + ctx.getDockerLogsMaxSizeMB() + "m");
        }

        // Add user
        if (applicationDefinition.getRunAs() != null) {
            arguments.add("-u");
            arguments.add(applicationDefinition.getRunAs().toString());
        }

        // Instance name and hostname
        if (ctx.getContainerName() != null) {
            arguments.add("--name");
            arguments.add(ctx.getContainerName());
        }

        if (ctx.getHostName() != null) {
            arguments.add("--hostname");
            arguments.add(ctx.getHostName());
        }

        // Network
        if (ctx.getNetworkName() != null) {
            arguments.add("--network=" + ctx.getNetworkName());
        }
        if (ctx.getNetworkIp() != null) {
            arguments.add("--ip=" + ctx.getNetworkIp());
        }

        // Image
        arguments.add(ctx.getImageName());

        // Command
        if (applicationDefinition.getCommand() != null) {
            arguments.addAll(Arrays.asList(applicationDefinition.getCommand().split(" ")));
        }

        return arguments.toArray(new String[arguments.size()]);
    }

    public static String[] toRunArgumentsSinglePassDetached(IPApplicationDefinition applicationDefinition, DockerContainerOutputContext ctx, boolean removeWhenFinished) {
        List<String> arguments = new ArrayList<>();

        arguments.add("run");

        arguments.add("--detach");

        if (removeWhenFinished) {
            arguments.add("--rm");
        }

        // Volumes
        for (IPApplicationDefinitionVolume volume : applicationDefinition.getVolumes()) {
            if (Strings.isNullOrEmpty(volume.getHostFolder())) {
                continue;
            }
            arguments.add("--volume");
            String volumeLine = sanitize(volume.getHostFolder() + ":" + sanitize(volume.getContainerFsFolder()));
            if (volume.isReadOnly()) {
                volumeLine += ":ro";
            }
            arguments.add(volumeLine);
        }

        // Exposed ports
        if (!applicationDefinition.getPortsExposed().isEmpty()) {
            for (Entry<Integer, Integer> entry : applicationDefinition.getPortsExposed().entrySet()) {
                arguments.add("--publish");
                arguments.add(entry.getKey() + ":" + entry.getValue());
            }
        }
        if (!applicationDefinition.getUdpPortsExposed().isEmpty()) {
            for (Entry<Integer, Integer> entry : applicationDefinition.getUdpPortsExposed().entrySet()) {
                arguments.add("--publish");
                arguments.add(entry.getKey() + ":" + entry.getValue() + "/udp");
            }
        }

        // Host to IP mapping
        for (Tuple2<String, String> hostToIp : applicationDefinition.getHostToIpMapping()) {
            arguments.add("--add-host");
            arguments.add(hostToIp.getA() + ":" + hostToIp.getB());
        }

        // Log
        if (ctx.getDockerLogsMaxSizeMB() != null) {
            arguments.add("--log-driver");
            arguments.add("json-file");
            arguments.add("--log-opt");
            arguments.add("max-size=" + ctx.getDockerLogsMaxSizeMB() + "m");
        }

        // Add user
        if (applicationDefinition.getRunAs() != null) {
            arguments.add("-u");
            arguments.add(applicationDefinition.getRunAs().toString());
        }

        // Instance name and hostname
        if (ctx.getContainerName() != null) {
            arguments.add("--name");
            arguments.add(ctx.getContainerName());
        }

        if (ctx.getHostName() != null) {
            arguments.add("--hostname");
            arguments.add(ctx.getHostName());
        }

        // Network
        if (ctx.getNetworkName() != null) {
            arguments.add("--network=" + ctx.getNetworkName());
        }
        if (ctx.getNetworkIp() != null) {
            arguments.add("--ip=" + ctx.getNetworkIp());
        }

        // Image
        arguments.add(ctx.getImageName());

        // Command
        if (applicationDefinition.getCommand() != null) {
            arguments.addAll(Arrays.asList(applicationDefinition.getCommand().split(" ")));
        }

        return arguments.toArray(new String[arguments.size()]);
    }

    public static String[] toRunArgumentsWithRestart(IPApplicationDefinition applicationDefinition, DockerContainerOutputContext ctx) {
        List<String> arguments = new ArrayList<>();

        arguments.add("run");

        arguments.add("--detach");

        arguments.add("--restart");
        arguments.add("always");

        // Volumes
        for (IPApplicationDefinitionVolume volume : applicationDefinition.getVolumes()) {
            if (Strings.isNullOrEmpty(volume.getHostFolder())) {
                continue;
            }
            arguments.add("--volume");
            String volumeLine = sanitize(volume.getHostFolder() + ":" + sanitize(volume.getContainerFsFolder()));
            if (volume.isReadOnly()) {
                volumeLine += ":ro";
            }
            arguments.add(volumeLine);
        }

        // Exposed ports
        if (!applicationDefinition.getPortsExposed().isEmpty()) {
            for (Entry<Integer, Integer> entry : applicationDefinition.getPortsExposed().entrySet()) {
                arguments.add("--publish");
                arguments.add(entry.getKey() + ":" + entry.getValue());
            }
        }
        if (!applicationDefinition.getUdpPortsExposed().isEmpty()) {
            for (Entry<Integer, Integer> entry : applicationDefinition.getUdpPortsExposed().entrySet()) {
                arguments.add("--publish");
                arguments.add(entry.getKey() + ":" + entry.getValue() + "/udp");
            }
        }

        // Host to IP mapping
        for (Tuple2<String, String> hostToIp : applicationDefinition.getHostToIpMapping()) {
            arguments.add("--add-host");
            arguments.add(hostToIp.getA() + ":" + hostToIp.getB());
        }

        // Log
        if (ctx.getDockerLogsMaxSizeMB() != null) {
            arguments.add("--log-driver");
            arguments.add("json-file");
            arguments.add("--log-opt");
            arguments.add("max-size=" + ctx.getDockerLogsMaxSizeMB() + "m");
        }

        // Add user
        if (applicationDefinition.getRunAs() != null) {
            arguments.add("-u");
            arguments.add(applicationDefinition.getRunAs().toString());
        }

        // Instance name and hostname
        if (ctx.getContainerName() != null) {
            arguments.add("--name");
            arguments.add(ctx.getContainerName());
        }

        if (ctx.getHostName() != null) {
            arguments.add("--hostname");
            arguments.add(ctx.getHostName());
        }

        // Network
        if (ctx.getNetworkName() != null) {
            arguments.add("--network=" + ctx.getNetworkName());
        }
        if (ctx.getNetworkIp() != null) {
            arguments.add("--ip=" + ctx.getNetworkIp());
        }

        // Image
        arguments.add(ctx.getImageName());

        // Command
        if (applicationDefinition.getCommand() != null) {
            arguments.addAll(Arrays.asList(applicationDefinition.getCommand().split(" ")));
        }

        return arguments.toArray(new String[arguments.size()]);
    }

}
