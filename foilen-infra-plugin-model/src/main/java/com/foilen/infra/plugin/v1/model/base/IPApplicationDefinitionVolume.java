/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IPApplicationDefinitionVolume {

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IPApplicationDefinitionVolume other = (IPApplicationDefinitionVolume) obj;
        if (containerFsFolder == null) {
            if (other.containerFsFolder != null) {
                return false;
            }
        } else if (!containerFsFolder.equals(other.containerFsFolder)) {
            return false;
        }
        if (groupId == null) {
            if (other.groupId != null) {
                return false;
            }
        } else if (!groupId.equals(other.groupId)) {
            return false;
        }
        if (hostFolder == null) {
            if (other.hostFolder != null) {
                return false;
            }
        } else if (!hostFolder.equals(other.hostFolder)) {
            return false;
        }
        if (ownerId == null) {
            if (other.ownerId != null) {
                return false;
            }
        } else if (!ownerId.equals(other.ownerId)) {
            return false;
        }
        if (permissions == null) {
            if (other.permissions != null) {
                return false;
            }
        } else if (!permissions.equals(other.permissions)) {
            return false;
        }
        if (readOnly != other.readOnly) {
            return false;
        }
        return true;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((containerFsFolder == null) ? 0 : containerFsFolder.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((hostFolder == null) ? 0 : hostFolder.hashCode());
        result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
        result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
        result = prime * result + (readOnly ? 1231 : 1237);
        return result;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IPApplicationDefinitionVolume [hostFolder=");
        builder.append(hostFolder);
        builder.append(", containerFsFolder=");
        builder.append(containerFsFolder);
        builder.append(", ownerId=");
        builder.append(ownerId);
        builder.append(", groupId=");
        builder.append(groupId);
        builder.append(", permissions=");
        builder.append(permissions);
        builder.append(", readOnly=");
        builder.append(readOnly);
        builder.append("]");
        return builder.toString();
    }

}
