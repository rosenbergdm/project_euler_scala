#!/usr/bin/env make

PROBLEM_NO         = @PROBLEM_NO@

SCALA_HOME         = @SCALA_HOME@
PROJECT_EULER_HOME = @PROJECT_EULER_HOME@
PANDOC_EXE         = @PANDOC_EXE@
SCALA              = @SCALA@
SCALAC             = @SCALAC@
BROWSER            = @BROWSER@

SRC                = problem_$(PROBLEM_NO).src
DATAFILES          = @DATA_FILES@
IMAGEFILES         = @IMAGEFILES@
SCALA_SRC          = problem_$(PROBLEM_NO).scala




LHSTEX = $(shell which lhs2Tex)
PDFTEX = $(shell which pdflatex)
PDF = __FILENAME__.pdf
TEX = __FILENAME__.tex
LHS = __FILENAME__.lhs


all : __FILENAME__.pdf

__FILENAME__.tex : __FILENAME__.lhs
	$(LHSTEX) $< > __FILENAME__.tex

__FILENAME__.pdf : __FILENAME__.tex
	$(PDFTEX) $<
