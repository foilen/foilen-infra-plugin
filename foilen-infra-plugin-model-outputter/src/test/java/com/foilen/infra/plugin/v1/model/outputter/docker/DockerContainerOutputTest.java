/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.outputter.docker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinition;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionService;
import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionVolume;
import com.foilen.infra.plugin.v1.model.docker.DockerContainerEndpoints;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.ResourceTools;
import com.google.common.base.Joiner;

public class DockerContainerOutputTest {

    private static final Joiner joiner = Joiner.on(" ");

    private IPApplicationDefinition applicationDefinition;
    private DockerContainerOutputContext ctx;

    @Before
    public void init() {
        applicationDefinition = new IPApplicationDefinition();
        applicationDefinition.setFrom("ubuntu:16.04");
        applicationDefinition.addBuildStepCommand("export TERM=dumb ; apt-get update && apt-get install -y haproxy && apt-get clean && rm -rf /var/lib/apt/lists/*");
        applicationDefinition.addBuildStepCopy("asset/a.zip", "/tmp/a.zip");
        applicationDefinition.addBuildStepCommand("unzip /tmp/a.zip");
        applicationDefinition.addBuildStepCopy("asset/adir", "/asserts/adir");
        applicationDefinition.addContainerUserToChangeId("containerUser1", 1000L);
        applicationDefinition.addContainerUserToChangeId("containerUser2", 1000L);
        applicationDefinition.addVolume(new IPApplicationDefinitionVolume("/tmp/docker/config", "/volumes/config", null, null, null));
        applicationDefinition.addVolume(new IPApplicationDefinitionVolume("/tmp/docker/etc", "/volumes/etc", null, null, null, true));
        applicationDefinition.addPortExposed(80, 8080);
        applicationDefinition.addPortExposed(443, 8443);
        applicationDefinition.addPortRedirect(3306, "d001.node.example.com", "mysql01.db.example.com", DockerContainerEndpoints.MYSQL_TCP);
        applicationDefinition.addPortEndpoint(8080, "HTTP");
        applicationDefinition.setRunAs(10001L);
        applicationDefinition.setCommand("/usr/sbin/haproxy -f /volumes/config/haproxy");

        ctx = new DockerContainerOutputContext("Uroot_Stest", "Uroot_Stest", "Uroot_Stest");

    }

    @Test
    public void testSanitize() {
        Assert.assertEquals("/tmp/space", DockerContainerOutput.sanitize("/tmp/space"));
        Assert.assertEquals("/tmp/l\\'ecole", DockerContainerOutput.sanitize("/tmp/l'ecole"));
        Assert.assertEquals("/tmp/l\\\"ecole", DockerContainerOutput.sanitize("/tmp/l\"ecole"));
    }

    @Test
    public void testToDockerfile() {
        String actual = DockerContainerOutput.toDockerfile(applicationDefinition, ctx);
        String expected = ResourceTools.getResourceAsString("DockerContainerOutputTest-testToDockerfile-expected.txt", this.getClass());
        AssertTools.assertIgnoreLineFeed(expected, actual);
    }

    @Test
    public void testToDockerfile_NoInfra() {

        applicationDefinition.getPortsRedirect().clear();

        String actual = DockerContainerOutput.toDockerfile(applicationDefinition, ctx);
        String expected = ResourceTools.getResourceAsString("DockerContainerOutputTest-testToDockerfile_NoInfra-expected.txt", this.getClass());
        AssertTools.assertIgnoreLineFeed(expected, actual);
    }

    @Test
    public void testToDockerfile_NoInfra_MultipleServices() {
        applicationDefinition.getPortsRedirect().clear();
        applicationDefinition.getServices().add(new IPApplicationDefinitionService("other", "/other-start.sh", 0L));

        IPApplicationDefinition actual = DockerContainerOutput.addInfrastructure(applicationDefinition, ctx);
        AssertTools.assertJsonComparisonWithoutNulls("DockerContainerOutputTest-testToDockerfile_NoInfra_MultipleServices-expected.txt", this.getClass(), actual);
    }

    @Test
    public void testToDockerfile_WithInternalVolume() {

        applicationDefinition.addVolume(new IPApplicationDefinitionVolume(null, "/volumes/internal", null, null, null));

        String actual = DockerContainerOutput.toDockerfile(applicationDefinition, ctx);
        String expected = ResourceTools.getResourceAsString("DockerContainerOutputTest-testToDockerfile_WithInternalVolume-expected.txt", this.getClass());
        AssertTools.assertIgnoreLineFeed(expected, actual);
    }

    @Test
    public void testToDockerfile_workdirSpace() {

        applicationDefinition.setWorkingDirectory("/tmp/with space/here");

        String actual = DockerContainerOutput.toDockerfile(applicationDefinition, ctx);
        String expected = ResourceTools.getResourceAsString("DockerContainerOutputTest-testToDockerfile_workdirSpace-expected.txt", this.getClass());
        AssertTools.assertIgnoreLineFeed(expected, actual);
    }

    @Test
    public void testToRunArgumentsSinglePassAttached() {
        String expected = "run -i --rm --volume /tmp/docker/config:/volumes/config --volume /tmp/docker/etc:/volumes/etc:ro --publish 80:8080 --publish 443:8443 -u 10001 --name Uroot_Stest --hostname Uroot_Stest Uroot_Stest /usr/sbin/haproxy -f /volumes/config/haproxy";
        String actual = joiner.join(DockerContainerOutput.toRunArgumentsSinglePassAttached(applicationDefinition, ctx));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testToRunArgumentsSinglePassAttached_ip() {
        ctx.setNetworkName("fcloud").setNetworkIp("172.20.5.1");
        String expected = "run -i --rm --volume /tmp/docker/config:/volumes/config --volume /tmp/docker/etc:/volumes/etc:ro --publish 80:8080 --publish 443:8443 -u 10001 --name Uroot_Stest --hostname Uroot_Stest --network=fcloud --ip=172.20.5.1 Uroot_Stest /usr/sbin/haproxy -f /volumes/config/haproxy";
        String actual = joiner.join(DockerContainerOutput.toRunArgumentsSinglePassAttached(applicationDefinition, ctx));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testToRunArgumentsSinglePassAttached_WithInternalVolume() {

        applicationDefinition.addVolume(new IPApplicationDefinitionVolume(null, "/volumes/internal", null, null, null));

        String expected = "run -i --rm --volume /tmp/docker/config:/volumes/config --volume /tmp/docker/etc:/volumes/etc:ro --publish 80:8080 --publish 443:8443 -u 10001 --name Uroot_Stest --hostname Uroot_Stest Uroot_Stest /usr/sbin/haproxy -f /volumes/config/haproxy";
        String actual = joiner.join(DockerContainerOutput.toRunArgumentsSinglePassAttached(applicationDefinition, ctx));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testToRunCommandWithRestart() {
        String expected = "run --detach --restart always --volume /tmp/docker/config:/volumes/config --volume /tmp/docker/etc:/volumes/etc:ro --publish 80:8080 --publish 443:8443 -u 10001 --name Uroot_Stest --hostname Uroot_Stest Uroot_Stest /usr/sbin/haproxy -f /volumes/config/haproxy";
        String actual = joiner.join(DockerContainerOutput.toRunArgumentsWithRestart(applicationDefinition, ctx));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testToRunCommandWithRestart_log() {
        ctx.setDockerLogsMaxSizeMB(100);
        String expected = "run --detach --restart always --volume /tmp/docker/config:/volumes/config --volume /tmp/docker/etc:/volumes/etc:ro --publish 80:8080 --publish 443:8443 --log-driver json-file --log-opt max-size=100m -u 10001 --name Uroot_Stest --hostname Uroot_Stest Uroot_Stest /usr/sbin/haproxy -f /volumes/config/haproxy";
        String actual = joiner.join(DockerContainerOutput.toRunArgumentsWithRestart(applicationDefinition, ctx));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testToRunCommandWithRestart_NoInfra() {

        applicationDefinition.getPortsRedirect().clear();

        String expected = "run --detach --restart always --volume /tmp/docker/config:/volumes/config --volume /tmp/docker/etc:/volumes/etc:ro --publish 80:8080 --publish 443:8443 -u 10001 --name Uroot_Stest --hostname Uroot_Stest Uroot_Stest /usr/sbin/haproxy -f /volumes/config/haproxy";
        String actual = joiner.join(DockerContainerOutput.toRunArgumentsWithRestart(applicationDefinition, ctx));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testToRunCommandWithRestart_WithInternalVolume() {

        applicationDefinition.addVolume(new IPApplicationDefinitionVolume(null, "/volumes/internal", null, null, null));

        String expected = "run --detach --restart always --volume /tmp/docker/config:/volumes/config --volume /tmp/docker/etc:/volumes/etc:ro --publish 80:8080 --publish 443:8443 -u 10001 --name Uroot_Stest --hostname Uroot_Stest Uroot_Stest /usr/sbin/haproxy -f /volumes/config/haproxy";
        String actual = joiner.join(DockerContainerOutput.toRunArgumentsWithRestart(applicationDefinition, ctx));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testToRunCommandWithRestartAndIp() {
        String expected = "run --detach --restart always --volume /tmp/docker/config:/volumes/config --volume /tmp/docker/etc:/volumes/etc:ro --publish 80:8080 --publish 443:8443 -u 10001 --name Uroot_Stest --hostname Uroot_Stest Uroot_Stest /usr/sbin/haproxy -f /volumes/config/haproxy";
        String actual = joiner.join(DockerContainerOutput.toRunArgumentsWithRestart(applicationDefinition, ctx));
        Assert.assertEquals(expected, actual);
    }

}
