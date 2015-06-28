#!/bin/sh

ROOT=`pwd`
JRE="$ROOT/jre/bin/java"
JARS="$ROOT/jar"
ZIPS="$ROOT/src"

max=-1
for j in `ls $JARS`; do
	timestamp="${j%.*}"
	if [ $timestamp -gt $max ]; then
		max=$timestamp
	fi
done

JAR="$JARS/${max}.jar"

max=-1
for z in `ls $ZIPS`; do
	timestamp="${z%.*}"
	if [ $timestamp -gt $max ]; then
		max=$timestamp
	fi
done

if [ $max -gt -1 ]; then
	ZIP="$ZIPS/${max}.zip"
	mv "$ZIPS/ui" "$ZIPS/ui.bak"
	unzip $ZIP -d $ZIPS
	rm -rf "$ZIPS/ui.bak"
	rm $ZIPS/*.zip
fi

"$JRE" -jar -Djava.library.path=lib "$JAR"
