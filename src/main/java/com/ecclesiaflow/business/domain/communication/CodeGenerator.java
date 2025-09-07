package com.ecclesiaflow.business.domain.communication;

/**
 * Port du domaine pour la génération de codes aléatoires.
 * <p>
 * Définit un contrat pour les services de génération de codes.
 * Le domaine métier peut utiliser cette interface sans connaître
 * l'implémentation concrète.
 * </p>
 */
public interface CodeGenerator {
    String generateCode();
}