package com.babylonradio.notification_service.mapper;

import com.babylonradio.notification_service.model.Receiver;
import com.babylonradio.notification_service.model.Sender;
import com.babylonradio.notification_service.service.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.InjectionStrategy.FIELD;

@Mapper(injectionStrategy = FIELD, componentModel = "spring")
public abstract class SenderMapper {
    @Autowired
    public UserService senderService;

    @Mapping(target = "username", expression = "java(senderService.fetchUsername(userUID))")
    public abstract Sender toSender(String userUID);
}
