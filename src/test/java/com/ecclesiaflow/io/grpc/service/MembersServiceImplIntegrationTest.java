package com.ecclesiaflow.io.grpc.service;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.grpc.members.ConfirmationStatusRequest;
import com.ecclesiaflow.grpc.members.ConfirmationStatusResponse;
import com.ecclesiaflow.grpc.members.MembersServiceGrpc;
import com.ecclesiaflow.io.grpc.server.MembersGrpcServiceImpl;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests d'intégration pour MembersGrpcServiceImpl.
 * <p>
 * Teste le serveur gRPC avec communication réelle via InProcessServer.
 * Couvre les scénarios de succès et d'erreur.
 * </p>
 */
class MembersServiceImplIntegrationTest {

    private Server server;
    private ManagedChannel channel;
    private MembersServiceGrpc.MembersServiceBlockingStub stub;

    private MemberRepository memberRepository;
    private ApplicationEventPublisher eventPublisher;

    private static final String SERVER_NAME = "test-members-grpc-service";
    private static final String CONFIRMED_EMAIL = "confirmed@example.com";
    private static final String UNCONFIRMED_EMAIL = "unconfirmed@example.com";
    private static final String NOT_FOUND_EMAIL = "notfound@example.com";
    private static final UUID MEMBER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        // Mock dependencies
        memberRepository = mock(MemberRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        // Créer le service gRPC
        MembersGrpcServiceImpl service = new MembersGrpcServiceImpl(memberRepository, eventPublisher);

        // Démarrer le serveur in-memory
        server = InProcessServerBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .addService(service)
                .build()
                .start();

        // Créer le canal client
        channel = InProcessChannelBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .build();

        // Créer le stub
        stub = MembersServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    @DisplayName("Should return confirmed=true when member is confirmed")
    void getMemberConfirmationStatus_MemberConfirmed() {
        // Given
        Member confirmedMember = Member.builder()
                .memberId(MEMBER_ID)
                .email(CONFIRMED_EMAIL)
                .firstName("John")
                .build()
                .confirm();

        when(memberRepository.getByEmail(CONFIRMED_EMAIL)).thenReturn(Optional.of(confirmedMember));

        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail(CONFIRMED_EMAIL)
                .build();

        // When
        ConfirmationStatusResponse response = stub.getMemberConfirmationStatus(request);

        // Then
        assertNotNull(response);
        assertTrue(response.getMemberExists());
        assertTrue(response.getIsConfirmed());
    }

    @Test
    @DisplayName("Should return confirmed=false when member is not confirmed")
    void getMemberConfirmationStatus_MemberNotConfirmed() {
        // Given
        Member unconfirmedMember = Member.builder()
                .memberId(MEMBER_ID)
                .email(UNCONFIRMED_EMAIL)
                .firstName("Jane")
                .build();

        when(memberRepository.getByEmail(UNCONFIRMED_EMAIL)).thenReturn(Optional.of(unconfirmedMember));

        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail(UNCONFIRMED_EMAIL)
                .build();

        // When
        ConfirmationStatusResponse response = stub.getMemberConfirmationStatus(request);

        // Then
        assertNotNull(response);
        assertTrue(response.getMemberExists());
        assertFalse(response.getIsConfirmed());
    }

    @Test
    @DisplayName("Should return memberExists=false when member does not exist")
    void getMemberConfirmationStatus_MemberNotFound() {
        // Given
        when(memberRepository.getByEmail(NOT_FOUND_EMAIL)).thenReturn(Optional.empty());

        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail(NOT_FOUND_EMAIL)
                .build();

        // When
        ConfirmationStatusResponse response = stub.getMemberConfirmationStatus(request);

        // Then
        assertNotNull(response);
        assertFalse(response.getMemberExists());
        assertFalse(response.getIsConfirmed());
    }

    @Test
    @DisplayName("Should throw INVALID_ARGUMENT for empty email")
    void getMemberConfirmationStatus_EmptyEmail() {
        // Given
        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail("")
                .build();

        // When/Then
        StatusRuntimeException exception = assertThrows(
                StatusRuntimeException.class,
                () -> stub.getMemberConfirmationStatus(request)
        );

        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Email cannot be empty"));
    }

    @Test
    @DisplayName("Should throw INTERNAL when repository throws exception")
    void getMemberConfirmationStatus_RepositoryError() {
        // Given
        when(memberRepository.getByEmail(anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail("test@example.com")
                .build();

        // When/Then
        StatusRuntimeException exception = assertThrows(
                StatusRuntimeException.class,
                () -> stub.getMemberConfirmationStatus(request)
        );

        assertEquals(Status.INTERNAL.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("An unexpected error occurred"));
    }

    @Test
    @DisplayName("Should handle multiple concurrent requests")
    void getMemberConfirmationStatus_ConcurrentRequests() throws InterruptedException {
        // Given
        Member member = Member.builder()
                .memberId(MEMBER_ID)
                .email(CONFIRMED_EMAIL)
                .firstName("John")
                .build()
                .confirm();

        when(memberRepository.getByEmail(CONFIRMED_EMAIL)).thenReturn(Optional.of(member));

        ConfirmationStatusRequest request = ConfirmationStatusRequest.newBuilder()
                .setEmail(CONFIRMED_EMAIL)
                .build();

        // When - Simuler plusieurs requêtes simultanées
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                ConfirmationStatusResponse response = stub.getMemberConfirmationStatus(request);
                assertNotNull(response);
                assertTrue(response.getMemberExists());
                assertTrue(response.getIsConfirmed());
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - All requests should succeed
    }

    @Test
    @DisplayName("Should validate that server is running")
    void serverIsRunning() {
        assertNotNull(server);
        assertFalse(server.isShutdown());
        assertFalse(server.isTerminated());
    }
}
