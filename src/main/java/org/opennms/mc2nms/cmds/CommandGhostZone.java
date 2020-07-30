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
import org.opennms.mc2nms.ZoneActivityForwarder;

/**
 * Add or remove fake players (ghosts) to zones.
 */
public class CommandGhostZone implements CommandExecutor, TabCompleter {
    private final MinecraftPlugin plugin;

    public CommandGhostZone(MinecraftPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            List<Zone> zones = plugin.getZones();
            Player p = (Player) sender;
            if (args.length == 2) {
                // FIXME: Refactor w/ gotozone
                Zone matchedZone = null;
                String zoneName = args[0].toLowerCase().trim();
                for (Zone zone : zones) {
                    if (zoneName.equalsIgnoreCase(zone.getName())) {
                        matchedZone = zone;
                        break;
                    }
                }
                if (matchedZone == null) {
                    p.sendMessage(ChatHelper.format(String.format("No zone found matching: ", zoneName)));
                    return false;
                }

                int k = Integer.parseInt(args[1]);
                if (k > 0) {
                    for (int i = 0; i < k; i++) {
                        plugin.getZoneActivityForwarder().sendEventsAsync(ZoneActivityForwarder.ENTERED_ZONE_UEI, matchedZone, "Ghost #" + i);
                    }
                } else if (k < 0) {
                    for (int i = k; i < 0; i++) {
                        plugin.getZoneActivityForwarder().sendEventsAsync(ZoneActivityForwarder.LEFT_ZONE_UEI, matchedZone, "Ghost #" + i);
                    }
                }

            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
