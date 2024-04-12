package com.babylonradio.notification_service.mapper;

import com.babylonradio.notification_service.model.Receiver;
import com.babylonradio.notification_service.service.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.InjectionStrategy.FIELD;

@Mapper(injectionStrategy = FIELD, componentModel = "spring")
public abstract class ReceiverMapper {

    @Autowired
    public UserService receiverService;

    @Mapping(target = "username", expression = "java(receiverService.fetchUsername(userUID))")
    @Mapping(target = "fcmToken", expression = "java(receiverService.fetchFcmToken(userUID))")
    public abstract Receiver toReceiver(String userUID);
}
