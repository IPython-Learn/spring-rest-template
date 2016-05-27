FROM java:8
VOLUME /tmp

ADD spring-rest-template.jar spring-rest-template.jar
RUN bash -c 'touch /spring-rest-template.jar'

EXPOSE 8080

ENTRYPOINT ["java", "-jar","spring-rest-template.jar"]