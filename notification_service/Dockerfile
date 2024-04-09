FROM openjdk:17
VOLUME /tmp
ADD target/notification_service.jar notification_service.jar
ENTRYPOINT ["java","-jar","/notification_service.jar"]