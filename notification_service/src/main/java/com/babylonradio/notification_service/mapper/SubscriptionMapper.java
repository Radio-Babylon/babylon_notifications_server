package com.babylonradio.notification_service.mapper;

import com.babylonradio.notification_service.model.Subscription;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.InjectionStrategy.FIELD;

@Mapper(injectionStrategy = FIELD, componentModel = "spring")
public abstract class SubscriptionMapper {

    @Autowired
    public ReceiverMapper receiverMapper;

    @Mapping(target = "id", expression = "java(querySnapshot.getId())")
    @Mapping(target = "subscriptionType", expression = "java(com.babylonradio.notification_service.model.SubscriptionType.valueOf(querySnapShot.getData().get(\"type\").toString()))")
    @Mapping(target = "topic", expression = "java(querySnapShot.getData().get(\"topic\").toString())")
    @Mapping(target = "users", expression = "java()")
    public abstract Subscription toSubscription(QueryDocumentSnapshot querySnapshot);
}
