package com.babylonradio.notification_service.utils;

public class TopicUtils {
    public static String mapTopic(String topic) {
        return topic.replace(" ", "_");
    }
}
