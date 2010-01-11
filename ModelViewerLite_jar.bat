mkdir tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model\loaders

xcopy bin\edu\ncsa\model\ModelViewer.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model /i /y
xcopy bin\edu\ncsa\model\ModelViewerApplet.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model /i /y
xcopy bin\edu\ncsa\model\Mesh*.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model /i /y
xcopy bin\edu\ncsa\model\AnimatedMesh*.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model /i /y
xcopy bin\edu\ncsa\model\RayTracer*.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model /i /y
xcopy bin\edu\ncsa\model\loaders\MeshLoader_OBJ.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model\loaders /i /y

cd tmp/snapshot/ModelViewerLite_jar
jar xf ../../../lib/ncsa/ImageUtilities.jar
cd ../../..

cd tmp/snapshot/ModelViewerLite_jar
jar xf ../../../lib/ncsa/MatrixUtilities.jar
cd ../../..

cd tmp/snapshot/ModelViewerLite_jar
jar xf ../../../lib/ncsa/Utilities.jar
cd ../../..

jar cvf tmp/snapshot/ModelViewerLite.jar -C tmp/snapshot/ModelViewerLite_jar .
