package com.ecclesiaflow.business.domain.events;

import java.util.UUID;

/**
 * Événement de domaine publié après l'inscription réussie d'un membre.
 * <p>
 * Cet événement est utilisé pour déclencher les actions post-inscription
 * de manière asynchrone ET garantir qu'elles ne s'exécutent que si la
 * transaction d'inscription réussit (commit effectué).
 * </p>
 *
 * @param email             Adresse email du membre inscrit.
 * @param confirmationToken Token de confirmation UUID généré pour ce membre.
 * @param firstName         Prénom du membre pour personnalisation de l'email.
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public record MemberRegisteredEvent(String email, UUID confirmationToken, String firstName) {

}
