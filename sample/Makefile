#!/usr/bin/env make

include config.mk

DATAFILES          = 
IMAGEFILES         = 
SRC                = problem_sample.lscala
SCALA_SRC          = problem_sample.scala
SCALA_OUT          = problem_sample.out
OUTPUT_MKD         = problem_sample_solution.lscala
OUTPUT_HTML        = problem_sample_solution.html

.PHONY: all compile view clean

view:			$(OUTPUT_HTML)
			$(BROWSER) $< &

all:			$(OUTPUT_HTML)

compile:		$(OUTPUT_MKD)

clean: 
	rm -Rf *.out *.scala *solution.lscala *solution.html

$(SCALA_SRC):		$(SRC)
	$(STANGLE) $< --output=$@

$(SCALA_OUT): 		$(SCALA_SRC)
	$(SCALAC) $< 
	$(SCALA) -cp . $$(ls *.class | head -n1 | sed -e 's#.class##') -e "$$(ls *.class | head -n1 | sed -e 's#.class##').main(null)" > $@

$(OUTPUT_MKD): 		$(SRC) $(SCALA_OUT) 
	( cat $< && \
	  echo '##Result' && \
	  echo '~~~~~~~~~~~~~~~~~~~~~' && \
	  echo '> scalac $(SCALA_SRC)' && \
	  echo "> scala -cp . $$(ls *.class | head -n1 | sed -e 's#.class##') -e \"$$(ls *.class | head -n1 | sed -e 's#.class##').main(null)\"" && \
	  echo -n "  " && cat $(SCALA_OUT) && \
	  echo '~~~~~~~~~~~~~~~~~~~~~' && \
	  echo && echo ) > $@



$(OUTPUT_HTML): 	$(OUTPUT_MKD)
	$(PANDOC_EXE) -s -S -t html $< > $@


