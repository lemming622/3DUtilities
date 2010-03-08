[Setup]
AppName=NCSA 3D File Utilities
AppVerName=NCSA 3D File Utilities 0.1
AppPublisher=NCSA
AppPublisherURL=http://isda.ncsa.uiuc.edu
AppSupportURL=http://isda.ncsa.uiuc.edu
AppUpdatesURL=http://isda.ncsa.uiuc.edu
UsePreviousAppDir=no
DefaultDirName={userdesktop}\3DFu
DefaultGroupName=3DFu
Compression=lzma
SolidCompression=yes
OutputDir=build
OutputBaseFilename=3DFu

[Files]
Source: "build\3DUtilities.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "build\ModelViewer_Lite.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "ModelBrowser_cwd.ini.txt"; DestDir: "{app}"; DestName: "ModelBrowser.ini"; Flags: ignoreversion
Source: "ModelViewer_cwd.ini.txt"; DestDir: "{app}"; DestName: "ModelViewer.ini"; Flags: ignoreversion
Source: "ModelBrowser_cwd.bat.txt"; DestDir: "{app}"; DestName: "ModelBrowser.bat"; Flags: ignoreversion
Source: "ModelConvert_cwd.bat.txt"; DestDir: "{app}"; DestName: "ModelConvert.bat"; Flags: ignoreversion
Source: "polyglotDomain_OBJ.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "ModelViewer.php"; DestDir: "{app}"; Flags: ignoreversion
Source: "logos\*"; DestDir: "{app}\logos"; Flags: ignoreversion recursesubdirs
Source: "lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs
Source: "build\javadocs\*"; DestDir: "{app}\doc"; Flags: ignoreversion recursesubdirs

[Dirs]
Name: "{app}/data/Models"
Name: "{app}/data/MetaData"
Name: "{app}/data/Exports"
