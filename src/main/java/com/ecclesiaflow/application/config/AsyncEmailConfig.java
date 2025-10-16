package com.ecclesiaflow.application.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration dédiée pour l'exécution asynchrone des envois d'emails.
 * <p>
 * Cette configuration crée un thread pool dédié spécifiquement pour les
 * opérations d'envoi d'emails, isolant ainsi ces opérations potentiellement
 * lentes du pool de threads principal de l'application.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Configuration - Async Email Processing</p>
 * 
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Créer un thread pool dédié pour les emails</li>
 *   <li>Configurer la politique de rejection (CallerRunsPolicy)</li>
 *   <li>Gérer les exceptions non capturées dans les tâches async</li>
 * </ul>
 * 
 * <p><strong>Configuration du pool :</strong></p>
 * <ul>
 *   <li>Core threads: 2 (toujours actifs)</li>
 *   <li>Max threads: 5 (peut scaler jusqu'à 5 threads)</li>
 *   <li>Queue capacity: 100 (file d'attente si tous threads occupés)</li>
 *   <li>Rejection policy: CallerRunsPolicy (fallback sur thread appelant)</li>
 * </ul>
 * 
 * <p><strong>⚠️ Production :</strong> Ajuster les valeurs selon la charge attendue.
 * Pour un service d'emailing dédié avec outbox pattern, cette config sera remplacée.</p>
 * 
 * <p><strong>Note :</strong> Le logging est géré par {@code AsyncEmailLoggingAspect}
 * dans la couche application/logging/aspect pour respecter la séparation des concerns.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.application.logging.aspect.AsyncEmailLoggingAspect
 */
@Configuration
@EnableAsync
public class AsyncEmailConfig implements AsyncConfigurer {
    
    /**
     * Crée un executor dédié pour les opérations d'envoi d'emails asynchrones.
     * <p>
     * Ce thread pool est isolé du pool principal pour éviter que des envois
     * d'emails lents ne bloquent d'autres opérations critiques de l'application.
     * </p>
     * 
     * <p><strong>Politique de rejection :</strong> CallerRunsPolicy garantit
     * qu'aucune tâche n'est perdue en cas de saturation. La tâche sera exécutée
     * dans le thread appelant comme fallback.</p>
     * 
     * @return l'executor configuré pour les emails
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Taille du pool
        executor.setCorePoolSize(2);         // Min threads toujours actifs
        executor.setMaxPoolSize(5);          // Max threads en pic de charge
        executor.setQueueCapacity(100);      // Taille de la queue si tous threads occupés
        
        // Configuration avancée
        executor.setThreadNamePrefix("email-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Politique de rejection: exécuter dans le thread appelant si queue pleine
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        
        return executor;
    }
    
    /**
     * Configure le gestionnaire d'exceptions pour les tâches asynchrones.
     * <p>
     * Les exceptions lancées dans les méthodes @Async ne remontent pas
     * naturellement au thread appelant. Ce handler les rethrow pour être
     * capturées par {@code AsyncEmailLoggingAspect} via AOP.
     * </p>
     * 
     * @return le gestionnaire d'exceptions configuré
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            // Rethrow pour permettre l'interception par l'aspect AOP
            // Le logging est géré par AsyncEmailLoggingAspect
            throw new AsyncEmailException(
                "Async email task failed: " + method.getName(), 
                throwable
            );
        };
    }
    
    /**
     * Exception personnalisée pour les tâches d'email asynchrones.
     * Permet l'interception spécifique par l'aspect de logging.
     */
    public static class AsyncEmailException extends RuntimeException {
        public AsyncEmailException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
