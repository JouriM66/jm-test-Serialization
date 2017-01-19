@echo off
set _LIBS=../lib/minimal-json-0.9.4.jar
set _LIBS=%_LIBS%;../lib/jackson-annotations-2.0.4.jar;../lib/jackson-core-2.0.4.jar;../lib/jackson-databind-2.0.4.jar
set _LIBS=%_LIBS%;../lib/nanoxml-2.2.3.jar
set _LIBS=%_LIBS%;SerializableTest.jar
java -d64 -Xms6G -Xmx8G -cp %_LIBS% jm.test.Serialize.SerMainKt %*
