/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.mc2nms.cmds;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opennms.mc2nms.ChatHelper;
import org.opennms.mc2nms.MinecraftPlugin;
import org.opennms.mc2nms.Zone;

public class CommandGotoZone implements CommandExecutor, TabCompleter {
    private final MinecraftPlugin plugin;

    public CommandGotoZone(MinecraftPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            List<Zone> zones = plugin.getZones();
            Player p = (Player) sender;
            if (args.length >= 1) {
                String zoneName = args[0].toLowerCase().trim();
                for (Zone zone : zones) {
                    if (zoneName.equalsIgnoreCase(zone.getName())) {
                        p.sendMessage(ChatHelper.format(String.format("Teleporting to %s", zone.getName())));

                        // Use the zone's center, but overwrite the world with the player's current world
                        Location targetLocation = zone.getCenter().clone();
                        targetLocation.setWorld(p.getWorld());

                        p.teleport(targetLocation);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(command.getName().equalsIgnoreCase("gotozone") && args.length == 1) { //if we are on the first arg
            if(sender instanceof Player) { //and we are a player
                //return these commands to auto complete with
                return plugin.getZones().stream().map(Zone::getName).collect(Collectors.toList());
            }
        }
        return null;
    }
}
