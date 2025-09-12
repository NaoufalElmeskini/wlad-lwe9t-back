FROM openjdk:21-slim
COPY target/wladLwe9t-0.0.1-SNAPSHOT.jar wladLwe9t.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/wladLwe9t.jar"]