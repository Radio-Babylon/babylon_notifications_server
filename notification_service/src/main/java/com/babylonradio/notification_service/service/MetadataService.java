package com.babylonradio.notification_service.service;

import com.babylonradio.notification_service.model.Metadata;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Service
public class MetadataService {
    public static Map<String, String> extractMetadata(Metadata metadata) {
        Map<String, String> metaDataExtracted = new HashMap<>();
        for(Field field: metadata.getClass().getFields()) {
            metaDataExtracted.put(field.getName(), ""); // TODO: extract proper values
        }
        return metaDataExtracted;
    }
}
