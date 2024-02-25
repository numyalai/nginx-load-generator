
FROM maven:3.8.4-openjdk-17 as build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN mvn package

FROM openjdk:17
COPY --from=build /usr/app/target/nginx-demo-jar-with-dependencies.jar /app/runner.jar
WORKDIR /output
ENTRYPOINT ["java", "-Xmx1964m","-jar", "/app/runner.jar"]
CMD ["tail", "-f", "/dev/null"]
