FROM openjdk:8
ARG NACOS_IP
ARG HOST_IP
ENV CHATSPACE_CLOUD_HOME=/project \
    HOST_IP=$HOST_IP \
    NACOS_IP=$NACOS_IP
WORKDIR $CHATSPACE_CLOUD_HOME
COPY ./chatspace-session-1.0.jar ./chatspace-session-1.0.jar
EXPOSE 19015
ENTRYPOINT ["java", "-jar", "chatspace-session-1.0.jar"]