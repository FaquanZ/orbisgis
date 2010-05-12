/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.orbisgis.core.renderer.se.label;

import java.awt.Graphics2D;
import java.io.IOException;

import javax.media.jai.RenderableGraphics;
import javax.xml.bind.JAXBElement;

import org.gdms.data.DataSource;

import org.orbisgis.core.renderer.liteShape.LiteShape;
import org.orbisgis.core.renderer.persistance.se.LineLabelType;
import org.orbisgis.core.renderer.persistance.se.ObjectFactory;
import org.orbisgis.core.renderer.persistance.se.ParameterValueType;
import org.orbisgis.core.renderer.se.parameter.ParameterException;

/**
 *
 * @author maxence
 * @todo implements
 */
public class LineLabel extends Label {

    /**
     *
     */
    @Override
    public void draw(Graphics2D g2, LiteShape shp, DataSource ds, long fid) throws ParameterException, IOException {
        
        RenderableGraphics l = this.label.getImage(ds, fid);

        // convert lineShape to a point
        // create AT according to rotation and exclusionZone

        /*g2.drawImage(label,
                     new AffineTransformOp(AT,
                                           AffineTransformOp.TYPE_BICUBIC),
                      -label.getWidth() / 2,
                      -label.getHeight() / 2);

         */

    }


    @Override
    public JAXBElement<LineLabelType> getJAXBInstance() {
        LineLabelType ll = new LineLabelType();

        if (uom != null) {
            ll.setUnitOfMeasure(uom.toString());
        }

        if (hAlign != null) {
            ParameterValueType h = new ParameterValueType();
            h.getContent().add(hAlign.toString());
            ll.setHorizontalAlignment(h);
        }

        if (hAlign != null) {
            ParameterValueType v = new ParameterValueType();
            v.getContent().add(vAlign.toString());
            ll.setHorizontalAlignment(v);
        }

        if (label != null) {
            ll.setStyledLabel(label.getJAXBType());
        }

        ObjectFactory of = new ObjectFactory();

        return of.createLineLabel(ll);
    }


}
