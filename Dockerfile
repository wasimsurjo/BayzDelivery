FROM gradle:7.3.3-jdk17 as gradlebuilder-clean
RUN mkdir /project
COPY . /project
WORKDIR /project
RUN ./gradlew bootJar -DskipTests


FROM azul/zulu-openjdk-alpine:17-jre
RUN mkdir /app
COPY --from=gradlebuilder-clean /project/build/libs//bayzdelivery-0.0.1-SNAPSHOT.jar /app/bayzdelivery-0.0.1-SNAPSHOT.jar
WORKDIR /app
CMD ["java", "-jar", "bayzdelivery-0.0.1-SNAPSHOT.jar"]
