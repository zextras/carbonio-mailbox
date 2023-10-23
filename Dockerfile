FROM maven:3.8-openjdk-11-slim

WORKDIR /app/store

RUN keytool -genkey \
    -keystore /docker-keystore \
    -storepass docker-keystore-pwd \
    -keyalg RSA -keysize 2048 -validity 365000 \
    -dname "cn=Unknown, ou=Unknown, o=Unknown, c=Unknown"

CMD ["mvn", "jetty:run", "-Dmaven.compile.skip=true", "-Dmaven.test.skip=true", "-Dmaven.main.skip=true", "-Dmaven.antrun.skip=true", "-Dzimbra.config=../docker-jetty-localconfig.xml"]
