#!/bin/sh

ROOT=$(dirname "$(readlink -f $0)")
JRE="$ROOT/jre/bin/java"
JARS="$ROOT/jar"
SRC="$ROOT/src"

# Detect an update.
max=-1
for f in `find "$ROOT" -iname "*.zip"`; do
	zip=`basename "$f"`
	timestamp="${zip%.*}" # Remove the extension.
	if [ $timestamp -gt $max ]; then
		max=$timestamp
	fi
done

if [ $max -gt -1 ]; then
	# Clean up any old update directory.
	if [ -d "$ROOT/update" ]; then
		rm -rf "$ROOT/update"
	fi

	mkdir "$ROOT/update"
	unzip "$ROOT/${max}.zip" -d "$ROOT/update"

	# There should only be one directory in the zip.
	UPDATE=`ls "$ROOT/update/"`
	UPDATE="$ROOT/update/$UPDATE"

	# Remove the old backups.
	if [ -d "$ROOT/jar.old" ]; then
		rm -rf "$ROOT/jar.old"
	fi

	if [ -d "$ROOT/src.old" ]; then
		rm -rf "$ROOT/src.old"
	fi

	# Create new backups.
	mv "$JARS" "$ROOT/jar.old"
	mv "$SRC" "$ROOT/src.old"

	# Remove the now backed-up directories.
	rm -rf "$JARS"
	rm -rf "$SRC"

	mkdir "$JARS"
	mkdir "$SRC"

	# Copy the updates.
	cp "$UPDATE/*.jar" "$JARS"
	cp -r "$UPDATE/ui" "$SRC"

	# Clean up any left over zips.
	rm -f "$ROOT/*.zip"
fi

LIB="$ROOT/lib"
export LD_LIBRARY_PATH="$LIB:$LD_LIBRARY_PATH"
export LD_PRELOAD="libcef.so"

"$JRE" \
-Djava.library.path="$LIB" \
-cp "$JARS/*" \
uk.me.mantas.eternity.EternityKeeper
