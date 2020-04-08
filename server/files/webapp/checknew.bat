if not exist jre.new goto cont1

rmdir /s /q jre
ren jre.new jre

:cont1

if not exist apploader.jar.new goto cont2

del apploader.jar
ren apploader.jar.new apploader.jar

:cont2
