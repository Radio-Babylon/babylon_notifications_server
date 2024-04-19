package com.babylonradio.notification_service.publicnotification.model;

import com.babylonradio.notification_service.publicnotification.enums.SubscriptionType;
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
