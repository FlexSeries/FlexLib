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
package me.st28.flexseries.flexlib.permission;

import me.st28.flexseries.flexlib.utils.StringUtils;
import org.bukkit.permissions.Permissible;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a permission node.
 */
public interface PermissionNode {

    /**
     * @return True if the permissible has permission for this node.
     */
    default boolean isAllowed(Permissible permissible) {
        return permissible.hasPermission(getNode());
    }

    /**
     * @return the string representation of the permission node.
     */
    String getNode();

    static PermissionNode buildVariableNode(PermissionNode mainPerm, String... variables) {
        final String node = mainPerm.getNode() + "." + StringUtils.collectionToString(Arrays.asList(variables), ".").toLowerCase();

        if (VARIABLE_NODES.containsKey(node)) {
            return VARIABLE_NODES.get(node);
        }

        PermissionNode newNode = () -> node;

        VARIABLE_NODES.put(node, newNode);
        return newNode;
    }

    Map<String, PermissionNode> VARIABLE_NODES = new HashMap<>();

}