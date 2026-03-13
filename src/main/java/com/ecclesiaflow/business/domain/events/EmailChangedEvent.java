package com.ecclesiaflow.business.domain.events;

public record EmailChangedEvent(String oldEmail, String firstName) {
}
