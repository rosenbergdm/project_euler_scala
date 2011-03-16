#!/bin/sh
#
# This script allows you to integrate literate programming support into
# the scala compiler. This is done by tangling the source files into the
# directory of the scala compiler.
SOURCEFILES="markup/blocks.nw markup/markup.nw tangle/compilesupport.nw tangle/tangle.nw util/commandline.nw util/conversions.nw util/filters.nw weave/weave.nw"
TANGLE="scala -cp scalit.jar scalit.tangle.Tangle"

if [ $1 ]; then
    COMPILERDIR="$1/src/compiler/scalit"
    # first, make the necessary directories
    for dir in markup tangle util weave; do
	mkdir -p "$COMPILERDIR/$dir"
    done

    for x in $SOURCEFILES; do
	scalafile=$COMPILERDIR/`echo $x | sed 's/\\.nw/.scala/g'`
	$TANGLE $x > "$scalafile"
    done

    # We also have to include the correct verbfilter
    mkdir -p "$1/src/compiler/toolsupport"
    cp toolsupport/verbfilterScala.java "$1/src/compiler/toolsupport"

    # Finally, apply the patch
    cp literate_2.7.6.diff $1
    cd $1
    patch -p0 -i literate_2.7.6.diff
else
    echo "Usage: ./patch_my_compiler.sh main_scala_source_dir"
fi