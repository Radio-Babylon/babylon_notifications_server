package com.babylonradio.notification_service.mapper;

import com.babylonradio.notification_service.model.Notification;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.mapstruct.Mapper;
import org.springframework.web.bind.annotation.Mapping;

import static org.mapstruct.InjectionStrategy.FIELD;

@Mapper(injectionStrategy = FIELD, componentModel = "spring")
public abstract class NotificationMapper {

//    @Mapping(target = "id", expression = "java(queryDocumentSnapshot.getId())")
    public abstract Notification toNotification(QueryDocumentSnapshot queryDocumentSnapshot);
}
