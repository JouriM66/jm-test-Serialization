@echo off
set _LIBS=D:/java/myjava/lib/minimal-json-0.9.4.jar
set _LIBS=%_LIBS%;D:\java\myjava\lib\jackson-annotations-2.0.4.jar;D:\java\myjava\lib\jackson-core-2.0.4.jar;D:\java\myjava\lib\jackson-databind-2.0.4.jar
set _LIBS=%_LIBS%;SerializableTest.jar
java -d64 -Xms6G -Xmx8G -cp %_LIBS% jm.test.Serialize.SerMainKt "%1" %2 %3 %4 %5 %6 %7 %8 %9
