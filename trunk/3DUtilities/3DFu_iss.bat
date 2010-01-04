REM Create needed jars and compile 3DFu installation

call 3DFu_jar
call 3DFu_doc
call ModelViewerLite_jar
"C:\Program Files\Inno Setup 5\Compil32" /cc 3DFu.iss