mkdir tmp\snapshot\3DFu_Core_jar\edu\ncsa\model

echo Mesh.class > tmp\snapshot\excludes.txt
xcopy bin\edu\ncsa\model\*.class tmp\snapshot\3DFu_Core_jar\edu\ncsa\model /i /y /EXCLUDE:tmp\snapshot\excludes.txt
copy bin\edu\ncsa\model\AnimatedMesh.class tmp\snapshot\3DFu_Core_jar\edu\ncsa\model

jar cvf tmp/snapshot/3DFu_Core.jar -C tmp/snapshot/3DFu_Core_jar .
