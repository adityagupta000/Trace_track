package com.lostandfound.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lostandfound.model.Message;
import com.lostandfound.model.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReceiverOrderBySentAtDesc(User receiver);
    List<Message> findBySenderOrderBySentAtDesc(User sender);
    List<Message> findAllByOrderBySentAtDesc();
}