/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mqtt.generic.discovery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.discovery.AbstractMQTTDiscovery;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.generic.internal.MqttBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This handles discovery of new Things
 *
 * @author Bjarne Loft- Initial contribution
 */
@Component(immediate = true, service = DiscoveryService.class, configurationPid = "discovery.mqtt")
@NonNullByDefault
public class GenericDiscovery extends AbstractMQTTDiscovery {
    private final Logger logger = LoggerFactory.getLogger(GenericDiscovery.class);

    public GenericDiscovery() {
        super(Stream.of(MqttBindingConstants.GENERIC_MQTT_THING).collect(Collectors.toSet()), 3, true, "+/+/openhab");
    }

    @NonNullByDefault({})
    protected MQTTTopicDiscoveryService discoveryService;

    @Reference
    public void setMQTTTopicDiscoveryService(MQTTTopicDiscoveryService service) {
        discoveryService = service;
    }

    public void unsetMQTTTopicDiscoveryService(@Nullable MQTTTopicDiscoveryService service) {
        discoveryService.unsubscribe(this);
        this.discoveryService = null;
    }

    @Override
    protected MQTTTopicDiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    @Override
    public void receivedMessage(ThingUID connectionBridge, MqttBrokerConnection connection, String topic, byte[] payload) {
        Gson gson = new GsonBuilder().create();
        Map data = gson.fromJson(new String(payload), Map.class);

        logger.debug("Message recieved");

        logger.debug("Payload: {}", new String(payload));

        String id = data.get("id").toString();
        String label = data.getOrDefault("label", id).toString();

        Map<String, Object> properties = new HashMap<>();
        properties.put("deviceid", id);
        properties.put("payload", new String(payload));

        if(data.containsKey("properties") && data.get("properties") instanceof Map) {
            properties.putAll((Map)data.get("properties"));
        }

        thingDiscovered(DiscoveryResultBuilder
                .create(new ThingUID(MqttBindingConstants.GENERIC_MQTT_THING, connectionBridge, id))
                .withBridge(connectionBridge)
                .withProperties(properties)
                .withRepresentationProperty("deviceid")
                .withLabel(label)
                .build());
    }

    @Override
    public void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic) {

    }
}
