FROM maven:3.8-openjdk-11-slim

WORKDIR /app

RUN apt update && apt install -y build-essential git

CMD ["mvn", "jetty:run"]