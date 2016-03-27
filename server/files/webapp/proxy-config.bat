@echo off
call checknew.bat
call setjava.bat
%JAVABIN% -Dapplication=proxyConfig -jar apploader.jar
