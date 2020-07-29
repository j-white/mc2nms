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

import java.util.Objects;

import org.bukkit.Location;

public class Zone {
    private final String name;
    private final Location a;
    private final Location z;

    public Zone(String name, Location a, Location z) {
        this.name = Objects.requireNonNull(name);
        this.a = Objects.requireNonNull(a);
        this.z = Objects.requireNonNull(z);
    }

    public String getName() {
        return name;
    }

    public Location getA() {
        return a;
    }

    public Location getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zone zone = (Zone) o;
        return Objects.equals(name, zone.name) &&
                Objects.equals(a, zone.a) &&
                Objects.equals(z, zone.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, a, z);
    }

    @Override
    public String toString() {
        return "Zone{" +
                "name='" + name + '\'' +
                ", a=" + a +
                ", z=" + z +
                '}';
    }
}
