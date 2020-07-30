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

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ZoneActivityForwarder implements ZoneListener {

    public static final String ENTERED_ZONE_UEI = "uei.opennms.org/devjam/2020/minecraft/playerEnteredZone";
    public static final String LEFT_ZONE_UEI = "uei.opennms.org/devjam/2020/minecraft/playerLeftZone";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();

    private final MinecraftPlugin plugin;
    private final OkHttpClient client;
    private List<OpenNMSServer> servers;

    public ZoneActivityForwarder(MinecraftPlugin plugin, List<OpenNMSServer> servers) {
        this.plugin = Objects.requireNonNull(plugin);
        this.servers = Objects.requireNonNull(servers);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);
        builder = configureToIgnoreCertificate(builder);
        client = builder.build();
    }

    @Override
    public void onPlayerEnteredZone(Zone zone, Player player) {
        sendEventsAsync(ENTERED_ZONE_UEI, zone, player);
    }

    @Override
    public void onPlayerLeftZone(Zone zone, Player player) {
        sendEventsAsync(LEFT_ZONE_UEI, zone, player);
    }

    public void sendEventsAsync(String uei, Zone zone, Player player) {
        sendEventsAsync(uei, zone, player.getDisplayName());
    }

    public void sendEventsAsync(String uei, Zone zone, String playerDisplayName) {
        final Map<String,String> parms = ImmutableMap.<String,String>builder()
                .put("zone", zone.getName())
                .put("player", playerDisplayName)
                .build();

        final Event event = new Event();
        event.setSource("mc2nms");
        event.setUei(uei);
        event.setParms(parms);

        for (OpenNMSServer server : servers) {
            sendEvent(server, event);
        }
    }

    public CompletableFuture<Void> sendEvent(OpenNMSServer server, Event event) {
        final HttpUrl url = HttpUrl.parse(server.getBaseUrl()).newBuilder()
                .addPathSegment("rest")
                .addPathSegment("events")
                .build();
        RequestBody body = RequestBody.create(gson.toJson(event), JSON);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", Credentials.basic(server.getUsername(), server.getPassword()))
                .post(body)
                .build();
        plugin.getLogger().fine(String.format("Asynchronously sending event with UEI=%s to server %s.", event.getUei(), url));
        CompletableFuture<Void> future = new CompletableFuture<>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                plugin.getLogger().warning(String.format("Failed to send event with UEI=%s to server: %s: %s", event.getUei(), url, e));
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    plugin.getLogger().fine(String.format("Successfully sent event with UEI=%s to server %s.", event.getUei(), url));
                    future.complete(null);
                } else {
                    String msg = String.format("Failed to send event with UEI=%s to server %s with response code: %d", event.getUei(), url, response.code());
                    future.completeExceptionally(new Exception(msg));
                    plugin.getLogger().warning(msg);
                }
                response.close();
            }
        });
        return future;
    }

    private static OkHttpClient.Builder configureToIgnoreCertificate(OkHttpClient.Builder builder) {
        try {

            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return builder;
    }
}
