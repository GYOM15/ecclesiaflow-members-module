package com.ecclesiaflow.business.exceptions;

import com.ecclesiaflow.application.logging.SecurityMaskingUtils;
import io.grpc.Status;

/**
 * Exception thrown on gRPC communication failures.
 */
public class GrpcCommunicationException extends RuntimeException {

    private final String serviceName;
    private final String methodName;
    private final Status.Code statusCode;
    private final String statusDescription;

    public GrpcCommunicationException(
            String serviceName,
            String methodName,
            Status.Code statusCode,
            String statusDescription,
            Throwable cause) {
        super(buildSafeMessage(serviceName, methodName, statusCode), cause);
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
    }

    private static String buildSafeMessage(String serviceName, String methodName, Status.Code statusCode) {
        return String.format("gRPC call failed: %s.%s - %s", serviceName, methodName, statusCode);
    }

    public String getSanitizedDescription() {
        return SecurityMaskingUtils.sanitizeInfra(statusDescription);
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Status.Code getStatusCode() {
        return statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }
}
