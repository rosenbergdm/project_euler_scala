#!/bin/sh

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
