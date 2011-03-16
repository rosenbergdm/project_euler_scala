#!/bin/sh
#
# Tests all .nw examples to see whether they give the same output
#
SCALIT_MARKUP="scala -cp .. markup.Markup"
NOWEB_MARKUP=/usr/local/noweb/lib/markup
RED=`tput setaf 1`
GREEN=`tput setaf 2`
NORMAL=`tput setaf 0`
for x in *.nw ../markup/markup.nw; do
    $SCALIT_MARKUP $x > /tmp/scalit-out
    $NOWEB_MARKUP $x > /tmp/noweb-out
    differ=`diff -q /tmp/scalit-out /tmp/noweb-out | wc -l`
    if [ $differ -gt 0 ]; then
        printf "Testing %-30s ${NORMAL}[${RED}failed${NORMAL}]\n" $x
    else
	printf "Testing %-30s ${NORMAL}[${GREEN}OK${NORMAL}]\n" $x
    fi
done
