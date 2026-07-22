if not exist client.new goto cont1

call client.new\client.update.bat 2>nul

:cont1

if not exist apploader.jar.new goto cont2

del apploader.jar
ren apploader.jar.new apploader.jar

:cont2

if exist options.bat call options.bat
