package com.ecclesiaflow.business.services.impl;


import com.ecclesiaflow.io.entities.Member;
import com.ecclesiaflow.io.repository.MemberRepository;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service de gestion des mots de passe des membres EcclesiaFlow.
 * <p>
 * Cette classe orchestre les opérations liées aux mots de passe des membres
 * en déléguant au module d'authentification via {@link AuthModuleService}.
 * Fournit également des utilitaires pour récupérer les informations des membres.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Service orchestrateur - Gestion des mots de passe</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Définition initiale du mot de passe avec token temporaire</li>
 *   <li>Récupération des informations membres pour les opérations de mot de passe</li>
 *   <li>Orchestration avec le module d'authentification</li>
 * </ul>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link AuthModuleService} - Communication avec le module d'authentification</li>
 *   <li>{@link MemberRepository} - Récupération des informations membres</li>
 * </ul>
 * 
 * <p><strong>Flux typique :</strong></p>
 * <ol>
 *   <li>Réception de la demande de définition de mot de passe</li>
 *   <li>Validation de l'existence du membre</li>
 *   <li>Délégation au module d'authentification</li>
 * </ol>
 * 
 * <p><strong>Note architecturale :</strong> Ce service fait partie du module membres
 * mais délègue toute la logique d'authentification au module spécialisé.</p>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, gestion d'erreurs, séparation des responsabilités.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see AuthModuleService
 * @see MemberRepository
 */
@Service
@RequiredArgsConstructor
public class MemberPasswordService {

    private final AuthModuleService authModuleService;
    private final MemberRepository memberRepository;

    /**
     * Définit le mot de passe initial d’un membre après validation du token temporaire.
     *
     * @param email           Email du membre
     * @param password        Nouveau mot de passe
     * @param temporaryToken  Token temporaire obtenu après confirmation
     */
    public void setPassword(String email, String password, String temporaryToken) {
        authModuleService.setPassword(email, password, temporaryToken);
    }

    public UUID getMemberIdByEmail(String email) throws MemberNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("Membre non trouvé pour l'email : " + email));
        return member.getId();
    }
}

