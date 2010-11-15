package org.orbisgis.core.renderer.se.parameter.color;

import java.awt.Color;
import javax.xml.bind.JAXBElement;
import org.gdms.data.DataSource;
import org.gdms.data.feature.Feature;
import org.orbisgis.core.renderer.persistance.ogc.PropertyNameType;
import org.orbisgis.core.renderer.se.parameter.ParameterException;

import org.orbisgis.core.renderer.se.parameter.PropertyName;

public class ColorAttribute extends PropertyName implements ColorParameter {

    public ColorAttribute(String fieldName) {
        super(fieldName);
    }

    public ColorAttribute(JAXBElement<PropertyNameType> expr) {
        super(expr);
    }

    @Override
    public Color getColor(Feature feat) throws ParameterException {
        try {
            return Color.getColor(getFieldValue(feat).getAsString());
        } catch (Exception e) {
            throw new ParameterException("Could not fetch feature attribute \"" + fieldName + "\"");
        }
    }
}
