#!/usr/bin/env sh
. ./checknew.sh
. ./setjava.sh
$JAVABIN -Dapplication=proxyConfig -jar apploader.jar
