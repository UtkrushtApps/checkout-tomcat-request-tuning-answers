FROM tomcat:9.0-jdk11-temurin

# Remove default sample applications
RUN rm -rf /usr/local/tomcat/webapps/ROOT \
    /usr/local/tomcat/webapps/docs \
    /usr/local/tomcat/webapps/examples \
    /usr/local/tomcat/webapps/host-manager \
    /usr/local/tomcat/webapps/manager

# Copy the PostgreSQL JDBC driver built by Maven into Tomcat's shared lib
# so the JNDI datasource can load it at container startup.
COPY target/tomcat-lib/postgresql-42.6.0.jar /usr/local/tomcat/lib/postgresql-42.6.0.jar

# Copy Tomcat configuration files
COPY conf/server.xml /usr/local/tomcat/conf/server.xml
COPY conf/context.xml /usr/local/tomcat/conf/context.xml
COPY conf/logging.properties /usr/local/tomcat/conf/logging.properties

# Deploy the application WAR
COPY target/checkoutweb.war /usr/local/tomcat/webapps/checkoutweb.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
