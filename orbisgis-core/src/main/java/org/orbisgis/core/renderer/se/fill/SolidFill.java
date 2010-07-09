package org.orbisgis.core.renderer.se.fill;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.xml.bind.JAXBElement;
import org.orbisgis.core.renderer.persistance.se.ObjectFactory;
import org.orbisgis.core.renderer.persistance.se.SolidFillType;
import org.gdms.data.DataSource;
import org.gdms.data.feature.Feature;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.SeParameterFactory;
import org.orbisgis.core.renderer.se.parameter.color.ColorHelper;
import org.orbisgis.core.renderer.se.parameter.color.ColorLiteral;
import org.orbisgis.core.renderer.se.parameter.color.ColorParameter;
import org.orbisgis.core.renderer.se.parameter.real.RealLiteral;
import org.orbisgis.core.renderer.se.parameter.real.RealParameter;

/**
 * A solid fill fills a shape with a solid color (+opacity)
 *
 * @author maxence
 */
public final class SolidFill extends Fill {

    /**
     * fill with random color 60% opaque
     */
    public SolidFill() {
        this(new ColorLiteral(), new RealLiteral(60.0));
    }

    /**
     * fill with specified color 60% opaque
     * @param c 
     */
    public SolidFill(Color c) {
        this(new ColorLiteral(c), new RealLiteral(60.0));
    }

    /**
     * fill with specified color and opacity
     * @param c
     * @param opacity
     */
    public SolidFill(Color c, double opacity) {
        this(new ColorLiteral(c), new RealLiteral(opacity));
    }

    /**
     * fill with specified color and opacity
     * @param c
     * @param opacity
     */
    public SolidFill(ColorParameter c, RealParameter opacity) {
		this.setColor(c);
		this.setOpacity(opacity);
    }

    public SolidFill(JAXBElement<SolidFillType> sf) {
        if (sf.getValue().getColor() != null) {
            setColor(SeParameterFactory.createColorParameter(sf.getValue().getColor()));
        }

        if (sf.getValue().getOpacity() != null) {
            setOpacity(SeParameterFactory.createRealParameter(sf.getValue().getOpacity()));
        }
    }

    public void setColor(ColorParameter color) {
        this.color = color;
    }

    public ColorParameter getColor() {
        return color;
    }

    public void setOpacity(RealParameter opacity) {
        this.opacity = opacity;

		if (opacity != null){
			this.opacity.setMinValue(0.0);
			this.opacity.setMaxValue(100.0);
		}
    }

    public RealParameter getOpacity() {
        return opacity;
    }

    @Override
    public void draw(Graphics2D g2, Shape shp, Feature feat, boolean selected, MapTransform mt) throws ParameterException {
        if (color != null) {
            Color c = color.getColor(feat);
            Double op = 100.0;

            if (this.opacity != null) {
                op = this.opacity.getValue(feat);
            }

            // Add opacity to the color 
            Color ac = ColorHelper.getColorWithAlpha(c, op);


            if (selected) {
                ac = ColorHelper.invert(ac);
            }


            g2.setColor(ac);
            g2.fill(shp);
        }
    }

    @Override
    public String toString() {
        return "Color: " + color + " alpha: " + opacity;
    }


    @Override
    public boolean dependsOnFeature() {
        if (color != null && this.color.dependsOnFeature())
            return true;
        if (opacity != null && this.opacity.dependsOnFeature())
            return true;
        return false;
    }

    @Override
    public SolidFillType getJAXBType() {
        SolidFillType f = new SolidFillType();

        if (color != null) {
            f.setColor(color.getJAXBParameterValueType());
        }
        if (opacity != null) {
            f.setOpacity(opacity.getJAXBParameterValueType());
        }

        return f;
    }

    @Override
    public JAXBElement<SolidFillType> getJAXBElement() {
        ObjectFactory of = new ObjectFactory();
        return of.createSolidFill(this.getJAXBType());
    }
    private ColorParameter color;
    private RealParameter opacity;

}
