package com.lostandfound.service;

import com.lostandfound.dto.request.MessageRequest;
import com.lostandfound.dto.response.ApiResponse;
import com.lostandfound.dto.response.MessageResponse;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.exception.UnauthorizedException;
import com.lostandfound.model.Item;
import com.lostandfound.model.Message;
import com.lostandfound.model.User;
import com.lostandfound.repository.ItemRepository;
import com.lostandfound.repository.MessageRepository;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public ApiResponse sendMessage(MessageRequest request, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in to send a message");
        }

        User sender = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver user", "id", request.getReceiverId()));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", request.getItemId()));

        // Validate sender is not sending to themselves
        if (sender.getId().equals(receiver.getId())) {
            throw new BadRequestException("You cannot send a message to yourself");
        }

        // Validate message content
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new BadRequestException("Message content cannot be empty");
        }

        if (request.getMessage().trim().length() > 1000) {
            throw new BadRequestException("Message content cannot exceed 1000 characters");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setItem(item);
        message.setMessage(request.getMessage().trim());

        messageRepository.save(message);
        logger.info("User {} sent message to user {} about item {}",
                sender.getEmail(), receiver.getEmail(), item.getId());

        return ApiResponse.builder()
                .success(true)
                .message("Message sent successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getUserMessages(UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in to view messages");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        List<Message> messages = messageRepository.findByReceiverOrderBySentAtDesc(user);
        return messages.stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getSentMessages(UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in to view sent messages");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        List<Message> messages = messageRepository.findBySenderOrderBySentAtDesc(user);
        return messages.stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMessage(Long messageId, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in to delete a message");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Check if user is admin, sender, or receiver
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isSender = message.getSender().getId().equals(currentUser.getId());
        boolean isReceiver = message.getReceiver().getId().equals(currentUser.getId());

        if (!isAdmin && !isSender && !isReceiver) {
            throw new UnauthorizedException("You don't have permission to delete this message");
        }

        messageRepository.delete(message);
        logger.info("User {} deleted message {}", user.getEmail(), messageId);
    }

    private MessageResponse mapToMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName())
                .receiverId(message.getReceiver().getId())
                .itemId(message.getItem().getId())
                .itemName(message.getItem().getName())
                .message(message.getMessage())
                .sentAt(message.getSentAt())
                .build();
    }
}