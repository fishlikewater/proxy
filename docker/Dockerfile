FROM debian:stretch-slim
ARG PORT=8090
ENV JAVA_HOME=/opt/java/jdk1.8.0_144
ENV PATH=${PATH}:${JAVA_HOME}/bin
WORKDIR /opt/proxy
COPY ./ /opt/proxy
EXPOSE ${PORT}
ENTRYPOINT ["/opt/proxy/bin/start.sh"]