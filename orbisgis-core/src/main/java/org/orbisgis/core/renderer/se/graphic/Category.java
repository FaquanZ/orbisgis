package org.orbisgis.core.renderer.se.graphic;

import org.orbisgis.core.renderer.se.SymbolizerNode;
import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.core.renderer.se.fill.Fill;
import org.orbisgis.core.renderer.se.parameter.real.RealParameter;
import org.orbisgis.core.renderer.se.stroke.Stroke;

/**
 *
 * @author maxence
 * @todo add support for stacked bar (means category fill / stroke are mandatory) and others are forbiden
 */
public class Category implements SymbolizerNode {

    public Category() {
        graphic = new GraphicCollection();
        graphic.setParent(this);
    }

    public Fill getFill() {
        return fill;
    }

    public void setFill(Fill fill) {
        this.fill = fill;
        fill.setParent(this);
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
        stroke.setParent(this);
    }


    @Override
    public Uom getUom() {
        return parent.getUom();
    }

    @Override
    public SymbolizerNode getParent() {
        return parent;
    }

    @Override
    public void setParent(SymbolizerNode node) {
        parent = node;
    }

    private RealParameter measure;

    /* in order to draw bars, optionnal */
    private Fill fill;
    private Stroke stroke;

    /* In order to draw points, optionnal */
    private GraphicCollection graphic;
    private SymbolizerNode parent;

}