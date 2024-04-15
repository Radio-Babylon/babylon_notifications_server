package com.babylonradio.notification_service.mapper;

import com.babylonradio.notification_service.model.Notification;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.InjectionStrategy.FIELD;

@Mapper(injectionStrategy = FIELD, componentModel = "spring")
public abstract class NotificationMapper {

    @Autowired
    public ReceiverMapper receiverMapper;

    @Autowired
    public SenderMapper senderMapper;

    @Autowired
    public MetadataMapper metadataMapper;

    @Mapping(target = "id", expression = "java(queryDocumentSnapshot.getId())")
    @Mapping(target = "notificationType", expression = "java(java.util.Arrays.stream(com.babylonradio.notification_service.model.NotificationType.values()).map(com.babylonradio.notification_service.model.NotificationType::toString).toList().contains(queryDocumentSnapshot.getData().get(\"type\")) ? com.babylonradio.notification_service.model.NotificationType.valueOf(queryDocumentSnapshot.getData().get(\"type\").toString()) : com.babylonradio.notification_service.model.NotificationType.NOT_RECOGNIZED)")
    @Mapping(target = "receiver", expression = "java(receiverMapper.toReceiver(queryDocumentSnapshot.getData().get(\"receiver\").toString()))")
    @Mapping(target = "sender", expression = "java(senderMapper.toSender(queryDocumentSnapshot.getData().get(\"sender\").toString()))")
    @Mapping(target = "metadata", expression = "java(metadataMapper.toMetadata(queryDocumentSnapshot.getData().get(\"sender\").toString()))")
    public abstract Notification toNotification(QueryDocumentSnapshot queryDocumentSnapshot);

    @Mapping(target = "notificationType", expression = "java(isGroupChat ? com.babylonradio.notification_service.model.NotificationType.GROUPCHAT_MESSAGE : com.babylonradio.notification_service.model.NotificationType.CHAT_MESSAGE)")
    @Mapping(target = "metadata", expression = "java(metadataMapper.toMetadata(documentSnapshot))")
    public abstract Notification toNotification(DocumentSnapshot documentSnapshot, boolean isGroupChat);
}
