FROM openjdk:8
ARG NACOS_IP
ARG HOST_IP
ENV CHATSPACE_CLOUD_HOME=/project \
    HOST_IP=$HOST_IP \
    NACOS_IP=$NACOS_IP \
    SERVER_ID=$SERVER_ID
COPY ./chatspace-realtime-1.0.jar ./chatspace-realtime-1.0.jar
EXPOSE 19018
EXPOSE 19019
ENTRYPOINT ["java", "-jar", "chatspace-realtime-1.0.jar"]