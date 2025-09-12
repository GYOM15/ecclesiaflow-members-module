package com.ecclesiaflow.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO représentant une réponse paginée contenant une liste de membres.
 * Utilise SignUpResponse pour représenter chaque membre dans la liste.
 */
@Getter
@Builder
public class MemberPageResponse {
    
    /**
     * Liste des membres pour la page courante
     */
    private final List<SignUpResponse> content;
    
    /**
     * Numéro de la page courante (commence à 0)
     */
    private final int page;
    
    /**
     * Alias pour page (compatibilité Spring Data)
     */
    private final int number;
    
    /**
     * Taille de la page (nombre d'éléments par page)
     */
    private final int size;
    
    /**
     * Nombre total d'éléments disponibles
     */
    private final long totalElements;
    
    /**
     * Nombre total de pages disponibles
     */
    private final int totalPages;
    
    /**
     * Indique s'il s'agit de la première page
     */
    private final boolean first;
    
    /**
     * Indique s'il s'agit de la dernière page
     */
    private final boolean last;
    
    /**
     * Nombre d'éléments dans la page courante
     */
    private final int numberOfElements;
    
    /**
     * Indique si la page est vide
     */
    private final boolean empty;
}
