FROM openjdk:17
VOLUME /tmp
ADD target/notification_batches.jar notification_batches.jar
ENTRYPOINT ["java","-jar","/notification_batches.jar"]