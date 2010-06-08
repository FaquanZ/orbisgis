/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.orbisgis.core.renderer.se.label;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.io.IOException;
import javax.xml.bind.JAXBElement;
import org.orbisgis.core.renderer.persistance.se.LabelType;
import org.gdms.data.DataSource;

import org.orbisgis.core.renderer.persistance.se.LineLabelType;
import org.orbisgis.core.renderer.persistance.se.PointLabelType;
import org.orbisgis.core.renderer.se.SymbolizerNode;
import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.SeParameterFactory;

/**
 *
 * @author maxence
 */
public abstract class Label implements SymbolizerNode {

    public enum HorizontalAlignment {

        LEFT, CENTER, RIGHT;

        public static HorizontalAlignment fromString(String token) {
            if (token.equals("left"))
                return LEFT;
            
            if (token.equals("center"))
                return CENTER;

            return RIGHT; // default value
        }
    }

    public enum VerticalAlignment {

        TOP, MIDDLE, BASELINE, BOTTOM;

        public static VerticalAlignment fromString(String token) {
            if (token.equals("bottom"))
                return BOTTOM;
            if (token.equals("middle"))
                return MIDDLE;
            if (token.equals("baseline"))
                return BASELINE;
             return TOP;
        }
    }

    public static Label createLabelFromJAXBElement(JAXBElement<? extends LabelType> l) {
        if (l.getDeclaredType() == PointLabelType.class) {
            return new PointLabel((JAXBElement<PointLabelType>)l);
        } else if (l.getDeclaredType() == LineLabelType.class) {
            return new LineLabel((JAXBElement<LineLabelType>)l);
        }

        return null;
    }

    protected Label(){
    }

    protected Label(JAXBElement<? extends LabelType> l) {
        LabelType t = (LabelType) l.getValue();

        if (t.getUnitOfMeasure() != null) {
            this.uom = Uom.fromOgcURN(t.getUnitOfMeasure());
        }

        if (t.getStyledLabel() != null) {
            this.setLabel(new StyledLabel(t.getStyledLabel()));
        }

        if (t.getHorizontalAlignment() != null) {
            this.hAlign = HorizontalAlignment.fromString(SeParameterFactory.extractToken(t.getHorizontalAlignment()));
        }

        if (t.getVerticalAlignment() != null) {
            this.vAlign = VerticalAlignment.fromString(SeParameterFactory.extractToken(t.getVerticalAlignment()));
        }
    }

    @Override
    public Uom getUom() {
        if (uom != null) {
            return uom;
        } else {
            return parent.getUom();
        }
    }

    public void setUom(Uom uom) {
        this.uom = uom;
    }

    @Override
    public SymbolizerNode getParent() {
        return parent;
    }

    @Override
    public void setParent(SymbolizerNode node) {
        parent = node;
    }

    public StyledLabel getLabel() {
        return label;
    }

    public void setLabel(StyledLabel label) {
        this.label = label;
        label.setParent(this);
    }

    public abstract void draw(Graphics2D g2, Shape shp, DataSource ds, long fid, boolean selected) throws ParameterException, IOException;

    public abstract JAXBElement<? extends LabelType> getJAXBElement();
    protected SymbolizerNode parent;
    protected Uom uom;
    protected StyledLabel label;
    protected HorizontalAlignment hAlign;
    protected VerticalAlignment vAlign;
}
