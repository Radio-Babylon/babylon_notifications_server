package com.babylonradio.notification_service.publicnotification.utils;

public class TopicUtils {
    public static String mapTopic(Object topic) {
        if(topic == null) {
            return "";
        }
        return topic.toString().replace(" ", "_");
    }
}
