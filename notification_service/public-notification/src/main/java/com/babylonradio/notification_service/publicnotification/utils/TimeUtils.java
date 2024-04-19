package com.babylonradio.notification_service.publicnotification.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    public static OffsetDateTime formatOffsetDateTime(String timestamp) {
        return OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
