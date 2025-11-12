package com.lostandfound.service;

import com.lostandfound.dto.request.ItemRequest;
import com.lostandfound.dto.response.ItemResponse;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.exception.UnauthorizedException;
import com.lostandfound.model.Item;
import com.lostandfound.model.User;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.ItemRepository;
import com.lostandfound.repository.MessageRepository;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ClaimRepository claimRepository;
    private final MessageRepository messageRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ItemResponse createItem(ItemRequest request, MultipartFile image, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in to create an item");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Validate status
        Item.Status status;
        try {
            status = Item.Status.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Must be LOST or FOUND");
        }

        // Only LOST or FOUND allowed for new items
        if (status == Item.Status.CLAIMED) {
            throw new BadRequestException("Cannot create item with CLAIMED status");
        }

        String imagePath = null;
        if (image != null && !image.isEmpty()) {
            imagePath = fileStorageService.storeFile(image);
        }

        Item item = new Item();
        item.setName(request.getName().trim());
        item.setDescription(request.getDescription().trim());
        item.setLocation(request.getLocation().trim());
        item.setStatus(status);
        item.setImage(imagePath);
        item.setCreatedBy(user);

        item = itemRepository.save(item);
        logger.info("User ID {} created new item: {}", user.getId(), item.getId());

        return mapToItemResponse(item);
    }

    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        return mapToItemResponse(item);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> searchItems(String search, String status) {
        Item.Status itemStatus = null;
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
            try {
                itemStatus = Item.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + status + ". Must be LOST, FOUND, or CLAIMED");
            }
        }

        // Sanitize search input to prevent wildcard injection
        String sanitizedSearch = sanitizeSearchInput(search);

        List<Item> items = itemRepository.searchItems(
                sanitizedSearch,
                itemStatus
        );

        return items.stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Sanitize search input to prevent SQL wildcard injection
     */
    private String sanitizeSearchInput(String search) {
        if (search == null || search.trim().isEmpty()) {
            return "";
        }

        // Remove or escape SQL wildcards
        String sanitized = search.trim()
                .replace("\\", "\\\\")  // Escape backslash first
                .replace("%", "\\%")     // Escape %
                .replace("_", "\\_");    // Escape _

        // Limit length to prevent DoS
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }

        return sanitized;
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getUserItems(UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in to view your items");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        List<Item> items = itemRepository.findByCreatedByOrderByCreatedAtDesc(user);
        return items.stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemResponse updateItem(Long itemId, ItemRequest request, MultipartFile image, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in to update an item");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Check permissions
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isOwner = item.getCreatedBy().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("You don't have permission to update this item");
        }

        // Validate status
        Item.Status newStatus;
        try {
            newStatus = Item.Status.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Must be LOST, FOUND, or CLAIMED");
        }

        // Don't allow changing to CLAIMED unless it's already claimed
        if (newStatus == Item.Status.CLAIMED && item.getStatus() != Item.Status.CLAIMED) {
            throw new BadRequestException("Cannot manually set item status to CLAIMED. Use the claim endpoint instead.");
        }

        // Update fields
        item.setName(request.getName().trim());
        item.setDescription(request.getDescription().trim());
        item.setLocation(request.getLocation().trim());
        item.setStatus(newStatus);

        // Update image if provided
        if (image != null && !image.isEmpty()) {
            // Delete old image
            if (item.getImage() != null) {
                fileStorageService.deleteFile(item.getImage());
            }
            String imagePath = fileStorageService.storeFile(image);
            item.setImage(imagePath);
        }

        item = itemRepository.save(item);
        logger.info("User ID {} updated item: {}", user.getId(), item.getId());

        return mapToItemResponse(item);
    }

    @Transactional
    public void deleteItem(Long itemId, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in to delete an item");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Check permissions
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isOwner = item.getCreatedBy().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("You don't have permission to delete this item");
        }

        // Delete related claims
        claimRepository.deleteAll(claimRepository.findByItem(item));

        // Delete related messages
        messageRepository.deleteAll(messageRepository.findAll().stream()
                .filter(m -> m.getItem().getId().equals(itemId))
                .collect(Collectors.toList()));

        // Delete image file if exists
        if (item.getImage() != null) {
            fileStorageService.deleteFile(item.getImage());
        }

        itemRepository.delete(item);
        logger.info("User ID {} deleted item: {}", user.getId(), itemId);
    }

    @Transactional
    public void deleteItemAsAdmin(Long itemId, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // Verify user is admin
        if (user.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only admins can perform this action");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        // Delete related claims
        claimRepository.deleteAll(claimRepository.findByItem(item));

        // Delete related messages
        messageRepository.deleteAll(messageRepository.findAll().stream()
                .filter(m -> m.getItem().getId().equals(itemId))
                .collect(Collectors.toList()));

        // Delete image file if exists
        if (item.getImage() != null) {
            fileStorageService.deleteFile(item.getImage());
        }

        itemRepository.delete(item);
        logger.info("Admin ID {} deleted item: {}", user.getId(), itemId);
    }

    private ItemResponse mapToItemResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .location(item.getLocation())
                .status(item.getStatus().name())
                .image(item.getImage())
                .createdBy(item.getCreatedBy().getId())
                .creatorName(item.getCreatedBy().getName())
                .createdAt(item.getCreatedAt())
                .build();
    }
}