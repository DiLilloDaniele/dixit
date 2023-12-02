# syntax=docker/dockerfile:1
FROM python:3.7-alpine

ENV SCALA_VERSION 3.2.0
ENV SBT_VERSION 0.13.17

RUN yum install -y epel-release
RUN yum update -y && yum install -y wget

# INSTALL JAVA
RUN yum install -y java-11-openjdk

# INSTALL SBT
RUN wget http://dl.bintray.com/sbt/rpm/sbt-${SBT_VERSION}.rpm
RUN yum install -y sbt-${SBT_VERSION}.rpm

WORKDIR .
EXPOSE 8083
COPY . .
RUN sbt compile
CMD sbt runServer