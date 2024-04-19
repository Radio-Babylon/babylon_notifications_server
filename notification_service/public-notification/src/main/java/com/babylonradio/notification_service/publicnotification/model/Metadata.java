package com.babylonradio.notification_service.publicnotification.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Getter
@Setter
@Builder
public class Metadata {
    private List<String> picturesURLs;
    private List<String> messages;
    private String topic;
    private List<String> redirectLinks;
    private OffsetDateTime lastEventTimestamp;
}
