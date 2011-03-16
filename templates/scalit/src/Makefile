#
# Convenience makefile to produce documentation and source in one step
#
TANGLE=scala -cp classes scalit.tangle.LitComp
PLAINTANGLE=scala -cp classes scalit.tangle.Tangle 
TANGLECLASS=classes/scalit/tangle/LitComp.class
WEAVE=scala -cp classes scalit.weave.Weave -classpath classes -vf t -idx t 
PLAINWEAVE=scala -cp classes scalit.weave.Weave -vf f -idx f 
WEAVECLASS=classes/scalit/weave/Weave.class
LATEX=perltex --latex=pdflatex 

all: doc code tools

doc: markup/markup.pdf markup/blocks.pdf tangle/tangle.pdf \
	tangle/compilesupport.pdf weave/weave.pdf util/conversions.pdf \
	util/commandline.pdf util/filters.pdf

# Runs the bootstrap script
bootstrap:
	./bootstrap.sh

# Installs the literate programming jar in the main library
# Also puts the binaries under bin/
# Is this a permissible way to do it?
#
install: jar
	cp scalit.jar ${SCALA_HOME}/lib/
	cp bin/litcomp bin/sweave ${SCALA_HOME}/bin/

# Creates the jar file from the classes
jar: code
	jar cf scalit.jar -C classes .

# Makes a buildable distribution from the current version
scalit-src: all jar
	rm -rf scalit-src
	mkdir scalit-src
	mkdir -p scalit-src/{markup,tangle,weave,util,toolsupport,images,bin}
	cp markup/*.nw scalit-src/markup/
	cp tangle/*.nw scalit-src/tangle/
	cp weave/*.nw  scalit-src/weave/
	cp util/*.nw   scalit-src/util/
	cp images/*.pdf scalit-src/images/
	cp toolsupport/*.java scalit-src/toolsupport/
	cp Makefile scalit-src/
	cp bootstrap.sh scalit-src/
	cp scalit.jar scalit-src/
	cp scaladefs.sty scalit-src/
	cp scalit.sty scalit-src/
	cp patch_my_compiler.sh scalit-src/
	cp literate_2.7.6.diff scalit-src/
	cp README scalit-src/
	rm -f scalit-src.tar.gz
	tar cfz scalit-src.tar.gz scalit-src/*
	rm -r scalit-src

# Creates the minimal binary distribution
scalit-bin: all jar
	rm -rf scalit-bin
	mkdir scalit-bin
	cp scalit.jar scalit-bin/
	scala scalit.weave.Weave markup/markup.nw \
		markup/blocks.nw tangle/tangle.nw \
		tangle/compilesupport.nw weave/weave.nw \
		util/conversions.nw util/commandline.nw \
		util/filters.nw \
		-o scalit.tex
	${LATEX} scalit.tex
	cp scalit.pdf scalit-bin/
	cp scaladefs.sty scalit-bin/
	cp scalit.sty scalit-bin/
	cp README scalit-bin/
	cp bin/litcomp scalit-bin/
	cp bin/sweave scalit-bin/
	tar cfz scalit-bin.tar.gz scalit-bin/*
	rm -r scalit-bin

%.tex : %.nw ${WEAVECLASS}
	${WEAVE} $< -o $@

markup/markup.pdf: markup/markup.tex
	${LATEX} markup/markup.tex
	mv markup.pdf markup/markup.pdf

markup/blocks.pdf: markup/blocks.tex
	${LATEX} markup/blocks.tex
	mv blocks.pdf markup/blocks.pdf

tangle/tangle.pdf: tangle/tangle.tex
	${LATEX} tangle/tangle.tex
	mv tangle.pdf tangle/tangle.pdf

tangle/compilesupport.pdf: tangle/compilesupport.tex
	${LATEX} tangle/compilesupport.tex
	mv compilesupport.pdf tangle/compilesupport.pdf

weave/weave.pdf: weave/weave.tex
	${LATEX} weave/weave.tex
	mv weave.pdf weave/weave.pdf

util/conversions.pdf: util/conversions.tex
	${LATEX} util/conversions.tex
	mv conversions.pdf util/conversions.pdf

util/commandline.pdf: util/commandline.tex
	${LATEX} util/commandline.tex
	mv commandline.pdf util/commandline.pdf

util/filters.pdf: util/filters.tex
	${LATEX} util/filters.tex
	mv filters.pdf util/filters.pdf

code: classes/scalit/markup/Markup.class classes/scalit/markup/CodeBlock.class \
	classes/scalit/tangle/Tangle.class classes/scalit/tangle/LitComp.class \
	classes/scalit/weave/Weave.class classes/scalit/util/conversions.class \
	classes/scalit/util/LiterateSettings.class classes/scalit/util/MarkupFilter.class

classes/scalit/util/LiterateSettings.class: util/commandline.nw
	${TANGLE} util/commandline.nw -d classes

classes/scalit/markup/Markup.class: markup/markup.nw
	${TANGLE} markup/markup.nw -d classes

classes/scalit/markup/CodeBlock.class: markup/blocks.nw
	${TANGLE} markup/blocks.nw -d classes

classes/scalit/tangle/Tangle.class: tangle/tangle.nw
	${TANGLE} tangle/tangle.nw -d classes

classes/scalit/tangle/LitComp.class: tangle/compilesupport.nw
	${TANGLE} tangle/compilesupport.nw -d classes

classes/scalit/weave/Weave.class: weave/weave.nw classes/toolsupport/verbfilterScala.class
	${TANGLE} weave/weave.nw -d classes

classes/scalit/toolsupport/verbfilterScala.class: toolsupport/verbfilterScala.java
	javac toolsupport/verbfilterScala.java -d classes

classes/scalit/util/conversions.class: util/conversions.nw
	${TANGLE} util/conversions.nw -d classes

classes/scalit/util/MarkupFilter.class: util/filters.nw
	${TANGLE} util/filters.nw -d classes


tools: bin/sweave bin/litcomp util/tools.pdf

bin/sweave: util/tools.nw
	${PLAINTANGLE} util/tools.nw -r sweave > bin/sweave
	chmod a+x bin/sweave

bin/litcomp: util/tools.nw
	${PLAINTANGLE} util/tools.nw -r litcomp > bin/litcomp
	chmod a+x bin/litcomp

util/tools.pdf: util/tools.tex
	${LATEX} util/tools.tex
	mv tools.pdf util/tools.pdf

util/tools.tex: util/tools.nw
	${PLAINWEAVE} util/tools.nw -o util/tools.tex
