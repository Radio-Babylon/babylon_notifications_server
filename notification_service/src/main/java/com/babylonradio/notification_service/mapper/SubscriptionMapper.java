package com.babylonradio.notification_service.mapper;

import com.babylonradio.notification_service.publicnotification.model.Subscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(injectionStrategy = InjectionStrategy.FIELD, componentModel = "spring")
public abstract class SubscriptionMapper {

    @Autowired
    public ReceiverMapper receiverMapper;

    public ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(target = "id", expression = "java(querySnapshot.getId())")
    @Mapping(target = "subscriptionType", expression = "java(com.babylonradio.notification_service.publicnotification.enums.SubscriptionType.valueOf(querySnapshot.getData().get(\"type\").toString()))")
    @Mapping(target = "topic", expression = "java(com.babylonradio.notification_service.publicnotification.utils.TopicUtils.mapTopic(querySnapshot.getData().get(\"topic\").toString()))")
    @Mapping(target = "users", expression = "java(objectMapper.convertValue(querySnapshot.getData().get(\"users\"), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {}).stream().map(receiverMapper::toReceiver).toList())")
    public abstract Subscription toSubscription(QueryDocumentSnapshot querySnapshot);
}
