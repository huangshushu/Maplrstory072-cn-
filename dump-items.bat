@echo off
@title Dump
set CLASSPATH=.;dist\odinms_sea.jar;dist\mina-core.jar;dist\slf4j-api.jar;dist\slf4j-jdk14.jar;dist\mysql-connector-java-bin.jar;dist\bcprov-jdk16-145.jar
java -server -Dnet.sf.odinms.wzpath=wz\ -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.trustStore=filename.truststore -Djavax.net.ssl.keyStorePassword=HYGEomgLOL -Djavax.net.ssl.trustStorePassword=HYGEomgLOL tools.wztosql.DumpItems
pause