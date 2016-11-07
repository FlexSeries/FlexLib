/**
 * Copyright 2016 Stealth2800 <http://stealthyone.com/>
 * Copyright 2016 Contributors <https://github.com/FlexSeries>
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
package me.st28.flexseries.flexlib.permission

import org.bukkit.permissions.Permissible

interface PermissionNode {

    companion object {

        // I'm not sure if it's really worth caching variable nodes?
        //private val variableNodes: MutableMap<String, PermissionNode> = HashMap()

        fun buildVariableName(mainPerm: PermissionNode, vararg variables: String): PermissionNode {
            val node = "${mainPerm.node}.${variables.joinToString(separator = ".")}".toLowerCase()

            /*if (variableNodes.containsKey(node)) {
                return variableNodes[node]!!
            }*/

            val newNode = object: PermissionNode {
                override val node: String
                    get() = node
            }

            //variableNodes.put(node, newNode)
            return newNode
        }

    }

    fun isAllowed(permissible: Permissible): Boolean {
        return permissible.hasPermission(node)
    }

    val node: String

}

// Extension method
fun Permissible.hasPermission(perm: PermissionNode): Boolean = this.hasPermission(perm.node)
