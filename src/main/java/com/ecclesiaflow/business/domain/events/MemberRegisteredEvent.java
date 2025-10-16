package com.ecclesiaflow.business.domain.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Événement de domaine publié après l'inscription réussie d'un membre.
 * <p>
 * Cet événement est utilisé pour déclencher les actions post-inscription
 * de manière asynchrone ET garantir qu'elles ne s'exécutent que si la
 * transaction d'inscription réussit (commit effectué).
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Domain Event - Event-Driven Architecture</p>
 * 
 * <p><strong>Garanties :</strong></p>
 * <ul>
 *   <li>Publié uniquement si la transaction commit avec succès</li>
 *   <li>Traité de manière asynchrone après le commit</li>
 *   <li>Découplage entre inscription et notifications</li>
 * </ul>
 * 
 * <p><strong>Flux d'exécution :</strong></p>
 * <pre>
 * 1. Transaction BEGIN
 * 2. Member.save()
 * 3. Confirmation.save()
 * 4. publishEvent(MemberRegisteredEvent)  ← Événement en queue
 * 5. Transaction COMMIT ✓
 * 6. @TransactionalEventListener déclenché APRÈS commit
 * 7. Traitement asynchrone (envoi email)
 * </pre>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class MemberRegisteredEvent {
    
    /**
     * Adresse email du membre inscrit.
     */
    private final String email;
    
    /**
     * Token de confirmation UUID généré pour ce membre.
     */
    private final UUID confirmationToken;
    
    /**
     * Prénom du membre pour personnalisation de l'email.
     */
    private final String firstName;
}
