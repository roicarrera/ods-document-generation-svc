FROM registry.access.redhat.com/rhel7

MAINTAINER metmajer@getcloudnative.io

WORKDIR /app
COPY app.jar ./app.jar

# Install wkhtmltopdf
ENV WKHTMLTOPDF_VERSION 0.12.5

RUN yum install -y xz xorg-x11-fonts-75dpi xorg-x11-fonts-Type1 openssl git-core fontconfig && \
    curl -kLO https://downloads.wkhtmltopdf.org/0.12/0.12.4/wkhtmltox-0.12.4_linux-generic-amd64.tar.xz && \
    unxz wkhtmltox-0.12.4_linux-generic-amd64.tar.xz && \
    tar xvf wkhtmltox-0.12.4_linux-generic-amd64.tar && \
    mv wkhtmltox/bin/wkhtmlto* /usr/bin

RUN yum -y install java-1.8.0-openjdk-devel.x86_64

# See https://docs.openshift.com/container-platform/3.9/creating_images/guidelines.html
RUN chgrp -R 0 /app && \
    chmod -R g=u /app

USER 1001

EXPOSE 8080

CMD ["java", "-Xmx512m", "-jar", "app.jar"]
