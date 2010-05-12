package org.orbisgis.core.renderer.se.label;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.media.jai.RenderableGraphics;
import org.gdms.data.DataSource;
import org.orbisgis.core.renderer.persistance.se.FontType;
import org.orbisgis.core.renderer.persistance.se.StyledLabelType;
import org.orbisgis.core.renderer.se.SymbolizerNode;
import org.orbisgis.core.renderer.se.common.Halo;
import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.core.renderer.se.fill.Fill;
import org.orbisgis.core.renderer.se.fill.SolidFill;
import org.orbisgis.core.renderer.se.graphic.Graphic;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.color.ColorLiteral;
import org.orbisgis.core.renderer.se.parameter.real.RealLiteral;
import org.orbisgis.core.renderer.se.parameter.real.RealParameter;
import org.orbisgis.core.renderer.se.parameter.string.StringLiteral;
import org.orbisgis.core.renderer.se.parameter.string.StringParameter;
import org.orbisgis.core.renderer.se.stroke.Stroke;

public class StyledLabel implements SymbolizerNode {

    public StyledLabel() {
        this.labelText = new StringLiteral("Label");
        this.fontFamily = new StringLiteral("Arial");
        this.fontWeight = new StringLiteral("Normal");
        this.fontStyle = new StringLiteral("Normal");
        this.fontSize = new RealLiteral(12);

        setFill(new SolidFill(Color.BLACK, 100.0));
        setStroke(null);
    }

    public StyledLabel(String label) {
        this.labelText = new StringLiteral(label);
        this.fontFamily = new StringLiteral("Arial");
        this.fontWeight = new StringLiteral("Normal");
        this.fontStyle = new StringLiteral("Normal");
        this.fontSize = new RealLiteral(12);

        SolidFill f = new SolidFill();
        f.setOpacity(new RealLiteral(100.0));
        f.setColor(new ColorLiteral(Color.black));

        this.setFill(f);
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

    public Fill getFill() {
        return fill;
    }

    public void setFill(Fill fill) {
        this.fill = fill;
        if (fill != null) {
            fill.setParent(this);
        }
    }

    public Halo getHalo() {
        return halo;
    }

    public void setHalo(Halo halo) {
        this.halo = halo;
        if (halo != null) {
            halo.setParent(this);
        }
    }

    public StringParameter getLabelText() {
        return labelText;
    }

    public void setLabelText(StringParameter labelText) {
        this.labelText = labelText;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
        if (stroke != null) {
            stroke.setParent(this);
        }
    }

    public StringParameter getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(StringParameter fontFamily) {
        this.fontFamily = fontFamily;
    }

    public RealParameter getFontSize() {
        return fontSize;
    }

    public void setFontSize(RealParameter fontSize) {
        this.fontSize = fontSize;
    }

    public StringParameter getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(StringParameter fontStyle) {
        this.fontStyle = fontStyle;
    }

    public StringParameter getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(StringParameter fontWeight) {
        this.fontWeight = fontWeight;
    }

    // TODO implements
    public RenderableGraphics getImage(DataSource ds, long fid) throws ParameterException, IOException {

        String text = labelText.getValue(ds, fid);

        String family = fontFamily.getValue(ds, fid);

        // Family is comma delimeted list of fonts family
        // TODO Choose the first available

        String weight = fontWeight.getValue(ds, fid);
        String style = fontStyle.getValue(ds, fid);
        double size = fontSize.getValue(ds, fid);

        int st = Font.PLAIN;

        if (weight.equalsIgnoreCase("bold")) {
            st = Font.BOLD;
        }

        if (style.equalsIgnoreCase("italic")) {
            if (st == Font.PLAIN) {
                st |= Font.ITALIC;
            } else {
                st = Font.ITALIC;
            }
        }


        Font font = new Font(family, st, (int) size);
        FontMetrics metrics = new FontMetrics(font) {
        };
        Rectangle2D bounds = metrics.getStringBounds(text, null);

        RenderableGraphics rg = new RenderableGraphics(bounds);

        TextLayout tl = new TextLayout(text, font, rg.getFontRenderContext());

        double ty;

        ty = -bounds.getMaxY() + bounds.getHeight() / 2.0;

        Shape outline = tl.getOutline(AffineTransform.getTranslateInstance(-bounds.getCenterX(), ty));

        double margin = 0.0;

        if (stroke != null) {
            margin = stroke.getMaxWidth(ds, fid);
        }

        rg = Graphic.getNewRenderableGraphics(outline.getBounds2D(), margin);

        if (fill != null) {
            fill.draw(rg, outline, ds, fid);
        }

        if (stroke != null) {
            stroke.draw(rg, outline, ds, fid);
        }

        // HALO, FILL, STROKE
        return rg;
    }

    public StyledLabelType getJAXBType() {
        StyledLabelType l = new StyledLabelType();

        if (labelText != null) {
            l.setLabelText(labelText.getJAXBParameterValueType());
        }

        if (halo != null) {
            l.setHalo(halo.getJAXBType());
        }

        if (fill != null) {
            l.setFill(fill.getJAXBInstance());
        }

        if (stroke != null) {
            l.setStroke(stroke.getJAXBInstance());
        }

        FontType font = new FontType();
        if (fontFamily != null) {
            font.setFontFamily(fontFamily.getJAXBParameterValueType());
        }

        if (fontWeight != null) {
            font.setFontWeight(fontWeight.getJAXBParameterValueType());
        }

        if (fontSize != null) {
            font.setFontSize(fontSize.getJAXBParameterValueType());
        }

        if (fontStyle != null) {
            font.setFontStyle(fontStyle.getJAXBParameterValueType());
        }

        l.setFont(font);

        return l;
    }
    private SymbolizerNode parent;

    private StringParameter labelText;
    
    private StringParameter fontFamily;
    private StringParameter fontWeight;
    private StringParameter fontStyle;
    private RealParameter fontSize;

    private Stroke stroke;
    private Fill fill;
    private Halo halo;
}
