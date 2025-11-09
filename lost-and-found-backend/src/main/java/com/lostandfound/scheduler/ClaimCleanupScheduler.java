package com.lostandfound.scheduler;

import com.lostandfound.model.Claim;
import com.lostandfound.model.Item;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ClaimCleanupScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(ClaimCleanupScheduler.class);
    
    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    
    @Scheduled(cron = "0 0 2 * * ?") // Run every day at 2 AM
    @Transactional
    public void cleanupOldClaims() {
        logger.info("Starting cleanup of old claims...");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            List<Claim> oldClaims = claimRepository.findOldClaims(cutoffDate);
            
            int claimsDeleted = oldClaims.size();
            
            for (Claim claim : oldClaims) {
                Item item = claim.getItem();
                claimRepository.delete(claim);
                
                // Update item status if it was claimed
                if (item.getStatus() == Item.Status.CLAIMED) {
                    item.setStatus(Item.Status.FOUND);
                    itemRepository.save(item);
                }
            }
            
            logger.info("Deleted {} old claims and updated item statuses", claimsDeleted);
        } catch (Exception e) {
            logger.error("Error during claim cleanup", e);
        }
    }
}