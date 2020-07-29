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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

public class ZoneTrackerTest implements ZoneListener {

    private int zoneEntries = 0;
    private int zoneExits = 0;

    private ServerMock server;

    @Before
    public void setUp() {
        server = MockBukkit.mock();
    }

    @After
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void canTrackZone() {
        Location center = new Location(null, 0, 0, 0);
        Zone zone = new Zone("test", center, 1, 1);
        ZoneTracker zoneTracker = new ZoneTracker(Collections.singletonList(zone));
        zoneTracker.addListener(this);
        zoneTracker.trackCurrentLocations(Collections.emptyList());

        // No players in the zone yet
        assertThat(getNumPlayersInZone(), equalTo(0));

        // A new player joins in the center of the zone
        MyPlayerMock player = new MyPlayerMock(server, "me");
        player.setLocation(center);
        PlayerJoinEvent joinEvent = new PlayerJoinEvent(player, "hi");
        zoneTracker.onPlayerJoin(joinEvent);

        // One player in the zone
        assertThat(getNumPlayersInZone(), equalTo(1));

        // Move outside the zone
        Location outside = new Location(null, 99, 0, 0);
        PlayerMoveEvent moveOutsideOfZoneEvent = new PlayerMoveEvent(player, center, outside);
        zoneTracker.onPlayerMove(moveOutsideOfZoneEvent);

        // No players in the zone
        assertThat(getNumPlayersInZone(), equalTo(0));

        // Move back into the zone
        PlayerMoveEvent moveInsideOfZoneEvent = new PlayerMoveEvent(player, outside, center);
        zoneTracker.onPlayerMove(moveInsideOfZoneEvent);

        // One player in the zone
        assertThat(getNumPlayersInZone(), equalTo(1));

        // Player leaves
        PlayerQuitEvent quitEvent = new PlayerQuitEvent(player, "bye");
        zoneTracker.onPlayerQuit(quitEvent);

        // No players in the zone
        assertThat(getNumPlayersInZone(), equalTo(0));
    }

    private int getNumPlayersInZone() {
        return zoneEntries - zoneExits;
    }

    @Override
    public void onPlayerEnteredZone(Zone zone, Player player) {
        zoneEntries++;
    }

    @Override
    public void onPlayerLeftZone(Zone zone, Player player) {
        zoneExits++;
    }
}
