package com.babylonradio.notification_service.mapper;

import com.babylonradio.notification_service.publicnotification.model.Receiver;
import com.babylonradio.notification_service.service.UserService;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(injectionStrategy = InjectionStrategy.FIELD, componentModel = "spring")
public abstract class ReceiverMapper {

    @Autowired
    public UserService receiverService;

    @Mapping(target = "username", expression = "java(receiverService.fetchUsername(userUID))")
    @Mapping(target = "fcmToken", expression = "java(receiverService.fetchFCMToken(userUID))")
    public abstract Receiver toReceiver(String userUID);
}
