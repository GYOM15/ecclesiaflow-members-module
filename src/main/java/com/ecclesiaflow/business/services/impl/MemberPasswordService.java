package com.ecclesiaflow.business.services.impl;


import com.ecclesiaflow.business.services.AuthModuleService;
import com.ecclesiaflow.io.entities.Member;
import com.ecclesiaflow.io.repository.MemberRepository;
import com.ecclesiaflow.web.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

