package com.ecclesiaflow.business.services;

/**
 * Service pour communiquer avec le module d'authentification centralisé
 */
public interface AuthModuleService {
    /**
     * Génère un token temporaire pour permettre la définition du mot de passe
     */
    String generateTemporaryToken(String email);

    }
