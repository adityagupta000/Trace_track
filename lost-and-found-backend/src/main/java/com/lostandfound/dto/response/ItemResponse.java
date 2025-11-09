package com.lostandfound.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String status;
    private String image;
    private Long createdBy;
    private String creatorName;
    private LocalDateTime createdAt;
}