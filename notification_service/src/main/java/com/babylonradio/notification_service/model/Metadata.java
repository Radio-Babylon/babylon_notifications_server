package com.babylonradio.notification_service.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Metadata {
    private ArrayList<String> photoURLs;
    private ArrayList<String> messages;
    private String topic;
    private ArrayList<String> redirectLinks;
}
