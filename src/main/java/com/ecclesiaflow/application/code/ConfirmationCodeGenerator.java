package com.ecclesiaflow.application.code;

import com.ecclesiaflow.business.domain.communication.CodeGenerator;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Service spécialisé dans la génération de codes de confirmation EcclesiaFlow.
 * <p>
 * Cette classe respecte le principe SRP en se concentrant uniquement sur
 * la génération de codes de confirmation aléatoires et formatés. Extrait
 * la logique de génération de codes du service principal pour améliorer
 * la séparation des responsabilités et la testabilité.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Service utilitaire - Génération de codes</p>
 * 
 * <p><strong>Responsabilité unique :</strong></p>
 * <ul>
 *   <li>Génération de codes de confirmation aléatoires à 6 chiffres</li>
 *   <li>Formatage cohérent des codes avec zéros de tête</li>
 *   <li>Algorithme de génération centralisé et réutilisable</li>
 * </ul>
 * 
 * <p><strong>Avantages SRP :</strong></p>
 * <ul>
 *   <li>Testabilité isolée de la logique de génération</li>
 *   <li>Réutilisabilité dans d'autres contextes</li>
 *   <li>Évolution indépendante de l'algorithme de génération</li>
 *   <li>Séparation claire des préoccupations</li>
 * </ul>
 * 
 * <p><strong>Format des codes :</strong> 6 chiffres avec zéros de tête (ex: "012345", "987654")</p>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, codes uniformément distribués,
 * format cohérent, performance optimale.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Service
public class ConfirmationCodeGenerator implements CodeGenerator {

    private final Random random = new Random();

    /**
     * Génère un code de confirmation à 6 chiffres aléatoire.
     * <p>
     * Utilise {@link Random} pour générer un entier entre 0 et 999999,
     * puis le formate en chaîne de 6 caractères avec zéros de tête si nécessaire.
     * Garantit un format cohérent pour tous les codes générés.
     * </p>
     * 
     * @return un code de confirmation de 6 chiffres (ex: "012345", "987654")
     * 
     * @implNote Utilise String.format("%06d") pour garantir 6 caractères avec zéros de tête.
     *           La plage [0, 999999] assure une distribution uniforme des codes.
     */
    public String generateCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
}
