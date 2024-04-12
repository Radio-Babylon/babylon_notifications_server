package com.babylonradio.notification_service.mapper;

import com.babylonradio.notification_service.model.Metadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;

@Mapper(injectionStrategy = CONSTRUCTOR, componentModel = "spring")
public abstract class MetadataMapper {

//    @Mapping(target = "", expression = "java(com.babylonradio.notification_service.service.MetadataService.extractMetadata(metadata))")
//    public abstract Map<String, String> toMap(Metadata metadata);
}
