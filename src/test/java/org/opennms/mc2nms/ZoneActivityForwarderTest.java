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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class ZoneActivityForwarderTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Test
    public void canSendEvent() throws ExecutionException, InterruptedException {
        stubFor(post(urlEqualTo("/rest/events"))
                .willReturn(aResponse()
                        .withStatus(202)));

        // Initialize the forwarder
        MinecraftPlugin plugin = mock(MinecraftPlugin.class, RETURNS_DEEP_STUBS);
        OpenNMSServer server = new OpenNMSServer("test", wireMockRule.baseUrl(), "test", "test");
        ZoneActivityForwarder forwarder = new ZoneActivityForwarder(plugin, Collections.singletonList(server));

        // Send the event
        Event event = new Event();
        event.setUei(ZoneActivityForwarder.ENTERED_ZONE_UEI);
        forwarder.sendEvent(server, event).get();

        // Verify
        verify(postRequestedFor(urlEqualTo("/rest/events"))
                .withRequestBody(matching(".*devjam.*")));
    }
}
