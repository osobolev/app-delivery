
[Setup]
OutputDir=..
SourceDir=.
OutputBaseFilename="install"

DefaultDirName={sd}\{#apDir}
DefaultGroupName={#apName}

ShowLanguageDialog=no

AppName={#apName}
#ifdef appId
AppId={#appId}
#else
AppId={#apId}
#endif
AppVerName={#apName} {#apVersion}

UsePreviousSetupType=no
TimeStampRounding=0


[Languages]
Name: ru; MessagesFile: compiler:Languages\Russian.isl
Name: ru; MessagesFile: compiler:Default.isl


[Files]
Source: jre\*; DestDir: {app}\jre\; flags: recursesubdirs
Source: checknew.bat; DestDir: {app};
Source: "{#apId}-client.bat"; DestDir: {app};
Source: proxy-config.bat; DestDir: {app};
Source: setjava.bat; DestDir: {app};
Source: apploader.jar; DestDir: {app};
Source: apploader.properties; DestDir: {app};
Source: proxy.properties; DestDir: {app}; Flags: skipifsourcedoesntexist
Source: "{#apId}_splash.jpg"; DestDir: {app}; Flags: skipifsourcedoesntexist
Source: "{#apId}.ico"; DestDir: {app}; Flags: skipifsourcedoesntexist
Source: "uninst_{#apId}.ico"; DestDir: {app}; Flags: skipifsourcedoesntexist


[Tasks]
Name: desktopicon; Description: "Создать иконку запуска на рабочем столе"; GroupDescription: "Дополнительные иконки"


[Icons]
Name: "{group}\{#apName}"; Filename: "{app}\{#apId}-client.bat"; WorkingDir: "{app}"; IconFileName: "{app}\{#apId}.ico"
Name: "{commondesktop}\{#apName}"; Filename: "{app}\{#apId}-client.bat"; WorkingDir: "{app}"; IconFileName: "{app}\{#apId}.ico"; Tasks: desktopicon
Name: "{group}\Удаление {#apName}"; Filename: "{app}\unins000.exe"; WorkingDir: "{app}"; IconFileName: "{app}\uninst_{#apId}.ico"


[UninstallDelete]
Type: filesandordirs; Name: "{app}\jre";
Type: filesandordirs; Name: "{app}\distr";
Type: filesandordirs; Name: "{app}\lib";
Type: filesandordirs; Name: "{app}\help";
Type: filesandordirs; Name: "{app}\temp";
Type: files; Name: "{app}\*.bat";
Type: files; Name: "{app}\*.list";
Type: files; Name: "{app}\*.log";
Type: files; Name: "{app}\*.properties";
Type: files; Name: "{app}\*.zip";
Type: files; Name: "{app}\tzupdater.*";
Type: dirifempty; Name: "{app}";
