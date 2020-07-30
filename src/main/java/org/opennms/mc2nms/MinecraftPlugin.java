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

package org.opennms.mc2nms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.opennms.mc2nms.cmds.CommandGetZones;
import org.opennms.mc2nms.cmds.CommandGotoZone;

import com.google.common.collect.ImmutableList;

public class MinecraftPlugin extends JavaPlugin implements ZoneListener {

    private List<Zone> zones = new LinkedList<>();
    private List<OpenNMSServer> servers = new LinkedList<>();

    @Override
    public void onEnable() {
        // Configuration handling
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        FileConfiguration config = getConfig();
        servers = getServersFromConfig(config);
        getLogger().info(String.format("Loaded %d OpenNMS servers from the config.", servers.size()));
        zones = getZonesFromConfig(config);
        getLogger().info(String.format("Loaded %d zones from the config.", zones.size()));

        // Register our commands
        getCommand("getzones").setExecutor(new CommandGetZones(this));
        getCommand("gotozone").setExecutor(new CommandGotoZone(this));

        // Setup zone tracking
        ZoneActivityForwarder zoneActivityForwarder = new ZoneActivityForwarder(this, servers);
        ZoneTracker zoneTracker = new ZoneTracker(zones);
        zoneTracker.addListener(zoneActivityForwarder);
        zoneTracker.addListener(this);

        // Seed tracking w/ current player locations
        zoneTracker.trackCurrentLocations(Bukkit.getServer().getOnlinePlayers());

        // Listen for move events
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(zoneTracker, this);
    }

    @Override
    public void onPlayerEnteredZone(Zone zone, Player player) {
        player.sendMessage(ChatHelper.format(String.format("You have entered the %s zone.", zone.getName())));
    }

    @Override
    public void onPlayerLeftZone(Zone zone, Player player) {
        player.sendMessage(ChatHelper.format(String.format("You have left the %s zone.", zone.getName())));
    }

    @Override
    public void onDisable() {
        getLogger().info("onDisable has been invoked!");
    }

    public static List<OpenNMSServer> getServersFromConfig(Configuration config) {
        final List<OpenNMSServer> servers = new ArrayList<>();
        final ConfigurationSection opennmsSection = config.getConfigurationSection("opennms");
        if (opennmsSection == null) {
            return servers;
        }
        for (String serverName : opennmsSection.getKeys(false)) {
            String baseUrl = opennmsSection.getString(serverName + ".baseurl");
            String username = opennmsSection.getString(serverName + ".username");
            String password = opennmsSection.getString(serverName + ".password");
            servers.add(new OpenNMSServer(serverName, baseUrl, username, password));
        }
        return servers;
    }

    public static List<Zone> getZonesFromConfig(Configuration config) {
        final List<Zone> zones = new ArrayList<>();
        final ConfigurationSection zonesSection = config.getConfigurationSection("zones");
        if (zonesSection == null) {
            return zones;
        }
        for (String zoneName : zonesSection.getKeys(false)) {
            String worldName = zonesSection.getString(zoneName + ".world");
            double x = zonesSection.getDouble(zoneName + ".x");
            double y = zonesSection.getDouble(zoneName + ".y");
            double z = zonesSection.getDouble(zoneName + ".z");
            double radius = zonesSection.getDouble(zoneName + ".radius");
            double height = zonesSection.getDouble(zoneName + ".height");
            zones.add(new Zone(zoneName, new Location(Bukkit.getWorld(worldName), x, y, z), radius, height));
        }
        return zones;
    }

    public List<Zone> getZones() {
        return ImmutableList.copyOf(zones);
    }

}
