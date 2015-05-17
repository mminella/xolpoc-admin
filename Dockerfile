#
# Dockerfile to build the Spring XD Admin image
#

FROM java:8
VOLUME /tmp
ADD build/libs/xolpoc-admin-0.0.1-SNAPSHOT.jar xd-admin.jar
RUN bash -c 'touch /xd-admin.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/xd-admin.jar"]
