/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.foilen.smalltools.tools.AbstractBasics;

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IPApplicationDefinitionVolume extends AbstractBasics {

    private String hostFolder;
    private String containerFsFolder;

    private Long ownerId;
    private Long groupId;

    private String permissions;

    private boolean readOnly;

    public IPApplicationDefinitionVolume() {
    }

    /**
     * Will just create a volume without mounting a folder. Good for discardable volumes.
     *
     * @param containerFsFolder
     *            the path inside the container
     *
     */
    public IPApplicationDefinitionVolume(String containerFsFolder) {
        this.containerFsFolder = containerFsFolder;
    }

    /**
     * Mount a file or folder without changing its permissions.
     *
     * @param hostFolder
     *            the path on the host
     * @param containerFsFolder
     *            the path inside the container
     */
    public IPApplicationDefinitionVolume(String hostFolder, String containerFsFolder) {
        this.hostFolder = hostFolder;
        this.containerFsFolder = containerFsFolder;
    }

    /**
     * A volume where the host's file or folder will have its owner/group/permissions changed.
     *
     * @param hostFolder
     *            the path on the host
     * @param containerFsFolder
     *            the path inside the container
     * @param ownerId
     *            the owner
     * @param groupId
     *            the group
     * @param permissions
     *            the permissions
     */
    public IPApplicationDefinitionVolume(String hostFolder, String containerFsFolder, Long ownerId, Long groupId, String permissions) {
        this.hostFolder = hostFolder;
        this.containerFsFolder = containerFsFolder;
        this.ownerId = ownerId;
        this.groupId = groupId;
        this.permissions = permissions;
    }

    /**
     * A volume where the host's file or folder will have its owner/group/permissions changed.
     *
     * @param hostFolder
     *            the path on the host
     * @param containerFsFolder
     *            the path inside the container
     * @param ownerId
     *            the owner
     * @param groupId
     *            the group
     * @param permissions
     *            the permissions
     * @param readOnly
     *            if the volume should be mounted as read-only
     */
    public IPApplicationDefinitionVolume(String hostFolder, String containerFsFolder, Long ownerId, Long groupId, String permissions, boolean readOnly) {
        this.hostFolder = hostFolder;
        this.containerFsFolder = containerFsFolder;
        this.ownerId = ownerId;
        this.groupId = groupId;
        this.permissions = permissions;
        this.readOnly = readOnly;
    }

    public String getContainerFsFolder() {
        return containerFsFolder;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getHostFolder() {
        return hostFolder;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getPermissions() {
        return permissions;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setContainerFsFolder(String containerFsFolder) {
        this.containerFsFolder = containerFsFolder;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void setHostFolder(String hostFolder) {
        this.hostFolder = hostFolder;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

}
