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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ZoneTracker implements Listener {

    private final List<Zone> zones;
    private final List<ZoneListener> listeners = new LinkedList<>();
    private final Map<Zone, Set<String>> playersByZone = new HashMap<>();

    public ZoneTracker(List<Zone> zones) {
        this.zones = Objects.requireNonNull(zones);
    }

    public void trackCurrentLocations(Collection<? extends Player> players) {
        for (Player p : players) {
            for (Zone z : zones) {
                if (z.isInZone(p.getLocation())) {
                    addPlayerToZone(p, z);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Location location = e.getTo();
        for (Zone z : zones) {
            if (z.isInZone(location)) {
                addPlayerToZone(e.getPlayer(), z);
            } else {
                removePlayerFromZone(e.getPlayer(), z);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Location location = e.getPlayer().getLocation();
        for (Zone z : zones) {
            if (z.isInZone(location)) {
                addPlayerToZone(e.getPlayer(), z);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        for (Zone z : zones) {
            removePlayerFromZone(e.getPlayer(), z);
        }
    }

    private synchronized void addPlayerToZone(Player player, Zone zone) {
        Set<String> playersInZone = playersByZone.computeIfAbsent(zone, (z) -> new HashSet<>());
        boolean didAdd = playersInZone.add(player.getPlayerListName());
        if (didAdd) {
            for (ZoneListener listener : listeners) {
                listener.onPlayerEnteredZone(zone, player);
            }
        }
    }

    private synchronized void removePlayerFromZone(Player player, Zone zone) {
        Set<String> playersInZone = playersByZone.computeIfAbsent(zone, (z) -> new HashSet<>());
        boolean didRemove = playersInZone.remove(player.getPlayerListName());
        if (didRemove) {
            for (ZoneListener listener : listeners) {
                listener.onPlayerLeftZone(zone, player);
            }
        }
    }

    public synchronized void addListener(ZoneListener listener) {
        listeners.add(listener);
    }

}
