"=============================================================================
" $Id$
" File:         /opt/git/rosenbergdm/project_euler_scala/lscala.vim {{{1
" Author:       root
" Version:      0.01
" Created:      Thu 17 Mar 2011 11:03:13 AM CDT
" Last Update:  $Date$
"------------------------------------------------------------------------
" Description:
"       Literate scala (combined markdown and scala) syntax highlighting.
" 
"------------------------------------------------------------------------
" Installation:
"       Drop this file into {rtp}/syntax
"       Requires Vim7+
" }}}1
"=============================================================================


if version < 600
  syntax clear
elseif exists("b:current_syntax")
  finish
endif

runtime! syntax/markdown.vim
let b:current_syntax = ''
unlet b:current_syntax
syntax include @mkd syntax/markdown.vim
let b:current_syntax = ''
unlet b:current_syntax
syntax include @scala syntax/scala.vim
syntax region scalaCode matchgroup=Snip start="^\~\~\~*{.Scala}$" end="^\~\~\~\~*$" containedin=@mkd contains=@scala

hi link Snip SpecialComment
let b:current_syntax = 'lscala'


let s:cpo_save=&cpo
set cpo&vim
"------------------------------------------------------------------------
let &cpo=s:cpo_save
"=============================================================================
" vim600: set fdm=marker:
