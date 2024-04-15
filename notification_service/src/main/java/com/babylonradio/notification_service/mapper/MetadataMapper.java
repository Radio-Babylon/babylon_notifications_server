package com.babylonradio.notification_service.mapper;

import com.babylonradio.notification_service.model.Metadata;
import com.babylonradio.notification_service.service.MetadataService;
import com.google.cloud.firestore.DocumentSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.InjectionStrategy.FIELD;

@Mapper(injectionStrategy = FIELD, componentModel = "spring")
public abstract class MetadataMapper {

    @Autowired
    public MetadataService metadataService;

    @Mapping(target = "picturesURLs", expression = "java(metadataService.fetchPictures(userUID))")
    public abstract Metadata toMetadata(String userUID);

    @Mapping(target = "topic", expression = "java(documentSnapshot.get(\"chatName\").toString())")
    @Mapping(target = "picturesURLs", expression = "java(java.util.Arrays.asList(documentSnapshot.get(\"iconPath\").toString()))")
    @Mapping(target = "messages", expression = "java(java.util.Arrays.asList(documentSnapshot.get(\"lastMessage\").toString()))")
    public abstract Metadata toMetadata(DocumentSnapshot documentSnapshot);
}
