mkdir tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model\loaders
mkdir tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model\matrix

xcopy bin\edu\ncsa\model\ModelViewer.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model /i /y
xcopy bin\edu\ncsa\model\ModelViewerApplet.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model /i /y
xcopy bin\edu\ncsa\model\Mesh*.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model /i /y
xcopy bin\edu\ncsa\model\Utils*.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model /i /y
xcopy bin\edu\ncsa\model\ImageUtils*.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model /i /y
xcopy bin\edu\ncsa\model\RayTracer*.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model /i /y
xcopy bin\edu\ncsa\model\matrix\MatrixUtils.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model\matrix /i /y
xcopy bin\edu\ncsa\model\loaders\MeshLoader_OBJ.class tmp\snapshot\ModelViewerLite_jar\edu\ncsa\model\loaders /i /y

jar cvf tmp/snapshot/ModelViewerLite.jar -C tmp/snapshot/ModelViewerLite_jar .
