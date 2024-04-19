package com.babylonradio.notification_service.publicnotification.utils;

import com.babylonradio.notification_service.publicnotification.model.Metadata;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Component
public class MetadataUtils {
    public static Map<String, String> extractMetadata(Metadata metadata) {
        Map<String, String> metaDataExtracted = new HashMap<>();
        for(Field field: metadata.getClass().getFields()) {
            metaDataExtracted.put(field.getName(), ""); // TODO: extract proper values
        }
        return metaDataExtracted;
    }
}
