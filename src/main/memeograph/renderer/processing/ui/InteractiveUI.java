package memeograph.renderer.processing.ui;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import java.io.FileNotFoundException;
import memeograph.Config;
import memeograph.Generator;
import memeograph.generator.jdi.InteractiveStep;
import memeograph.renderer.processing.ProcessingApplet;
import memeograph.util.SourceCodeManager;

public class InteractiveUI extends UI{

    private final InteractiveStep is;
    private final SourceCodeManager scm = Config.getConfig().getTargetProgram();

    public InteractiveUI(ProcessingApplet pApplet){
        super(pApplet);

        Generator g = Config.getConfig().getGenerator();
        if (g instanceof InteractiveStep) {
            is = (InteractiveStep)g;
        }else{
            is = null;
        }
    }

    @Override
    public WidgetContainer getBottomLeft(){
        if (is == null) { return super.getBottomLeft(); }

        return new LeftJustifiedBottomUp(){{
            add(new TextWidget(){
                public String getText(){
                    Location loc = is.getCurrentLocation();
                    try {
                        return "About to run line " + loc.lineNumber()
                                + " in " + loc.sourceName();
                    } catch (AbsentInformationException ex) {
                        return "";
                    }
                }
            });
            newRow();
            add(new TextWidget(){
                @Override
                public String getText(){
                    try {
                        Location loc = is.getCurrentLocation();
                        return "    " + scm.getLineFinder(loc.sourceName()).getLine(loc.lineNumber()).trim();
                    } catch (AbsentInformationException ex) {
                    } catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace();
                    }
                    return "";
                }
            });
        }};
    }

    @Override
    public WidgetContainer getBottomRight(){
        WidgetContainer br = super.getBottomRight();
        br.newRow(); br.add(new TextWidget("O : step over the current line"));
        br.newRow(); br.add(new TextWidget("I : step into the current line"));
        return br;
    }

}
