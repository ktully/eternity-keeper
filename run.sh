#!/bin/sh

my_java=`which java`
jre_dir=$(dirname $(readlink -f $my_java))
lib=$(readlink -f "./lib/native/linux64")
snapshot="snapshot_blob.bin"
natives="natives_blob.bin"
icudtl="icudtl.dat"
cp=`find ./lib/dependencies/ -iname "*.jar" ! -iname "*windows*" -print0 \
	| xargs -0 readlink -f \
	| tr "\n" ":"`

if [ ! -h "$jre_dir/$snapshot" -o ! -h "$jre_dir/$natives" -o ! -h "$jre_dir/$icudtl" ]; then
	echo "Symbolic links need to be set up before running this script."
	echo "Please run the following (possibly as root)."
	echo "ln -s \"$lib/$snapshot\" \"$jre_dir/$snapshot\""
	echo "ln -s \"$lib/$natives\" \"$jre_dir/$natives\""
	echo "ln -s \"$lib/$icudtl\" \"$jre_dir/$icudtl\""
	exit 1
fi

export LD_LIBRARY_PATH="$lib:$LD_LIBRARY_PATH"
export LD_PRELOAD="libcef.so"

"$my_java" \
-Djava.library.path="$lib" \
-cp "${cp}target/eternity-0.1.jar" \
uk.me.mantas.eternity.EternityKeeper
