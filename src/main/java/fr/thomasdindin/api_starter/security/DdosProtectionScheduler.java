package fr.thomasdindin.api_starter.security;

import fr.thomasdindin.api_starter.services.BlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DdosProtectionScheduler {

    private final BlacklistService ipBlockingService;

    public DdosProtectionScheduler(@Autowired BlacklistService ipBlockingService) {
        this.ipBlockingService = ipBlockingService;
    }

    @Scheduled(fixedRate = 60000) // Exécution toutes les minutes
    public void monitorAndBlockIps() {
        ipBlockingService.analyzeAndBlockSuspectIps(100, 1); // Par exemple, bloquer les IP avec plus de 100 requêtes en 1 minute
    }
}
