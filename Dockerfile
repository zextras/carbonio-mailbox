FROM maven:3.8-openjdk-11-slim

WORKDIR /app/store

RUN apt update && apt install -y build-essential git

CMD ["mvn", "jetty:run", "-Dmaven.compile.skip=true", "-Dzimbra.config=../docker-jetty-localconfig.xml"]
