#!/bin/bash

mvn_install() {
  file=$1
  groupId=$2
  artifactId=$3
  version=$4

  mvn install:install-file -Dfile=$file -Dpackaging=jar -DgroupId=$groupId -DartifactId=$artifactId -Dversion=$version
}

mvn_install gifAnimation.jar \
            extrapixel gifAnimation d734273

mvn_install leapmotion-1.0.9.8391/LeapJava.jar \
            leapmotion LeapJava 1.0.9.8391

mvn_install processing-2.1.1/core/core.jar \
            org.processing core 2.1.1

mvn_install processing-2.1.1/serial/serial.jar \
            org.processing.serial serial 2.1.1

mvn_install processing-2.1.1/video/gstreamer-java.jar \
            org.processing.video gstreamer-java 2.1.1
mvn_install processing-2.1.1/video/jna.jar \
            org.processing.video jna 2.1.1
mvn_install processing-2.1.1/video/video.jar \
            org.processing.video video 2.1.1

