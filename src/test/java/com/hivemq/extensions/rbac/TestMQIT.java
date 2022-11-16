package com.hivemq.extensions.rbac;


import com.hivemq.client.internal.mqtt.message.MqttCommonReasonCode;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.testcontainer.core.MavenHiveMQExtensionSupplier;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.*;

public class TestMQIT {

    @RegisterExtension
    public final @NotNull HiveMQTestContainerExtension container =
            new HiveMQTestContainerExtension()
                    .withExtension(MavenHiveMQExtensionSupplier.direct().get());

    @Test
    void test_mqtt_connect_success() {
        final Mqtt5BlockingClient publisher = Mqtt5Client.builder()
                .serverPort(container.getMqttPort())
                .identifier("publisher")
                .simpleAuth().username("user1").password("pass1".getBytes()).applySimpleAuth()
                .buildBlocking();

        Mqtt5ConnAck output = publisher.connect();
        assertNotNull(output);
        assertEquals(MqttCommonReasonCode.SUCCESS.getCode(), output.getReasonCode().getCode());
    }

    @Test
    void test_mqtt_connect_error_not_authorized_topic() {
        final Mqtt5BlockingClient publisher = Mqtt5Client.builder()
                .serverPort(container.getMqttPort())
                .identifier("publisher")
                .simpleAuth().username("user1").password("pass1".getBytes()).applySimpleAuth()
                .buildBlocking();

        Mqtt5ConnAck output = publisher.connect();
        assertEquals(MqttCommonReasonCode.SUCCESS.getCode(), output.getReasonCode().getCode());

        Mqtt5SubAckException thrown = assertThrows(
                Mqtt5SubAckException.class,
                ()-> publisher.subscribeWith().topicFilter("outgoing/publisher").send()
        );
        assertEquals(Mqtt5SubAckReasonCode.NOT_AUTHORIZED, thrown.getMqttMessage().getReasonCodes().get(0));
    }

    @Test
    void test_mqtt_connect_error_wrong_credentials() {
        final Mqtt5Client publisher = Mqtt5Client.builder()
                .serverPort(container.getMqttPort())
                .identifier("publisher")
                .simpleAuth().username("user1").password("pass2".getBytes()).applySimpleAuth()
                .build();

        Mqtt5ConnAckException thrown = assertThrows(
                Mqtt5ConnAckException.class,
                ()-> publisher.toBlocking().connect()
        );
        assertEquals(Mqtt5ConnAckReasonCode.NOT_AUTHORIZED, thrown.getMqttMessage().getReasonCode());
    }

    @Test
    void test_mqtt_connect_error_wrong_username() {
        final Mqtt5Client publisher = Mqtt5Client.builder()
                .serverPort(container.getMqttPort())
                .identifier("publisher")
                .simpleAuth().username("user100").password("pass1".getBytes()).applySimpleAuth()
                .build();

        Mqtt5ConnAckException thrown = assertThrows(
                Mqtt5ConnAckException.class,
                ()-> publisher.toBlocking().connect()
        );
        assertEquals(Mqtt5ConnAckReasonCode.NOT_AUTHORIZED, thrown.getMqttMessage().getReasonCode());
    }

    @Test
    void test_mqtt_connect_error_plus_wildcard_in_clientid() {
        final Mqtt5Client publisher = Mqtt5Client.builder()
                .serverPort(container.getMqttPort())
                .identifier("publisher/+")
                .simpleAuth().username("user1").password("pass1".getBytes()).applySimpleAuth()
                .build();

        Mqtt5ConnAckException thrown = assertThrows(
                Mqtt5ConnAckException.class,
                ()-> publisher.toBlocking().connect()
        );
        assertEquals(Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID, thrown.getMqttMessage().getReasonCode());
    }

    @Test
    void test_mqtt_connect_error_wildcard_in_clientid() {
        final Mqtt5Client publisher = Mqtt5Client.builder()
                .serverPort(container.getMqttPort())
                .identifier("publisher/#")
                .simpleAuth().username("user1").password("pass1".getBytes()).applySimpleAuth()
                .build();

        Mqtt5ConnAckException thrown = assertThrows(
                Mqtt5ConnAckException.class,
                ()-> publisher.toBlocking().connect()
        );
        assertEquals(Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID, thrown.getMqttMessage().getReasonCode());
    }

    @Test
    void test_mqtt_connect_error_plus_wildcard_in_username() {
        final Mqtt5Client publisher = Mqtt5Client.builder()
                .serverPort(container.getMqttPort())
                .identifier("publisher")
                .simpleAuth().username("user1/+").password("pass1".getBytes()).applySimpleAuth()
                .build();

        Mqtt5ConnAckException thrown = assertThrows(
                Mqtt5ConnAckException.class,
                ()-> publisher.toBlocking().connect()
        );
        assertEquals(Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID, thrown.getMqttMessage().getReasonCode());
    }

    @Test
    void test_mqtt_connect_error_wildcard_in_username() {
        final Mqtt5Client publisher = Mqtt5Client.builder()
                .serverPort(container.getMqttPort())
                .identifier("publisher")
                .simpleAuth().username("user1/#").password("pass1".getBytes()).applySimpleAuth()
                .build();

        Mqtt5ConnAckException thrown = assertThrows(
                Mqtt5ConnAckException.class,
                ()-> publisher.toBlocking().connect()
        );
        assertEquals(Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID, thrown.getMqttMessage().getReasonCode());
    }
}
