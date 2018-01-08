/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.system.utils;

import java.util.Map;

public interface UnixShellAndFsUtils {

    /**
     * Execute command.
     *
     * @param actionDetails
     *            tells what the command is used for
     * @param command
     *            the command to run
     * @param arguments
     *            arguments
     * @throws UtilsException
     *             if fails
     */
    void executeCommandOrFail(String actionDetails, String command, String... arguments);

    /**
     * Execute command.
     *
     * @param actionName
     *            the name of the action to put between []
     * @param actionDetails
     *            tells what the command is used for
     * @param command
     *            the command to run
     * @param arguments
     *            arguments
     */
    void executeCommandQuiet(String actionName, String actionDetails, String command, String... arguments);

    /**
     * Change file or directory ownership and permissions.
     *
     * @param path
     *            the path to the file
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permission
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     */
    void fileChangeOwnerAndPermissions(String path, int owner, int group, String permission);

    /**
     * Delete some files.
     *
     * @param fileNames
     *            the full path to the files
     * @return true if at least one file was deleted
     */
    boolean fileDelete(String... fileNames);

    /**
     * Create a file.
     *
     * @param path
     *            the path to the file
     * @param content
     *            the text content
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permission
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     * @return true if the file was created or the content is different
     */
    boolean fileInstall(String path, String content, int owner, int group, String permission);

    /**
     * Create a file.
     *
     * @param pathParts
     *            the path to the file (will all be joined)
     * @param content
     *            the text content
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permission
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     * @return true if the file was created or the content is different
     */
    boolean fileInstall(String[] pathParts, String content, int owner, int group, String permission);

    /**
     * Create a file. Used by other resources to pass their name
     *
     * @param actionName
     *            the name of the action to put between []
     * @param path
     *            the path to the file
     * @param content
     *            the text content
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permissions
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     * @return true if the file was created or the content is different
     */
    boolean fileInstallQuiet(String actionName, String path, String content, int owner, int group, String permissions);

    /**
     * Create a folder.
     *
     * @param directoryPath
     *            the full path of the folder to create
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permission
     *            the permission of the file
     */
    void folderCreate(String directoryPath, int owner, int group, String permission);

    /**
     * Create a folder.
     *
     * @param directoryPathParts
     *            the full path of the folder to create (will all be joined)
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permission
     *            the permission of the file
     */
    void folderCreate(String[] directoryPathParts, int owner, int group, String permission);

    /**
     * To create a symbolic link.
     *
     * @param link
     *            the full path of the link
     * @param target
     *            the absolute or relative path to the target
     * @return true if was created
     */
    boolean linkCreate(String link, String target);

    /**
     * Install a file from a resource.
     *
     * @param resourceName
     *            the full path of the resource
     * @param installPath
     *            the full path of the file to create or update
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permission
     *            the permission of the file
     * @return true if created or updated
     */
    boolean resourceInstall(String resourceName, String installPath, int owner, int group, String permission);

    /**
     * Install a file from a Freemarker template.
     *
     * @param templateName
     *            the full path of the resource
     * @param installPath
     *            the full path of the file to create or update
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permission
     *            the permission of the file
     * @param model
     *            the variables in the template
     * @return true if created or updated
     */
    boolean templateInstall(String templateName, String installPath, int owner, int group, String permission, Map<String, ?> model);

}
