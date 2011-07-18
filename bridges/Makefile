
all: slides.pdf paper.pdf

slides.pdf: beamerthememanhattan.sty slides.tex manhattancollege.jpg
	pdflatex slides
	pdflatex slides
	pdflatex slides

paper.pdf: paper.tex mybib.bib bridges.sty
	pdflatex paper
	bibtex paper
	pdflatex paper
	pdflatex paper
