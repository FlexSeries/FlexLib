/**
 * Copyright 2015 Stealth2800 <http://stealthyone.com/>
 * Copyright 2015 Contributors <https://github.com/FlexSeries>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.st28.flexseries.flexlib.command;

import me.st28.flexseries.flexlib.permission.PermissionNode;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains information about a command.
 */
public final class CommandDescriptor {

    private boolean isLocked = false;

    /**
     * The label(s) for the command, with the first entry being the main label.
     */
    private final List<String> labels = new ArrayList<>();

    /**
     * The description of the command.
     */
    private String description;

    /**
     * If this is a dummy command, no entry will show up in the help command.
     */
    private boolean dummy = false;

    /**
     * If this is set and the base command failed to run, the default command will run instead.
     */
    private String defaultCommand;

    /**
     * True if this command can only be run by players.
     */
    private boolean playerOnly = false;

    /**
     * The permission required to use this command.<br />
     * Null if no permission is required or will inherit if this is a subcommand and {@link #inheritPermission} is true.
     */
    private PermissionNode permission;

    /**
     * If true and the command this represents is a {@link Subcommand}, will inherit the parent permission
     * if {@link #permission} is null.
     */
    private boolean inheritPermission = true;

    public CommandDescriptor(String... labels) {
        labels(labels);
    }

    void lock() {
        if (isLocked) {
            throw new IllegalStateException("This descriptor is already locked.");
        }
        this.isLocked = true;
    }

    void checkLock() {
        if (isLocked) {
            throw new IllegalStateException("This descriptor can no longer be modified.");
        }
    }

    /**
     * @see #labels
     */
    public List<String> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    /**
     * @see #labels
     * @return This instance, for chaining.
     */
    public CommandDescriptor labels(String... labels) {
        checkLock();
        Validate.notNull(labels, "Labels cannot be null.");

        for (String label : labels) {
            Validate.notNull(label, "Label cannot be null.");

            label = label.toLowerCase();

            if (!this.labels.contains(label)) {
                this.labels.add(label);
            }
        }
        return this;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @see #description
     * @return This instance, for chaining.
     */
    public CommandDescriptor description(String description) {
        checkLock();
        this.description = description;
        return this;
    }

    /**
     * @see #dummy
     */
    public boolean isDummy() {
        return dummy;
    }

    /**
     * @see #dummy
     * @return This instance, for chaining.
     */
    public CommandDescriptor dummy(boolean dummy) {
        checkLock();
        this.dummy = dummy;
        return this;
    }

    /**
     * @see #defaultCommand
     * @return This instance, for chaining.
     */
    public String getDefaultCommand() {
        return defaultCommand;
    }

    /**
     * @see #defaultCommand
     * @return This instance, for chaining.
     */
    public CommandDescriptor defaultCommand(String defaultCommand) {
        this.defaultCommand = defaultCommand;
        return this;
    }

    /**
     * @see #playerOnly
     */
    public boolean isPlayerOnly() {
        return playerOnly;
    }

    /**
     * @see #playerOnly
     * @return This instance, for chaining.
     */
    public CommandDescriptor playerOnly(boolean playerOnly) {
        checkLock();
        this.playerOnly = playerOnly;
        return this;
    }

    /**
     * Note that this method does not check for inheritance ({@link #inheritPermission}). In
     * most cases, {@link AbstractCommand#getPermission()} should be used instead.
     *
     * @see #permission
     */
    public PermissionNode getPermission() {
        return permission;
    }

    /**
     * @see #permission
     * @return This instance, for chaining.
     */
    public CommandDescriptor permission(PermissionNode permission) {
        checkLock();
        this.permission = permission;
        return this;
    }

    /**
     * @see #inheritPermission
     */
    public boolean shouldInheritPermission() {
        return inheritPermission;
    }

    /**
     * @see #inheritPermission
     * @return This instance, for chaining.
     */
    public CommandDescriptor inheritPermission(boolean inheritPermission) {
        checkLock();
        this.inheritPermission = inheritPermission;
        return this;
    }

}