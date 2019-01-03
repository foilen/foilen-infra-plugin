/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.base;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.tools.JsonTools;

public class IPApplicationDefinitionTest {

    @Test
    public void testToImageUniqueId() {

        // Prepare
        IPApplicationDefinition definition = new IPApplicationDefinition();

        definition.addBuildStepCommand("clear");
        definition.addBuildStepCopy("a", "b");
        definition.addPortExposed(53, 53);
        definition.addUdpPortExposed(53, 53);
        definition.addPortEndpoint(53, "DNS_TCP");
        definition.addVolume(new IPApplicationDefinitionVolume("/var/log"));
        definition.addAssetsBundle() //
                .addAssetContent("/c", "hello") //
                .addAssetContent("/d", "hello".getBytes());
        definition.addContainerUserToChangeId("www-data", 70000L);

        // Ensure 2 instances have the same image id
        String firstId = definition.toImageUniqueId();
        definition = JsonTools.clone(definition);
        String secondId = definition.toImageUniqueId();
        Assert.assertEquals(firstId, secondId);

    }

}
