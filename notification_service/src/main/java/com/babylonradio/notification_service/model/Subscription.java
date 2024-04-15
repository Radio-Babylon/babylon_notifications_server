package com.babylonradio.notification_service.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class Subscription {
    private String id;
    private SubscriptionType subscriptionType;
    private String topic;
    private List<Receiver> users;
}
