FROM centos:centos8

ENV SCALA_VERSION 2.11.8
ENV SBT_VERSION 0.13.13

RUN cd /etc/yum.repos.d/
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*

RUN yum install -y epel-release
RUN yum update -y && yum install -y wget

# Install Java8 
RUN yum install -y java-11-openjdk

# Install Scala and SBT
RUN yum install -y https://downloads.lightbend.com/scala/2.11.8/scala-2.11.8.rpm
RUN rm -f /etc/yum.repos.d/bintray-rpm.repo || true
RUN curl -L https://www.scala-sbt.org/sbt-rpm.repo > sbt-rpm.repo
RUN mv sbt-rpm.repo /etc/yum.repos.d/
RUN yum install -y sbt

COPY . /dixit
WORKDIR /dixit

# Exposing port 80
EXPOSE 8083

RUN sbt compile
CMD sbt runServer -Dsbt.rootdir=true