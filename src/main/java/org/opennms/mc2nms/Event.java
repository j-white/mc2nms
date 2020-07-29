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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Event {

    private String uei;
    private String host;
    private String source;
    private List<Parameter> parms = new LinkedList<>();

    public String getUei() {
        return uei;
    }

    public void setUei(String uei) {
        this.uei = uei;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Parameter> getParms() {
        return parms;
    }

    public void setParms(List<Parameter> parms) {
        this.parms = parms;
    }

    public void setParms(Map<String,String> parms) {
        this.parms = new ArrayList<>(
                parms.entrySet().stream()
                .map((e) -> new Parameter(e.getKey(), e.getValue()) )
                .collect(Collectors.toList())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(uei, event.uei) &&
                Objects.equals(host, event.host) &&
                Objects.equals(source, event.source) &&
                Objects.equals(parms, event.parms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uei, host, source, parms);
    }

    @Override
    public String toString() {
        return "Event{" +
                "uei='" + uei + '\'' +
                ", host='" + host + '\'' +
                ", source='" + source + '\'' +
                ", parms=" + parms +
                '}';
    }

    public static class Parameter {
        private String parmName;
        private String value;

        public Parameter(String name, String value) {
            this.parmName = name;
            this.value = value;
        }

        public Parameter() {
            // noarg
        }

        public String getParmName() {
            return parmName;
        }

        public void setParmName(String parmName) {
            this.parmName = parmName;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Parameter parameter = (Parameter) o;
            return Objects.equals(parmName, parameter.parmName) &&
                    Objects.equals(value, parameter.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parmName, value);
        }

        @Override
        public String toString() {
            return "Parameter{" +
                    "parmName='" + parmName + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}
