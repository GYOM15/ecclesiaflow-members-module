package com.ecclesiaflow.application.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityMaskingUtils")
class SecurityMaskingUtilsTest {

    @Nested
    @DisplayName("maskEmail")
    class MaskEmailTests {

        @Test
        @DisplayName("should mask valid email")
        void shouldMaskValidEmail() {
            String result = SecurityMaskingUtils.maskEmail("john.doe@example.com");
            assertThat(result).isEqualTo("jo****@example.com");
        }

        @Test
        @DisplayName("should mask short local part")
        void shouldMaskShortLocalPart() {
            String result = SecurityMaskingUtils.maskEmail("ab@example.com");
            assertThat(result).isEqualTo("a****@example.com");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should return UNKNOWN for null or empty")
        void shouldReturnUnknownForNullOrEmpty(String email) {
            String result = SecurityMaskingUtils.maskEmail(email);
            assertThat(result).isEqualTo("[UNKNOWN]");
        }

        @Test
        @DisplayName("should return INVALID_FORMAT for invalid email")
        void shouldReturnInvalidFormatForInvalidEmail() {
            String result = SecurityMaskingUtils.maskEmail("not-an-email");
            assertThat(result).isEqualTo("[INVALID_FORMAT]");
        }
    }

    @Nested
    @DisplayName("maskConfirmationLink")
    class MaskConfirmationLinkTests {

        @Test
        @DisplayName("should mask token parameter in URL")
        void shouldMaskTokenInUrl() {
            String url = "https://example.com/confirm?token=abc123xyz";
            String result = SecurityMaskingUtils.maskConfirmationLink(url);
            assertThat(result).contains("token=****");
            assertThat(result).doesNotContain("abc123xyz");
        }

        @Test
        @DisplayName("should mask specific query param")
        void shouldMaskSpecificQueryParam() {
            String url = "https://example.com/confirm?code=secret123";
            String result = SecurityMaskingUtils.maskUrlQueryParam(url, "code");
            assertThat(result).contains("code=****");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should return UNKNOWN for null or empty URL")
        void shouldReturnUnknownForNullOrEmpty(String url) {
            String result = SecurityMaskingUtils.maskConfirmationLink(url);
            assertThat(result).isEqualTo("[UNKNOWN]");
        }
    }

    @Nested
    @DisplayName("maskId")
    class MaskIdTests {

        @Test
        @DisplayName("should mask UUID")
        void shouldMaskUuid() {
            UUID id = UUID.fromString("12345678-1234-1234-1234-123456789abc");
            String result = SecurityMaskingUtils.maskId(id);
            assertThat(result).isEqualTo("12345678********");
        }

        @Test
        @DisplayName("should mask string ID")
        void shouldMaskStringId() {
            String result = SecurityMaskingUtils.maskId("abcdefghij");
            assertThat(result).isEqualTo("abcdefgh********");
        }

        @Test
        @DisplayName("should return UNKNOWN for null")
        void shouldReturnUnknownForNull() {
            String result = SecurityMaskingUtils.maskId(null);
            assertThat(result).isEqualTo("[UNKNOWN]");
        }

        @Test
        @DisplayName("should handle short ID")
        void shouldHandleShortId() {
            String result = SecurityMaskingUtils.maskId("ab");
            assertThat(result).isEqualTo("********");
        }
    }

    @Nested
    @DisplayName("sanitizeInfra")
    class SanitizeInfraTests {

        @Test
        @DisplayName("should redact host:port patterns")
        void shouldRedactHostPort() {
            String message = "Connection to email-service:8080 failed";
            String result = SecurityMaskingUtils.sanitizeInfra(message);
            assertThat(result).contains("[HOST:PORT]");
            assertThat(result).doesNotContain("email-service:8080");
        }

        @Test
        @DisplayName("should redact URLs")
        void shouldRedactUrls() {
            String message = "Failed to connect to https://api.internal.com/path";
            String result = SecurityMaskingUtils.sanitizeInfra(message);
            assertThat(result).contains("[URL]");
        }

        @Test
        @DisplayName("should return blank for blank input")
        void shouldReturnBlankForBlank() {
            String result = SecurityMaskingUtils.sanitizeInfra("   ");
            assertThat(result).isEqualTo("   ");
        }
    }

    @Nested
    @DisplayName("rootMessage")
    class RootMessageTests {

        @Test
        @DisplayName("should return cause message if present")
        void shouldReturnCauseMessage() {
            Exception cause = new RuntimeException("Root cause");
            Exception exception = new RuntimeException("Wrapper", cause);
            String result = SecurityMaskingUtils.rootMessage(exception);
            assertThat(result).isEqualTo("Root cause");
        }

        @Test
        @DisplayName("should return main message if no cause")
        void shouldReturnMainMessageIfNoCause() {
            Exception exception = new RuntimeException("Main message");
            String result = SecurityMaskingUtils.rootMessage(exception);
            assertThat(result).isEqualTo("Main message");
        }

        @Test
        @DisplayName("should return NO_ERROR for null")
        void shouldReturnNoErrorForNull() {
            String result = SecurityMaskingUtils.rootMessage(null);
            assertThat(result).isEqualTo("[NO_ERROR]");
        }
    }

    @Nested
    @DisplayName("maskArgs")
    class MaskArgsTests {

        @Test
        @DisplayName("should mask email-like arguments")
        void shouldMaskEmailLikeArgs() {
            Object[] args = {"test@example.com", "normalArg"};
            String result = SecurityMaskingUtils.maskArgs(args);
            assertThat(result).contains("te****@example.com");
            assertThat(result).contains("normalArg");
        }

        @Test
        @DisplayName("should return empty brackets for null args")
        void shouldReturnEmptyBracketsForNullArgs() {
            String result = SecurityMaskingUtils.maskArgs(null);
            assertThat(result).isEqualTo("[]");
        }
    }

    @Nested
    @DisplayName("maskAny")
    class MaskAnyTests {

        @Test
        @DisplayName("should mask JWT token")
        void shouldMaskJwtToken() {
            String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";
            String result = SecurityMaskingUtils.maskAny(jwt);
            assertThat(result).isEqualTo("[REDACTED]");
        }

        @Test
        @DisplayName("should mask URL with token")
        void shouldMaskUrlWithToken() {
            String url = "https://example.com/confirm?token=secret123";
            String result = SecurityMaskingUtils.maskAny(url);
            assertThat(result).contains("token=****");
        }

        @Test
        @DisplayName("should mask bearer token")
        void shouldMaskBearerToken() {
            String bearer = "Bearer abc123xyz";
            String result = SecurityMaskingUtils.maskAny(bearer);
            assertThat(result).isEqualTo("Bearer ****");
        }

        @Test
        @DisplayName("should return UNKNOWN for null")
        void shouldReturnUnknownForNull() {
            String result = SecurityMaskingUtils.maskAny(null);
            assertThat(result).isEqualTo("[UNKNOWN]");
        }

        @Test
        @DisplayName("should return plain URL without token")
        void shouldReturnPlainUrlWithoutToken() {
            String url = "https://example.com/page";
            String result = SecurityMaskingUtils.maskAny(url);
            assertThat(result).isEqualTo("[URL]");
        }
    }

    @Nested
    @DisplayName("abbreviate")
    class AbbreviateTests {

        @Test
        @DisplayName("should abbreviate long string")
        void shouldAbbreviateLongString() {
            String longStr = "a".repeat(150);
            String result = SecurityMaskingUtils.abbreviate(longStr, 120);
            assertThat(result).hasSize(123);
            assertThat(result).endsWith("...");
        }

        @Test
        @DisplayName("should not abbreviate short string")
        void shouldNotAbbreviateShortString() {
            String result = SecurityMaskingUtils.abbreviate("short", 120);
            assertThat(result).isEqualTo("short");
        }

        @Test
        @DisplayName("should return UNKNOWN for null")
        void shouldReturnUnknownForNull() {
            String result = SecurityMaskingUtils.abbreviate(null, 120);
            assertThat(result).isEqualTo("[UNKNOWN]");
        }
    }

    @Nested
    @DisplayName("maskUrlQueryParam edge cases")
    class MaskUrlQueryParamEdgeCases {

        @Test
        @DisplayName("should return URL for null param name")
        void shouldReturnUrlForNullParamName() {
            String result = SecurityMaskingUtils.maskUrlQueryParam("https://example.com?a=1", null);
            assertThat(result).isEqualTo("[URL]");
        }

        @Test
        @DisplayName("should return URL when no query string")
        void shouldReturnUrlWhenNoQueryString() {
            String result = SecurityMaskingUtils.maskUrlQueryParam("https://example.com/page", "token");
            assertThat(result).isEqualTo("[URL]");
        }

        @Test
        @DisplayName("should handle param without value")
        void shouldHandleParamWithoutValue() {
            String result = SecurityMaskingUtils.maskUrlQueryParam("https://example.com?flag&token=secret", "token");
            assertThat(result).contains("flag");
            assertThat(result).contains("token=****");
        }
    }

    @Nested
    @DisplayName("rootMessage edge cases")
    class RootMessageEdgeCases {

        @Test
        @DisplayName("should return class name when message is null")
        void shouldReturnClassNameWhenMessageNull() {
            Exception ex = new RuntimeException((String) null);
            String result = SecurityMaskingUtils.rootMessage(ex);
            assertThat(result).isEqualTo("RuntimeException");
        }

        @Test
        @DisplayName("should return class name when message is blank")
        void shouldReturnClassNameWhenMessageBlank() {
            Exception ex = new RuntimeException("   ");
            String result = SecurityMaskingUtils.rootMessage(ex);
            assertThat(result).isEqualTo("RuntimeException");
        }
    }

    @Nested
    @DisplayName("maskEmail edge cases")
    class MaskEmailEdgeCases {

        @Test
        @DisplayName("should return UNKNOWN for blank email")
        void shouldReturnUnknownForBlankEmail() {
            String result = SecurityMaskingUtils.maskEmail("   ");
            assertThat(result).isEqualTo("[UNKNOWN]");
        }

        @Test
        @DisplayName("should return INVALID for email starting with @")
        void shouldReturnInvalidForEmailStartingWithAt() {
            String result = SecurityMaskingUtils.maskEmail("@domain.com");
            assertThat(result).isEqualTo("[INVALID_FORMAT]");
        }
    }

    @Nested
    @DisplayName("maskId edge cases")
    class MaskIdEdgeCases {

        @Test
        @DisplayName("should return UNKNOWN for blank string object")
        void shouldReturnUnknownForBlankStringObject() {
            String result = SecurityMaskingUtils.maskId("   ");
            assertThat(result).isEqualTo("[UNKNOWN]");
        }

        @Test
        @DisplayName("should mask non-UUID string longer than 8 chars")
        void shouldMaskNonUuidLongString() {
            String result = SecurityMaskingUtils.maskId("not-a-valid-uuid-string");
            assertThat(result).isEqualTo("not-a-va********");
        }
    }

    @Nested
    @DisplayName("maskAny edge cases")
    class MaskAnyEdgeCases {

        @Test
        @DisplayName("should return UNKNOWN for blank string")
        void shouldReturnUnknownForBlankString() {
            String result = SecurityMaskingUtils.maskAny("   ");
            assertThat(result).isEqualTo("[UNKNOWN]");
        }

        @Test
        @DisplayName("should mask http URL without token")
        void shouldMaskHttpUrlWithoutToken() {
            String result = SecurityMaskingUtils.maskAny("http://example.com/page");
            assertThat(result).isEqualTo("[URL]");
        }

        @Test
        @DisplayName("should abbreviate normal string")
        void shouldAbbreviateNormalString() {
            String result = SecurityMaskingUtils.maskAny("just a normal string");
            assertThat(result).isEqualTo("just a normal string");
        }
    }

    @Nested
    @DisplayName("sanitizeInfra edge cases")
    class SanitizeInfraEdgeCases {

        @Test
        @DisplayName("should redact hostname without port")
        void shouldRedactHostnameWithoutPort() {
            String result = SecurityMaskingUtils.sanitizeInfra("Connection to api.internal.com failed");
            assertThat(result).contains("[HOST]");
            assertThat(result).doesNotContain("api.internal.com");
        }

//        @Test
//        @DisplayName("should return null for null input")
//        void shouldReturnNullForNullInput() {
//            String result = SecurityMaskingUtils.sanitizeInfra(null);
//            assertThat(result).isNull();
//        }

        @Test
        @DisplayName("should return empty for empty input")
        void shouldReturnEmptyForEmptyInput() {
            String result = SecurityMaskingUtils.sanitizeInfra("");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("maskUrlQueryParam edge cases")
    class MaskUrlQueryParamCatchBlock {

        @Test
        @DisplayName("should return URL for blank param name")
        void shouldReturnUrlForBlankParamName() {
            String result = SecurityMaskingUtils.maskUrlQueryParam("https://example.com?a=1", "   ");
            assertThat(result).isEqualTo("[URL]");
        }

        @Test
        @DisplayName("should return UNKNOWN for blank URL")
        void shouldReturnUnknownForBlankUrl() {
            String result = SecurityMaskingUtils.maskUrlQueryParam("   ", "token");
            assertThat(result).isEqualTo("[UNKNOWN]");
        }

        @Test
        @DisplayName("should redact other params when masking specific one")
        void shouldRedactOtherParams() {
            String result = SecurityMaskingUtils.maskUrlQueryParam(
                    "https://example.com?token=secret&other=value&another=data", "token");
            assertThat(result).contains("token=****");
            assertThat(result).contains("other=[REDACTED]");
            assertThat(result).contains("another=[REDACTED]");
            assertThat(result).doesNotContain("secret");
            assertThat(result).doesNotContain("value");
        }
    }
}
