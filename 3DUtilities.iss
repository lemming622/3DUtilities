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
Source: "build\3DUtilities.jar"; DestDir: "{app}"; Flags: ignoreversion; AfterInstall: BuildDefaultConfiguration
Source: "build\ModelViewer_Lite.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "polyglotDomain_OBJ.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "ModelViewer.php"; DestDir: "{app}"; Flags: ignoreversion
Source: "logos\*"; DestDir: "{app}\logos"; Flags: ignoreversion recursesubdirs
Source: "lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs
Source: "build\javadocs\*"; DestDir: "{app}\doc"; Flags: ignoreversion recursesubdirs

[Dirs]
Name: "{app}/data/Models"
Name: "{app}/data/MetaData"
Name: "{app}/data/Exports"

[Code]
procedure BuildDefaultConfiguration;
var
  output: string;
begin
  output := ExpandConstant('{app}') + '\ModelBrowser.bat'
  SaveStringToFile(output, 'java -cp "%~dp03DUtilities.jar;%~dp0lib/ncsa/3DUtilities_Loaders.jar;%~dp0lib/ncsa/ImageUtilities.jar;%~dp0lib/ncsa/MatrixUtilities.jar;%~dp0lib/ncsa/Utilities.jar;%~dp0lib/jogl-1.1.1/jogl.jar;%~dp0lib/jogl-1.1.1/gluegen-rt.jar;%~dp0lib/jdom-1.1/jdom.jar;%~dp0lib/jama-1.0.2/Jama-1.0.2.jar;%~dp0lib/j3d/xj3D-1.0/xj3d-all.jar;%~dp0lib/j3d/j3d-1.5.2/j3dcore.jar;%~dp0lib/j3d/j3d-1.5.2/j3dutils.jar;%~dp0lib/ncsa/portfolio/portfolio.jar" -Djava.library.path="%~dp0lib/jogl-1.1.1/windows-i586;%~dp0lib/loaders/j3d-1.5.2" -Xmx1g edu.ncsa.model.ModelBrowser', false);

  output := ExpandConstant('{app}') + '\ModelConvert.bat'
  SaveStringToFile(output, 'java -cp "%~dp03DUtilities.jar;%~dp0lib/ncsa/3DUtilities_Loaders.jar;%~dp0lib/jogl-1.1.1/jogl.jar;%~dp0lib/jogl-1.1.1/gluegen-rt.jar;%~dp0lib/jdom-1.1/jdom.jar;%~dp0lib/jama-1.0.2/Jama-1.0.2.jar;%~dp0lib/j3d/xj3D-1.0/xj3d-all.jar;%~dp0lib/j3d/j3d-1.5.2/j3dcore.jar;%~dp0lib/j3d/j3d-1.5.2/j3dutils.jar;%~dp0lib/ncsa/portfolio/portfolio.jar" -Xmx1g edu.ncsa.model.ModelConverter %1 %2', false);

  output := ExpandConstant('{app}') + '\ModelViewer.ini'
  SaveStringToFile(output, 'LoadPath=data/Models' + #10, false);
  SaveStringToFile(output, 'ExportPath=data/Exports' + #10, true);
  SaveStringToFile(output, 'MetaDataPath=data/MetaData' + #10, true);
  SaveStringToFile(output, 'Adjust=true' + #10, true);
  SaveStringToFile(output, 'Signature=edu.ncsa.model.signatures.MeshSignature_LightField' + #10, true);
  SaveStringToFile(output, 'RebuildSignatures=true' + #10, true);
  SaveStringToFile(output, 'DefaultModel=logos/ncsa_horizontal_3D.dae' + #10, true);
  SaveStringToFile(output, 'Ortho=true' + #10, true);
  SaveStringToFile(output, 'Axis=true' + #10, true);
  SaveStringToFile(output, 'Points=false' + #10, true);
  SaveStringToFile(output, 'Transparent=false' + #10, true);
  SaveStringToFile(output, 'Shaded=true' + #10, true);
  SaveStringToFile(output, 'AutoRefresh=false', true);
  
  output := ExpandConstant('{app}') + '\ModelBrowser.ini'
  SaveStringToFile(output, 'LoadPath=data/Models' + #10, false);
  SaveStringToFile(output, 'MetaDataPath=data/MetaData' + #10, true);
  SaveStringToFile(output, 'Polyglot=http://teeve3.ncsa.uiuc.edu' + #10, true);
  SaveStringToFile(output, 'ConvertableList=polyglotDomain_OBJ.txt' + #10, true);
  SaveStringToFile(output, 'TransparentPanels=false' + #10, true);
  SaveStringToFile(output, 'WiiMote=false' + #10, true);
  SaveStringToFile(output, 'RebuildThumbs=false', true);
end;
