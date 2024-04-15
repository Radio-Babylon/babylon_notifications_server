package com.babylonradio.notification_service.model;

import lombok.Data;

import java.util.List;

@Data
public class Metadata {
    private List<String> picturesURLs;
    private List<String> messages;
    private String topic;
    private List<String> redirectLinks;
}
