#!/bin/sh
#
# Compares the output of the scalit markup to the output of
# the noweb markup
#
SCALIT_MARKUP="scala -cp .. markup.Markup"
NOWEB_MARKUP=/usr/local/noweb/lib/markup

$SCALIT_MARKUP $1 > /tmp/scalit-out
echo "=============="
echo "Scalit output:"
echo "=============="
cat /tmp/scalit-out

$NOWEB_MARKUP $1 > /tmp/noweb-out
echo "============="
echo "Noweb output:"
echo "============="
cat /tmp/noweb-out

echo "===="
echo "Diff"
echo "===="
diff /tmp/scalit-out /tmp/noweb-out
