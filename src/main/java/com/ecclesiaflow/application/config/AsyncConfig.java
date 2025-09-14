package com.ecclesiaflow.application.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration Spring pour l'exécution asynchrone des tâches.
 * <p>
 * Cette classe configure l'exécution asynchrone pour les opérations d'envoi d'email
 * et autres tâches longues qui ne doivent pas bloquer le thread principal.
 * Implémente {@link AsyncConfigurer} pour personnaliser le comportement asynchrone.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Configuration - Exécution asynchrone</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Configuration du pool de threads pour les tâches asynchrones</li>
 *   <li>Gestion des exceptions non capturées dans les méthodes @Async</li>
 *   <li>Optimisation des performances pour les opérations I/O (email)</li>
 * </ul>
 * 
 * <p><strong>Configuration du pool de threads :</strong></p>
 * <ul>
 *   <li>Core Pool Size: 2 threads minimum</li>
 *   <li>Max Pool Size: 10 threads maximum</li>
 *   <li>Queue Capacity: 100 tâches en attente</li>
 *   <li>Keep Alive: 60 secondes pour les threads inactifs</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation :</strong></p>
 * <ul>
 *   <li>Envoi d'emails de confirmation asynchrone</li>
 *   <li>Envoi d'emails de bienvenue asynchrone</li>
 *   <li>Opérations de notification non bloquantes</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see AsyncConfigurer
 * @see ThreadPoolTaskExecutor
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Configure l'exécuteur de tâches asynchrones.
     * <p>
     * Crée un pool de threads optimisé pour les opérations I/O comme l'envoi d'emails.
     * La configuration est adaptée pour gérer des pics de charge tout en conservant
     * des ressources raisonnables.
     * </p>
     * 
     * @return l'exécuteur configuré pour les tâches asynchrones
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configuration du pool de threads
        executor.setCorePoolSize(2);           // Threads minimum
        executor.setMaxPoolSize(10);           // Threads maximum
        executor.setQueueCapacity(100);        // Tâches en attente
        executor.setKeepAliveSeconds(60);      // Durée de vie des threads inactifs
        
        // Nommage des threads pour le debugging
        executor.setThreadNamePrefix("EcclesiaFlow-Async-");
        
        // Politique de rejet : CallerRunsPolicy pour éviter la perte de tâches
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // Attendre la fin des tâches lors de l'arrêt
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        return executor;
    }

    /**
     * Configure le gestionnaire d'exceptions non capturées pour les méthodes @Async.
     * <p>
     * Les exceptions qui se produisent dans les méthodes asynchrones ne remontent pas
     * naturellement au thread appelant. Ce gestionnaire permet de les logger et
     * de prendre des actions appropriées.
     * </p>
     * 
     * @return le gestionnaire d'exceptions asynchrones
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * Gestionnaire personnalisé pour les exceptions asynchrones non capturées.
     * <p>
     * Implémente {@link AsyncUncaughtExceptionHandler} pour logger les erreurs
     * qui se produisent dans les méthodes @Async et qui ne sont pas gérées
     * explicitement.
     * </p>
     */
    private static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        @Override
        public void handleUncaughtException(Throwable throwable, java.lang.reflect.Method method, Object... params) {
            // Les logs sont gérés par l'aspect BusinessOperationLoggingAspect
            // Ici, on pourrait ajouter d'autres actions comme :
            // - Envoyer une alerte
            // - Incrémenter des métriques d'erreur
            // - Déclencher un mécanisme de retry
        }
    }
}
