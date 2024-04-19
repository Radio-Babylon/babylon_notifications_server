package com.babylonradio.notification_service.mapper;

import com.babylonradio.notification_service.publicnotification.model.Metadata;
import com.babylonradio.notification_service.service.MetadataService;
import com.google.cloud.firestore.DocumentSnapshot;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(injectionStrategy = InjectionStrategy.FIELD, componentModel = "spring")
public abstract class MetadataMapper {

    @Autowired
    public MetadataService metadataService;

    @Mapping(target = "picturesURLs", expression = "java(metadataService.fetchPictures(userUID))")
    public abstract Metadata toMetadata(String userUID);

    @Mapping(target = "topic", expression = "java(documentSnapshot.get(\"chatName\").toString())")
    @Mapping(target = "picturesURLs", expression = "java(java.util.Arrays.asList(documentSnapshot.get(\"iconPath\").toString()))")
    @Mapping(target = "messages", expression = "java(java.util.Arrays.asList(documentSnapshot.get(\"lastMessage\").toString()))")
    @Mapping(target = "lastEventTimestamp", expression = "java(com.babylonradio.notification_service.publicnotification.utils.TimeUtils.formatOffsetDateTime(documentSnapshot.get(\"lastMessageTime\").toString()))")
    public abstract Metadata toMetadata(DocumentSnapshot documentSnapshot);
}
