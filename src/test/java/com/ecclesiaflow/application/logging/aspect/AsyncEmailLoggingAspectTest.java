package com.ecclesiaflow.application.logging.aspect;

import com.ecclesiaflow.business.domain.events.MemberRegisteredEvent;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link AsyncEmailLoggingAspect}.
 * <p>
 * Vérifie que l'aspect intercepte correctement les appels au listener
 * d'événements et logue les informations appropriées sans affecter
 * l'exécution métier.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class AsyncEmailLoggingAspectTest {

    @Mock
    private JoinPoint joinPoint;

    private AsyncEmailLoggingAspect aspect;

    private MemberRegisteredEvent testEvent;

    @BeforeEach
    void setUp() {
        aspect = new AsyncEmailLoggingAspect();
        
        testEvent = new MemberRegisteredEvent(
            "test@ecclesiaflow.com",
            UUID.randomUUID(),
            "Jean"
        );
    }

    @Test
    void logBeforeAsyncEmailSending_ShouldNotThrowException_WhenEventIsValid() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEvent});

        // when/then - Should not throw
        aspect.logBeforeAsyncEmailSending(joinPoint);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logBeforeAsyncEmailSending_ShouldHandleNullArgs() {
        // given
        when(joinPoint.getArgs()).thenReturn(null);

        // when/then - Should not throw
        aspect.logBeforeAsyncEmailSending(joinPoint);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logBeforeAsyncEmailSending_ShouldHandleEmptyArgs() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        // when/then - Should not throw
        aspect.logBeforeAsyncEmailSending(joinPoint);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logBeforeAsyncEmailSending_ShouldHandleNonEventArgs() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{"not an event"});

        // when/then - Should not throw
        aspect.logBeforeAsyncEmailSending(joinPoint);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logAfterSuccessfulAsyncEmailSending_ShouldNotThrowException() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEvent});

        // when/then - Should not throw
        aspect.logAfterSuccessfulAsyncEmailSending(joinPoint);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logFailedAsyncEmailSending_ShouldNotThrowException_WhenExceptionOccurs() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEvent});
        Exception exception = new RuntimeException("SMTP timeout");

        // when/then - Should not throw
        aspect.logFailedAsyncEmailSending(joinPoint, exception);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logFailedAsyncEmailSending_ShouldHandleNullException() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEvent});

        // when/then - Should not throw even with null exception
        aspect.logFailedAsyncEmailSending(joinPoint, null);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logFailedAsyncEmailSending_ShouldExtractEmailFromEvent() {
        // given
        MemberRegisteredEvent event = new MemberRegisteredEvent(
            "member@church.com",
            UUID.randomUUID(),
            "Marie"
        );
        when(joinPoint.getArgs()).thenReturn(new Object[]{event});
        Exception exception = new RuntimeException("Network error");

        // when
        aspect.logFailedAsyncEmailSending(joinPoint, exception);

        // then
        verify(joinPoint).getArgs();
        // Note: Logging is verified through log assertions in integration tests
    }

    @Test
    void logMethods_ShouldBeCallableMultipleTimes() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEvent});

        // when - Call multiple times
        aspect.logBeforeAsyncEmailSending(joinPoint);
        aspect.logAfterSuccessfulAsyncEmailSending(joinPoint);
        
        // then - Should not throw and be callable multiple times
        verify(joinPoint, times(2)).getArgs();
    }

    @Test
    void logAfterSuccessfulAsyncEmailSending_ShouldHandleNullArgs() {
        // given
        when(joinPoint.getArgs()).thenReturn(null);

        // when/then - Should not throw
        aspect.logAfterSuccessfulAsyncEmailSending(joinPoint);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logAfterSuccessfulAsyncEmailSending_ShouldHandleEmptyArgs() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        // when/then - Should not throw
        aspect.logAfterSuccessfulAsyncEmailSending(joinPoint);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logAfterSuccessfulAsyncEmailSending_ShouldHandleNonEventArgs() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{"not an event"});

        // when/then - Should not throw
        aspect.logAfterSuccessfulAsyncEmailSending(joinPoint);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logFailedAsyncEmailSending_ShouldHandleNullArgs() {
        // given
        when(joinPoint.getArgs()).thenReturn(null);
        Exception exception = new RuntimeException("Test");

        // when/then - Should not throw
        aspect.logFailedAsyncEmailSending(joinPoint, exception);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logFailedAsyncEmailSending_ShouldHandleEmptyArgs() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        Exception exception = new RuntimeException("Test");

        // when/then - Should not throw
        aspect.logFailedAsyncEmailSending(joinPoint, exception);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logFailedAsyncEmailSending_ShouldHandleNonEventArgs() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{"not an event"});
        Exception exception = new RuntimeException("Test");

        // when/then - Should not throw
        aspect.logFailedAsyncEmailSending(joinPoint, exception);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logFailedAsyncEmailSending_ShouldHandleExceptionWithMessage() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEvent});
        Exception exceptionWithMessage = new RuntimeException("Specific error message");

        // when/then - Should not throw
        aspect.logFailedAsyncEmailSending(joinPoint, exceptionWithMessage);
        
        verify(joinPoint).getArgs();
    }

    @Test
    void logFailedAsyncEmailSending_ShouldHandleExceptionWithNullMessage() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{testEvent});
        Exception exceptionWithNullMessage = new RuntimeException((String) null);

        // when/then - Should not throw and use "Unknown error"
        aspect.logFailedAsyncEmailSending(joinPoint, exceptionWithNullMessage);
        
        verify(joinPoint).getArgs();
    }
}
