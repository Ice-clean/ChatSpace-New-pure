FROM openjdk:8
ARG NACOS_IP
ARG HOST_IP
ENV CHATSPACE_CLOUD_HOME=/project \
    HOST_IP=$HOST_IP \
    NACOS_IP=$NACOS_IP
WORKDIR $CHATSPACE_CLOUD_HOME
COPY ./chatspace-gateway-1.0.jar ./chatspace-gateway-1.0.jar
EXPOSE 19010
ENTRYPOINT ["java", "-jar", "chatspace-gateway-1.0.jar"]