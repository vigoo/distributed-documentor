#!/bin/sh

mvn install:install-file -Dfile=org.eclipse.mylyn.wikitext.core_1.8.0.I20130522-1811.jar -DartifactId=core -DgroupId=org.wikitext -Dversion=1.8.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=org.eclipse.mylyn.wikitext.mediawiki.core_1.8.0.I20130522-1811.jar -DartifactId=mediawiki-core -DgroupId=org.wikitext -Dversion=1.8.0-SNAPSHOT -Dpackaging=jar 
