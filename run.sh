#!/bin/sh

# Run from this script's own directory. Every path below is relative, and on Linux
# EternityKeeper additionally resolves the UI as new File("src/ui/index.html") against the
# working directory, so the browser only finds it when we are in the project root.
cd "$(dirname "$0")" || exit 1

my_java=$(command -v java)
if [ -z "$my_java" ]; then
	echo "Could not find java on PATH. A JDK 8 is needed."
	exit 1
fi

jre_dir=$(dirname "$(readlink -f "$my_java")")
lib=$(readlink -f "./lib/native/linux64")

if [ ! -f "$lib/libjcef.so" ]; then
	echo "Native libraries not found in \"$lib\"."
	exit 1
fi

# The linux64 profile deliberately keeps the native dependencies out of the shaded jar, so
# unlike the Windows build they have to be named on the classpath here.
cp=`find ./lib/dependencies/ -iname "*.jar" ! -iname "*windows*" -print0 \
	| xargs -0 readlink -f \
	| tr "\n" ":"`

# CEF looks for these alongside the executable, so they have to be reachable from the JRE
# directory. Test for existence rather than specifically for a symlink, so that copying them
# in works too and so that a dangling link is still reported as missing.
missing=
for blob in snapshot_blob.bin natives_blob.bin icudtl.dat; do
	if [ ! -e "$jre_dir/$blob" ]; then
		missing="$missing $blob"
	fi
done

if [ -n "$missing" ]; then
	echo "Some CEF data files need to be set up before running this script."
	echo "Please run the following (possibly as root)."
	for blob in $missing; do
		echo "ln -s \"$lib/$blob\" \"$jre_dir/$blob\""
	done
	exit 1
fi

# Discover the jar rather than hard-coding it so this keeps working when the version in
# pom.xml changes. The glob anchors at the start of the name, so the pre-shade
# original-eternity-<version>.jar that maven-shade-plugin leaves behind is not a candidate.
jar=
for candidate in target/eternity-*.jar; do
	[ -f "$candidate" ] && jar="$candidate"
done

if [ -z "$jar" ]; then
	echo "No built jar found in target/. Build one first with:"
	echo "    mvn install -Plinux64"
	exit 1
fi

export LD_LIBRARY_PATH="$lib:$LD_LIBRARY_PATH"
export LD_PRELOAD="libcef.so"

"$my_java" \
-Djava.library.path="$lib" \
-cp "${cp}$jar" \
uk.me.mantas.eternity.EternityKeeper
