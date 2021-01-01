/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.model.outputter.servicesExecution;

import com.foilen.smalltools.tools.AbstractBasics;

public class ServicesExecutionServiceConfig extends AbstractBasics {

    private long userID;
    private long groupID;
    private String workingDirectory;
    private String command;

    public ServicesExecutionServiceConfig() {
    }

    public ServicesExecutionServiceConfig(long userID, long groupID, String workingDirectory, String command) {
        this.userID = userID;
        this.groupID = groupID;
        this.workingDirectory = workingDirectory;
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public long getGroupID() {
        return groupID;
    }

    public long getUserID() {
        return userID;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

}
