FROM debian:stretch-slim
ENV JAVA_HOME=/opt/java/jdk1.8.0_144
ENV PATH=${PATH}:${JAVA_HOME}/bin
WORKDIR /opt/proxy
COPY ./ /opt/proxy
EXPOSE 8080
EXPOSE 9000
ENTRYPOINT ["/opt/proxy/bin/start.sh"]