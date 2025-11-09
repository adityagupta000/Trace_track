package com.lostandfound.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lostandfound.model.Claim;
import com.lostandfound.model.Item;
import com.lostandfound.model.User;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    
    List<Claim> findByClaimedByOrderByClaimedAtDesc(User user);
    
    Optional<Claim> findByItemAndClaimedBy(Item item, User user);
    
    boolean existsByItemAndClaimedBy(Item item, User user);
    
    @Query("SELECT c FROM Claim c WHERE c.claimedAt < :cutoffDate")
    List<Claim> findOldClaims(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    List<Claim> findByItem(Item item);
    
    List<Claim> findAllByOrderByClaimedAtDesc();
}