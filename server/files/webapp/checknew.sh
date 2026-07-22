#!/usr/bin/env sh

if [ -d client.new ]; then
  . ./client.new/client.update.sh
fi

if [ -f apploader.jar.new ]; then
  rm -f apploader.jar
  mv -f apploader.jar.new apploader.jar
fi

if [ -f options.sh ]; then
  . ./options.sh
fi
