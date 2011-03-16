#!/bin/sh
#
# Uses the jar to extract the source code from markup.nw
# and tangle.nw and compile it, then uses the compiled tangle
# to compile directly from literate source files.
#
tangle="scala -cp scalit.jar $1 scalit.tangle.Tangle"

# If we specified another command for tangle, then we'll use this
if [ "$1" != "" ]; then
    tangle="$1"
fi

echo "* Tangling and compiling with $tangle"
mkdir -p bs/{util,markup,tangle}
$tangle util/conversions.nw > bs/util/conversions-noweb.scala
$tangle util/commandline.nw > bs/util/commandline-noweb.scala
$tangle util/filters.nw > bs/util/filters-noweb.scala
$tangle markup/markup.nw > bs/markup/markup-noweb.scala
$tangle markup/blocks.nw > bs/markup/blocks-noweb.scala
$tangle tangle/tangle.nw > bs/tangle/tangle-noweb.scala
$tangle tangle/compilesupport.nw > bs/tangle/compilesupport-noweb.scala

mkdir -p stage1
scalac -deprecation -d stage1 bs/markup/markup-noweb.scala \
    bs/util/conversions-noweb.scala bs/markup/blocks-noweb.scala \
    bs/tangle/tangle-noweb.scala bs/tangle/compilesupport-noweb.scala \
    bs/util/commandline-noweb.scala bs/util/filters-noweb.scala

if [ $? -eq 0 ]; then
    echo "* Successfully extracted the source and compiled"
    echo "  the scala tangle. Now using this build"
    
    mkdir -p bootstrap

    scala -cp stage1 tangle.LitComp markup/markup.nw markup/blocks.nw \
	util/filters.nw util/commandline.nw util/conversions.nw \
	tangle/tangle.nw tangle/compilesupport.nw \
	-d bootstrap

    if [ $? -eq 0 ]; then
	echo "* Removing old compiled files"
	rm -r stage1
#	rm -r bs

	echo "* Compiling with the new tangle"
	mkdir -p classes
	javac toolsupport/verbfilterScala.java -d classes
	scala -cp bootstrap tangle.LitComp markup/markup.nw markup/blocks.nw \
	    util/filters.nw util/commandline.nw util/conversions.nw \
	    tangle/tangle.nw tangle/compilesupport.nw \
	    -d classes
	if [ $? -eq 0 ]; then
	    echo "* Compilation succeeded"
	    rm -r bootstrap
	fi
    else
	echo "!!! Could not compile with noweb-compiled tangle"
    fi
else
    echo "!!! Compilation of noweb-tangled source files failed"
fi

