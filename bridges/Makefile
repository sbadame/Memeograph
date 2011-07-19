all: slides.pdf paper.pdf

slides.pdf: beamerthememanhattan.sty slides.tex manhattancollege.jpg mybib.bib
	pdflatex slides
	bibtex slides
	pdflatex slides
	pdflatex slides

paper.pdf: paper.tex mybib.bib bridges.sty
	pdflatex paper
	bibtex paper
	pdflatex paper
	pdflatex paper

clean:
	rm -f paper.aux paper.bbl paper.blg paper.log paper.pdf slides.aux slides.bbl slides.blg slides.log slides.nav slides.out slides.pdf slides.snm slides.toc
