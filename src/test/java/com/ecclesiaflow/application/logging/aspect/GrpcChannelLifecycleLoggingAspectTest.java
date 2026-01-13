package com.ecclesiaflow.application.logging.aspect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("GrpcChannelLifecycleLoggingAspect")
class GrpcChannelLifecycleLoggingAspectTest {

    private final GrpcChannelLifecycleLoggingAspect aspect = new GrpcChannelLifecycleLoggingAspect();

    @Test
    @DisplayName("should log before channel creation without error")
    void shouldLogBeforeChannelCreation() {
        ReflectionTestUtils.setField(aspect, "emailHost", "localhost");
        ReflectionTestUtils.setField(aspect, "emailPort", 9092);

        assertThatNoException().isThrownBy(aspect::logBeforeChannelCreation);
    }

    @Test
    @DisplayName("should log after channel creation without error")
    void shouldLogAfterChannelCreation() {
        assertThatNoException().isThrownBy(aspect::logAfterChannelCreation);
    }

    @Test
    @DisplayName("should log after client creation without error")
    void shouldLogAfterClientCreation() {
        assertThatNoException().isThrownBy(aspect::logAfterClientCreation);
    }

    @Test
    @DisplayName("should log before shutdown without error")
    void shouldLogBeforeShutdown() {
        assertThatNoException().isThrownBy(aspect::logBeforeShutdown);
    }

    @Test
    @DisplayName("should log after shutdown without error")
    void shouldLogAfterShutdown() {
        assertThatNoException().isThrownBy(aspect::logAfterShutdown);
    }
}
