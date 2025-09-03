package com.ecclesiaflow.web.config;

import com.ecclesiaflow.business.services.MemberPasswordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration des clients HTTP réactifs pour les appels inter-modules.
 * <p>
 * Cette classe configure les beans {@link WebClient} nécessaires pour communiquer
 * avec les modules externes d'EcclesiaFlow, notamment le module d'authentification.
 * Elle utilise des propriétés de configuration pour définir les URLs de base
 * et peut être désactivée conditionnellement.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Configuration - Clients HTTP inter-modules</p>
 * 
 * <p><strong>Modules intégrés :</strong></p>
 * <ul>
 *   <li>Module d'authentification : gestion des mots de passe et tokens</li>
 *   <li>Autres modules EcclesiaFlow (extensible)</li>
 * </ul>
 * 
 * <p><strong>Configuration conditionnelle :</strong></p>
 * <ul>
 *   <li>Activée par défaut (matchIfMissing = true)</li>
 *   <li>Peut être désactivée via ecclesiaflow.auth.module.enabled = false</li>
 *   <li>Permet des déploiements flexibles selon l'architecture</li>
 * </ul>
 * 
 * <p><strong>Propriétés requises :</strong></p>
 * <ul>
 *   <li>ecclesiaflow.auth.module.base-url : URL de base du module d'authentification</li>
 * </ul>
 * 
 * <p><strong>Avantages WebClient :</strong></p>
 * <ul>
 *   <li>Client HTTP réactif non-bloquant</li>
 *   <li>Support natif de Spring Boot</li>
 *   <li>Configuration centralisée et réutilisable</li>
 *   <li>Gestion automatique des timeouts et retry</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MemberPasswordService
 */
@ConditionalOnProperty(name = "ecclesiaflow.auth.module.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
public class WebClientConfig {

    /**
     * Configure le client HTTP pour communiquer avec le module d'authentification.
     * <p>
     * Ce bean WebClient est configuré avec l'URL de base du module d'authentification
     * et peut être injecté dans les services qui ont besoin de communiquer avec
     * ce module (ex: définition de mots de passe, validation de tokens).
     * </p>
     * 
     * @param baseUrl l'URL de base du module d'authentification (depuis les propriétés)
     * @return client WebClient configuré pour le module d'authentification
     */
    @Bean
    public WebClient authWebClient(@Value("${ecclesiaflow.auth.module.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
