/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.outputter.docker;

import java.util.ArrayList;
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
import com.foilen.infra.plugin.v1.model.outputter.DockerMissingDependencyException;
import com.foilen.infra.plugin.v1.model.outputter.ModelException;
import com.foilen.infra.plugin.v1.model.outputter.haproxy.HaProxyConfigOutput;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.FreemarkerTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.ResourceTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Strings;
import com.google.common.io.Files;

/**
 * To get different outputs from a {@link IPApplicationDefinition}.
 */
public class DockerContainerOutput {

    private static final Logger logger = LoggerFactory.getLogger(DockerContainerOutput.class);

    // TODO Make sure users cannot take these names
    public static final String REDIRECTOR_ENTRY_CONTAINER_NAME = "infra_redirector_entry";
    public static final String REDIRECTOR_EXIT_CONTAINER_NAME = "infra_redirector_exit";

    static protected String generateWaitInfraScript(List<IPApplicationDefinitionPortRedirect> portsRedirect, String command) {
        Map<String, Object> model = new HashMap<>();
        model.put("portsRedirect", portsRedirect.stream().map(it -> {
            return new IPApplicationDefinitionPortRedirectVisualWrapper(it);
        }).collect(Collectors.toList()));
        model.put("command", command);
        return FreemarkerTools.processTemplate("/com/foilen/infra/plugin/v1/model/outputter/docker/waitInfra.sh.ftl", model);
    }

    protected static String sanitize(String text) {
        text = text.replaceAll("'", "\\\\'");
        text = text.replaceAll("\\\"", "\\\\\"");
        return text;
    }

    static public void toDockerBuildDirectory(IPApplicationDefinition applicationDefinition, DockerContainerOutputContext ctx) {
        String instanceName = ctx.getContainerName();
        String buildDirectory = ctx.getBuildDirectory();
        if (Strings.isNullOrEmpty(instanceName)) {
            throw new ModelException("The instance name is not specified");
        }
        if (Strings.isNullOrEmpty(buildDirectory)) {
            buildDirectory = Files.createTempDir().getAbsolutePath();
            logger.info("[{}] The build directory is not specified. Will use a temporary one {}", instanceName, buildDirectory);
        }
        buildDirectory = DirectoryTools.pathTrailingSlash(buildDirectory);
        ctx.setBuildDirectory(buildDirectory);

        logger.info("[{}] Transform the application", instanceName);
        // Clone
        IPApplicationDefinition transformedApplicationDefinition = JsonTools.clone(applicationDefinition);

        List<IPApplicationDefinitionService> services = transformedApplicationDefinition.getServices();
        List<Tuple2<IPApplicationDefinitionService, Boolean>> serviceAndIsInfras = services.stream().map(it -> {
            return new Tuple2<>(it, false);
        }).collect(Collectors.toList());

        // Use a single assetsBundle for (supervisor if more than 1 service ; if infra)
        List<IPApplicationDefinitionPortRedirect> portsRedirect = transformedApplicationDefinition.getPortsRedirect();
        if (!portsRedirect.isEmpty() || services.size() > 1) {
            logger.info("[{}] Has multiple services to run or some port redirects", instanceName);

            IPApplicationDefinitionAssetsBundle assetsBundle = transformedApplicationDefinition.addAssetsBundle();

            // Add infra if needed
            if (!portsRedirect.isEmpty()) {

                // Change the container to run as root, the haproxy as root and all the other services as the container user
                Integer runAs = transformedApplicationDefinition.getRunAs();
                if (runAs != null && runAs != 0) {
                    logger.info("[{}] Changing the instance user to root and all the services to {}", instanceName, runAs);
                    transformedApplicationDefinition.setRunAs(0);
                    for (IPApplicationDefinitionService service : transformedApplicationDefinition.getServices()) {
                        if (service.getRunAs() == null) {
                            service.setRunAs(runAs);
                        }
                    }
                }

                HaProxyConfig haProxyConfig = new HaProxyConfig();
                haProxyConfig.setUser(null);
                haProxyConfig.setGroup(null);
                boolean missingDependency = false;
                for (IPApplicationDefinitionPortRedirect portRedirect : portsRedirect) {
                    String machineContainerEndpoint = portRedirect.getMachineContainerEndpoint();

                    logger.info("[{}] Adding infra {} ; Key {}", instanceName, portRedirect, machineContainerEndpoint);

                    String host = ctx.getRedirectIpByMachineContainerEndpoint().get(machineContainerEndpoint);
                    Integer port = ctx.getRedirectPortByMachineContainerEndpoint().get(machineContainerEndpoint);

                    if (host == null || port == null) {
                        logger.error("[{}] Infra {} -> Missing dependency to {}", machineContainerEndpoint);
                        missingDependency = true;
                    } else {
                        logger.info("[{}] Infra {} -> {}:{}", instanceName, portRedirect.getLocalPort(), host, port);
                        haProxyConfig.addPortTcp(portRedirect.getLocalPort(), new Tuple2<>(host, port));
                    }

                }
                assetsBundle.addAssetContent("_infra/_infra_ha_proxy.cfg", HaProxyConfigOutput.toConfigFile(haProxyConfig));

                if (missingDependency) {
                    throw new DockerMissingDependencyException(instanceName + " missing dependency");
                }

                IPApplicationDefinitionService service = new IPApplicationDefinitionService("_infra_ha_proxy", HaProxyConfigOutput.toRun(haProxyConfig, "/_infra/_infra_ha_proxy.cfg"));
                Tuple2<IPApplicationDefinitionService, Boolean> serviceAndIsInfra = new Tuple2<>(service, true);
                serviceAndIsInfras.add(serviceAndIsInfra);
            }

            // Supervisor command
            StringBuilder supervisorConfigContent = new StringBuilder();
            supervisorConfigContent.append("[supervisord]\n");
            supervisorConfigContent.append("nodaemon=true\n\n");

            for (Tuple2<IPApplicationDefinitionService, Boolean> serviceAndIsInfra : serviceAndIsInfras) {
                IPApplicationDefinitionService service = serviceAndIsInfra.getA();
                supervisorConfigContent.append("[program:").append(service.getName()).append("]\n");
                if (service.getRunAs() != null) {
                    supervisorConfigContent.append("user=").append(service.getRunAs()).append("\n");
                }
                supervisorConfigContent.append("startretries=0\n");
                supervisorConfigContent.append("autorestart=false\n");
                supervisorConfigContent.append("redirect_stderr=true\n");
                supervisorConfigContent.append("stdout_logfile=/var/log/supervisor/").append(service.getName()).append(".log\n");
                supervisorConfigContent.append("command=/_infra/program_").append(service.getName()).append(".sh\n\n");

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
            String mainScriptContent = ResourceTools.getResourceAsString("startSupervisord.sh", DockerContainerOutput.class);
            assetsBundle.addAssetContent("_infra/startSupervisord.sh", mainScriptContent);
            transformedApplicationDefinition.setCommand("/_infra/startSupervisord.sh");
            transformedApplicationDefinition.setWorkingDirectory("/_infra/");

            transformedApplicationDefinition.addBuildStepCommand("chmod -R 777 /_infra/");

            assetsBundle.addAssetContent("_infra/supervisord.conf", supervisorConfigContent.toString());

        } else {
            logger.info("[{}] Single command and no infra", instanceName);

            if (!services.isEmpty()) {
                transformedApplicationDefinition.setCommand(services.get(0).getCommand());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[{}] Final application definition\n{}", instanceName, JsonTools.prettyPrint(transformedApplicationDefinition));
        }

        logger.info("[{}] Preparing build folder {}", instanceName, buildDirectory);

        List<Tuple2<String, String>> assetsPathAndContent = transformedApplicationDefinition.getAssetsPathAndContent();
        logger.info("[{}] Copying {} asset files", instanceName, assetsPathAndContent.size());
        for (Tuple2<String, String> assetPathAndContent : assetsPathAndContent) {
            String assetPath = assetPathAndContent.getA();
            String absoluteAssetPath = buildDirectory + assetPath;
            String content = assetPathAndContent.getB();
            DirectoryTools.createPathToFile(absoluteAssetPath);
            FileTools.writeFile(content, absoluteAssetPath);
        }

        List<IPApplicationDefinitionAssetsBundle> assetsBundles = transformedApplicationDefinition.getAssetsBundles();
        logger.info("[{}] Copying {} assets bundles", instanceName, assetsBundles.size());
        for (IPApplicationDefinitionAssetsBundle assetBundles : assetsBundles) {
            String assetsFolderPath = assetBundles.getAssetsFolderPath();
            List<Tuple2<String, String>> assetsRelativePathAndContent = assetBundles.getAssetsRelativePathAndContent();
            for (Tuple2<String, String> assetRelativePathAndContent : assetsRelativePathAndContent) {
                String assetPath = assetsFolderPath + assetRelativePathAndContent.getA();
                String absoluteAssetPath = buildDirectory + assetPath;
                String content = assetRelativePathAndContent.getB();
                DirectoryTools.createPathToFile(absoluteAssetPath);
                FileTools.writeFile(content, absoluteAssetPath);
            }

        }

        logger.info("[{}] Creating DockerBuild file", instanceName);
        String dockerFileContent = DockerContainerOutput.toDockerfile(transformedApplicationDefinition, ctx);
        FileTools.writeFile(dockerFileContent, buildDirectory + "Dockerfile");

        logger.info("[{}] Preparing build folder {} completed", instanceName, buildDirectory);

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
        for (Tuple2<String, Integer> containerUserAndId : applicationDefinition.getContainerUsersToChangeId()) {
            content.append("RUN ");
            content.append("FIX_CONTAINER_USER_ID=$(id -u ").append(containerUserAndId.getA()).append(") ;");
            content.append("FIX_CONTAINER_GROUP_ID=$(id -g ").append(containerUserAndId.getA()).append(") ;");
            content.append("usermod -u ").append(containerUserAndId.getB()).append(" ").append(containerUserAndId.getA()).append(" -o ;");
            content.append("groupmod -g ").append(containerUserAndId.getB()).append(" ").append(containerUserAndId.getA()).append(" -o ;");
            content.append("find /etc /home /opt /root /run /srv /tmp /usr /var -uid $FIX_CONTAINER_USER_ID -exec chown ").append(containerUserAndId.getA()).append(" {} \\; ;");
            content.append("find /etc /home /opt /root /run /srv /tmp /usr /var -gid $FIX_CONTAINER_GROUP_ID -exec chgrp ").append(containerUserAndId.getA()).append(" {} \\; ;");
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
        content.append("\n");

        // Volumes
        if (!applicationDefinition.getVolumes().isEmpty()) {
            List<String> containerVolumes = applicationDefinition.getVolumes().stream().map(it -> it.getContainerFsFolder()).collect(Collectors.toList());
            content.append("VOLUME ").append(JsonTools.compactPrint(containerVolumes)).append("\n");
        }
        content.append("\n");

        // User
        content.append("USER ").append(applicationDefinition.getRunAs()).append("\n\n");

        // Working directory
        String workingDirectory = applicationDefinition.getWorkingDirectory();
        if (!Strings.isNullOrEmpty(workingDirectory)) {
            content.append("WORKDIR ").append(workingDirectory).append("\n\n");
        }

        // Command
        content.append("CMD ");
        content.append(applicationDefinition.getCommand()).append("\n");

        return content.toString();
    }

    public static String[] toRunArgumentsSinglePassAttached(IPApplicationDefinition applicationDefinition, DockerContainerOutputContext ctx) {
        List<String> arguments = new ArrayList<>();

        arguments.add("run");

        arguments.add("-i");

        arguments.add("--rm");

        // Volumes
        for (IPApplicationDefinitionVolume volume : applicationDefinition.getVolumes()) {
            arguments.add("--volume");
            arguments.add(sanitize(volume.getHostFolder() + ":" + sanitize(volume.getContainerFsFolder())));
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

        arguments.add(ctx.getImageName());

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
            arguments.add("--volume");
            arguments.add(sanitize(volume.getHostFolder() + ":" + sanitize(volume.getContainerFsFolder())));
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

        arguments.add(ctx.getImageName());

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
            arguments.add("--volume");
            arguments.add(sanitize(volume.getHostFolder() + ":" + sanitize(volume.getContainerFsFolder())));
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

        arguments.add(ctx.getImageName());

        return arguments.toArray(new String[arguments.size()]);
    }

}
