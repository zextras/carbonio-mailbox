FROM maven:3.8-openjdk-11-slim

WORKDIR /app/store

CMD ["mvn", "jetty:run", "-Dmaven.compile.skip=true", "-Dmaven.test.skip=true", "-Dmaven.main.skip=true", "-Dmaven.antrun.skip=true", "-Dzimbra.config=../docker-jetty-localconfig.xml"]
