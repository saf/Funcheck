.PHONY: clean tempclean view

all: praca.pdf

praca.pdf: praca.tex
	pdflatex praca.tex
	bibtex praca
	pdflatex praca.tex
	pdflatex praca.tex
	rm praca.aux praca.log praca.toc praca.bbl praca.blg

clean:
	rm -f praca.pdf 

tempclean:
	rm *~

view:
	screen -dmS mgrview evince praca.pdf