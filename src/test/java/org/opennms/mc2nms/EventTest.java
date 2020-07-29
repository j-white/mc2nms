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

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

public class EventTest {

    @Test
    public void canMarshalToJson() {
        Gson gson = new Gson();
        Event event = new Event();
        event.setUei("uei.opennms.org/internal/authentication/failure");
        event.setHost("localhost");
        event.setParms(ImmutableMap.<String,String>builder()
                .put("exceptionMessage", "I forgot it again")
                .put("ip", "127.0.0.1")
                .put("user", "me")
                .build());
        String actualJson = gson.toJson(event);
        String expectedJson = "{\n" +
                "\"uei\": \"uei.opennms.org/internal/authentication/failure\",\n" +
                "\"host\": \"localhost\",\n" +
                "\"parms\": [\n" +
                "  {\n" +
                "    \"parmName\": \"exceptionMessage\",\n" +
                "    \"value\": \"I forgot it again\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"parmName\": \"ip\",\n" +
                "    \"value\": \"127.0.0.1\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"parmName\": \"user\",\n" +
                "    \"value\": \"me\"\n" +
                "  }\n" +
                "]\n" +
                "}";
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);

    }
}
