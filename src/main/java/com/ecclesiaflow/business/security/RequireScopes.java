package com.ecclesiaflow.business.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour spécifier les scopes requis pour accéder à une méthode.
 * <p>
 * Cette annotation est utilisée en combinaison avec {@link ScopeValidationAspect} 
 * pour valider automatiquement que l'utilisateur authentifié possède les permissions nécessaires.
 * </p>
 * 
 * <p><strong>Logique de validation :</strong></p>
 * <ul>
 *   <li>Si {@code requireAll = true} : L'utilisateur doit avoir TOUS les scopes listés (AND)</li>
 *   <li>Si {@code requireAll = false} : L'utilisateur doit avoir AU MOINS UN scope (OR - par défaut)</li>
 * </ul>
 * 
 * <p><strong>Exemples d'utilisation :</strong></p>
 * <pre>
 * // Requiert AU MOINS UN des scopes (OR - comportement par défaut)
 * {@code @RequireScopes({"ef:members:read:own", "ef:members:read:all"})}
 * public ResponseEntity&lt;Member&gt; getMember(UUID id) { ... }
 * 
 * // Requiert TOUS les scopes (AND)
 * {@code @RequireScopes(value = {"ef:members:write:all", "ef:admin"}, requireAll = true)}
 * public ResponseEntity&lt;Void&gt; deleteAllMembers() { ... }
 * </pre>
 * 
 * <p><strong>Scopes EcclesiaFlow :</strong></p>
 * <ul>
 *   <li>{@code ef:members:read:own} - Lecture de ses propres données</li>
 *   <li>{@code ef:members:read:all} - Lecture de toutes les données membres</li>
 *   <li>{@code ef:members:write:own} - Modification de ses propres données</li>
 *   <li>{@code ef:members:write:all} - Modification de toutes les données membres</li>
 *   <li>{@code ef:members:delete:own} - Suppression de son propre compte</li>
 *   <li>{@code ef:members:delete:all} - Suppression de tous les comptes</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see ScopeValidationAspect
 * @see ScopeValidator
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireScopes {
    
    /**
     * Liste des scopes requis pour accéder à la méthode.
     * 
     * @return tableau de scopes (ex: {"ef:members:read:own", "ef:members:read:all"})
     */
    String[] value();
    
    /**
     * Indique si TOUS les scopes sont requis (AND) ou si AU MOINS UN suffit (OR).
     * 
     * @return {@code true} pour exiger tous les scopes (AND), {@code false} pour au moins un (OR - défaut)
     */
    boolean requireAll() default false;
}
