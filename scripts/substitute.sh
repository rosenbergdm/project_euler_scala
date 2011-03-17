#!/bin/bash
# encoding: utf-8
# substitute.sh
# 2011-03-17


substitue_char_entities() {
  sed -e "s/<img src='images\/symbol_le.gif' [^>]*>/ \&le; /g" \
      -e "s/<img src='images\/symbol_ge.gif' [^>]*>/ \&ge; /g" \
      -e "s/<img src='images\/symbol_gt.gif' [^>]*>/ \&gt; /g" \
      -e "s/<img src='images\/symbol_lt.gif' [^>]*>/ \&lt; /g" \
      -e "s/<img src='images\/symbol_maps.gif' [^>]*>/ \&rarr; /g" \
      -e "s/<img src='images\/symbol_minus.gif' [^>]*>/ \&minus; /g" \
      -e "s/<img src='images\/symbol_ne.gif' [^>]*>/ \&nee; /g" \
      -e "s/<img src='images\/symbol_radic.gif' [^>]*>/ \&radic; /g" \
      -e "s/<img src='images\/symbol_times.gif' [^>]*>/ \&times; /g" 
}

fetch_links() {
  text="$1"
  links=( $( echo $1 | \
    grep "project\/" | \
      sed -e 's/ /\n/g' | \
      grep 'project\/' | \
      sed -e "s/'/\"/g" -e 's#^.*="\(.*\)".*$#\1#' \
          ) )
  for l in $links; do 
    wget "http://projecteuler.net/${l}" -O $(basename $l)
  done
}



