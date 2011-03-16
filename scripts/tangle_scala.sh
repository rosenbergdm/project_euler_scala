#!/bin/bash
# encoding: utf-8
# tangle_scala.sh
# 2011-03-16


################ Help and usage message ################

if [[ "$1" = -h* ]] || [[ "$1" = "help" ]] || [[ "$1" = "--help" ]] || [[ "$#" -lt 1 ]]; then
  cat<<'EOF' 

    USAGE: tangle_scala.sh INPUT [OPTS] -- Extract scala code embedded in  markdown 
                                           file INPUT.
     
      -h|--help                         -- Show this help message
      -v|--verbose                      -- Be more verbose
      -o|--output=OUTFILE               -- Write extracted code to OUTFILE (default
                                           is to write to STDOUT.

EOF
exit 0
fi

INPUTFILE="$1"


################  Function definitions  ################

_drop_to_next_start() {
  local itext="$1"
  local nlines=$(echo $itext | wc -l | awk '{print $1}')
  local start_at="$(echo $itext | grep -n '^~~~~*{.Scala}' | head -n1 | awk -F ":" '{print $1}')"
  if [[ -z "$start_at" ]]; then
    start_at=$nlines
  fi
  echo $itext | tail -n $((nlines - start_at))
}

_take_to_next_stop() {
  local itext="$1"
  local extract_file="$2"
  local nlines=$(echo $itext | wc -l | awk '{print $1}')
  local start_at="$(echo $itext | grep -n '^~~~~*$' | head -n1 | awk -F ":" '{print $1}')"
  echo $itext | head -n $((start_at - 1)) >> $extract_file
  echo $itext | tail -n $((nlines - start_at))
}

_deindent_scala() {
  local fname="$1"
  perl -pi -e 's/^    //g' $fname
}


#############  Bash completion script writer ###########

_tangle_scala() {
  local cur prev opts base
  COMPREPLY=( )
  cur="${COMP_WORDS[COMP_CWORD]}"
  prev="${COMP_WORDS[COMP_CWORD-1]}"

  # subcmds="start stop"
  options="-h --help -v --verbose -o --output"

  if [[ "${cur}" = -* ]]; then
    COMPREPLY=( $(compgen -W "${options}" -- ${cur} ) )
    return 0
  fi

  COMPREPLY=( $(compgen -d -- ${cur} ) )
  return 0
}

complete -F _tangle_scala tangle_scala.sh

if [[ -d "/etc/bash_completion.d" ]] && [[ ! -e "/etc/bash_completion.d/_tangle_scala.sh" ]]; then
  {
    printf %q "#!/bin/bash\n\n"
    typeset -f _tangle_scala

    printf %q "complete -F _tangle_scala tangle_scala.sh\n"
  } > /etc/bash_completion.d/_tangle_scala
fi


#######################  MAIN ##########################

tmp=$(mktemp -t tangle_scalaXXXXX)
trap "rm -Rf $tmp" 0
IFS="" input_text="$(cat $INPUTFILE)"
text=$input_text

while [[ -n "$text" ]]; do
  text=$(_drop_to_next_start $text)
  if [[ -n "$text" ]]; then
    text=$(_take_to_next_stop $text $tmp)
  fi
done

_deindent_scala $tmp

if [[ "$(echo "$@" | sed -e 's/ /\n/g' | egrep '^\-o')" ]]; then
  mv $tmp "$(echo "$@" |  perl -pi -e 's/.*-o ([^ ]+).*/$1/')"
elif [[ "$(echo "$@" | sed -e 's/ /\n/g' | egrep '\-\-output=')" ]]; then
  mv $tmp "$(echo "$@" |  perl -pi -e 's/.*--output=([^ ]+).*/$1/')"
else
  IFS="" text=$(cat $tmp)
  rm $tmp
  echo $text
fi




  

