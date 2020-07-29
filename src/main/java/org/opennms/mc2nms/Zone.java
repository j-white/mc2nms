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
    private final Location center;
    private final double radius;
    private final double height;

    public Zone(String name, Location center, double radius, double height) {
        this.name = Objects.requireNonNull(name);
        this.center = Objects.requireNonNull(center);
        this.radius = radius;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public Location getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public double getHeight() {
        return height;
    }

    public boolean isInZone(Location location) {
        // y-test
        if (location.getY() < center.getY() || location.getY() > (center.getY() + height)) {
            return false;
        }
        // x-z test - compare euclidean distance of location & midpoint of circle to radius
        return (float)Math.sqrt((int)Math.pow(location.getX()-center.getX(),2)+(int)Math.pow(location.getZ()-center.getZ(),2)) <= radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zone zone = (Zone) o;
        return Double.compare(zone.radius, radius) == 0 &&
                Double.compare(zone.height, height) == 0 &&
                Objects.equals(name, zone.name) &&
                Objects.equals(center, zone.center);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, center, radius, height);
    }

    @Override
    public String toString() {
        return "Zone{" +
                "name='" + name + '\'' +
                ", center=" + center +
                ", radius=" + radius +
                ", height=" + height +
                '}';
    }
}
