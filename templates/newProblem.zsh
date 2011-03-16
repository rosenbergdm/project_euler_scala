#!/bin/zsh

if [[ -z $1 ]]; then
    print -n "Enter the problem number: "
    read _probNum
    _newname="problem${_probNum}"
else
    _newname="$1"
fi

while [[ -a ${_newname} ]]; do
    print -n "\n${RED}Error: ${RESET}File/directory name ${_newname} is already in use!.\nPlease enter a new problem name: "
    read _newname
done


mkdir ${_newname}
cd ${_newname}

cp ../.templates/{Makefile,problem.lhs} ./

mv problem.lhs ${_newname}.lhs


_webtext=$(w3m -dump "http://projecteuler.net/index.php?section=problems&id=${_probNum}")
_nl=$(echo $_webtext | wc -l | awk '{print $1}')
_qtext=$(echo $_webtext | head -n $((_nl - 1)) | tail -n $((_nl - 15)))

_has_image=0

if $(echo $_qtext | egrep "\[\w*\]" > /dev/null); then
    imname=$(echo $_qtext | egrep "\[\w*\]" |  perl -pi -e 's/\[(\w*)\]/$1/g' | head)
    imurl="http://projecteuler.net/project/images/${imname}.gif"
    wget "$imurl" 2> /dev/null
    convert "${imname}.gif" "${imname}.pdf"
    _has_image=1
fi > /dev/null


for srcfile in ${_newname}.lhs Makefile; do
    perl -pi -e "s/__FILENAME__/${_newname}/g" ${srcfile}
    perl -pi -e "s/__DATE__/$(date)/g" ${srcfile}
    perl -pi -e "s/__PROBLEM__/${_qtext}/" ${srcfile}
done

if [[ "$_has_image" == 0 ]]; then
    perl -pi -e 's/^%__.*$//g' ${_newname}.lhs
else
    perl -pi -e "s/^%__//g; s/__IMAGE__/${imname}/g" ${_newname}.lhs
fi






