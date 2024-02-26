FROM maven:3.8.4-openjdk-17 as build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN mvn package

FROM openjdk:17
COPY --from=build /usr/app/target/nginx-demo-jar-with-dependencies.jar /app/runner.jar
COPY keep_running.sh /keep_running.sh
RUN chmod +x /keep_running.sh
RUN mkdir -p /output
WORKDIR /

ENTRYPOINT ["/keep_running.sh" ]