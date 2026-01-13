package com.ecclesiaflow.application.config;

import io.grpc.ManagedChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("GrpcEmailClientConfig")
class GrpcEmailClientConfigTest {

    @Test
    @DisplayName("should create and shutdown gRPC channel without error")
    void shouldCreateAndShutdownChannel() {
        GrpcEmailClientConfig config = new GrpcEmailClientConfig("localhost", 6555, 1);

        ManagedChannel channel = config.emailGrpcChannel();
        assertThat(channel).isNotNull();

        assertThatNoException().isThrownBy(config::shutdown);
    }
}
