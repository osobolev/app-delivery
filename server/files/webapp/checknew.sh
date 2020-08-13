#!/usr/bin/env sh

if [ -d jre.new ]; then
  rm -rf jre
  mv -f jre.new jre
fi

if [ -f apploader.jar.new ]; then
  rm -f apploader.jar
  mv -f apploader.jar.new apploader.jar
fi
