package org.orbisgis.core.renderer.se.graphic;

import java.io.IOException;
import javax.media.jai.RenderableGraphics;
import javax.xml.bind.JAXBElement;
import org.gdms.data.DataSource;
import org.gdms.data.feature.Feature;
import org.orbisgis.core.renderer.persistance.se.AxisChartType;
import org.orbisgis.core.renderer.persistance.se.NormalizeType;
import org.orbisgis.core.renderer.persistance.se.ObjectFactory;
import org.orbisgis.core.renderer.persistance.se.PolarChartType;
import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.core.renderer.se.fill.Fill;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.SeParameterFactory;
import org.orbisgis.core.renderer.se.parameter.real.RealParameter;
import org.orbisgis.core.renderer.se.stroke.Stroke;
import org.orbisgis.core.renderer.se.transform.Transform;

/**
 *
 * @author maxence
 * @todo Implements drawGraphic
 */
public class AxisChart extends Graphic {

    public AxisChart(){
        
    }

    AxisChart(JAXBElement<AxisChartType> chartE) {
        this();
        AxisChartType t = chartE.getValue();

        if (t.getUnitOfMeasure() != null) {
            this.setUom(Uom.fromOgcURN(t.getUnitOfMeasure()));
        }

        if (t.getTransform() != null) {
            this.setTransform(new Transform(t.getTransform()));
        }

        this.setNormalizedToPercent(t.getNormalize() != null);

        this.setAxisType(t.getPolarChart() != null);


        if (t.getCategoryWidth() != null) {
            this.setCategoryWidth(SeParameterFactory.createRealParameter(t.getCategoryWidth()));
        }

        if (t.getCategoryGap() != null) {
            this.setCategoryGap(SeParameterFactory.createRealParameter(t.getCategoryGap()));
        }

        if (t.getFill() != null) {
            this.setAreaFill(Fill.createFromJAXBElement(t.getFill()));
        }

        if (t.getStroke() != null) {
            this.setLineStroke(Stroke.createFromJAXBElement(t.getStroke()));
        }

        if (t.getAxisScale() != null){
            this.setAxisScale(new AxisScale(t.getAxisScale()));
        }
    }

    public Fill getAreaFill() {
        return areaFill;
    }

    public void setAreaFill(Fill areaFill) {
        this.areaFill = areaFill;
        areaFill.setParent(this);
    }

    public AxisScale getAxisScale() {
        return axisScale;
    }

    public void setAxisScale(AxisScale axisScale) {
        this.axisScale = axisScale;
    }

    public boolean isPolarChart() {
        return isPolarChart;
    }

    public void switchToPolarChart(){
        isPolarChart = true;
    }

    
    public void switchToAxisChart(){
        isPolarChart = true;
    }

    public void setAxisType(boolean isPolar){
        this.isPolarChart = isPolar;
    }


    public RealParameter getCategoryGap() {
        return categoryGap;
    }

    public void setCategoryGap(RealParameter categoryGap) {
        this.categoryGap = categoryGap;
    }

    public RealParameter getCategoryWidth() {
        return categoryWidth;
    }

    public void setCategoryWidth(RealParameter categoryWidth) {
        this.categoryWidth = categoryWidth;
    }

    public Stroke getLineStroke() {
        return lineStroke;
    }

    public void setLineStroke(Stroke lineStroke) {
        this.lineStroke = lineStroke;
        lineStroke.setParent(this);
    }

    public boolean isNormalizedToPercent() {
        return normalizeToPercent;
    }

    public void setNormalizedToPercent(boolean normalizedToPercent) {
        this.normalizeToPercent = normalizedToPercent;
    }

    @Override
    public void updateGraphic() {   
    }

    @Override
    public RenderableGraphics getRenderableGraphics(Feature feat, boolean selected) throws ParameterException, IOException {
        return null; // TODO implements
    }

    @Override
    public double getMaxWidth(Feature feat) throws ParameterException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JAXBElement<AxisChartType> getJAXBElement() {
        AxisChartType a = new AxisChartType();

        if (axisScale != null) {
            a.setAxisScale(axisScale.getJAXBType());
        }

        if (categoryGap != null) {
            a.setCategoryGap(categoryGap.getJAXBParameterValueType());
        }

        if (categoryWidth != null) {
            a.setCategoryWidth(categoryWidth.getJAXBParameterValueType());
        }

        if (areaFill != null) {
            a.setFill(areaFill.getJAXBElement());
        }

        if (normalizeToPercent) {
            a.setNormalize(new NormalizeType());
        }

        if (this.isPolarChart) {
            a.setPolarChart(new PolarChartType());
        }

        if (lineStroke != null) {
            a.setStroke(lineStroke.getJAXBElement());
        }

        if (transform != null) {
            a.setTransform(transform.getJAXBType());
        }

        if (uom != null) {
            a.setUnitOfMeasure(uom.toString());
        }

        ObjectFactory of = new ObjectFactory();
        return of.createAxisChart(a);
    }


    @Override
    public boolean dependsOnFeature() {
        return true;
    }


    private boolean normalizeToPercent;
    private boolean isPolarChart;
    private AxisScale axisScale;
    private RealParameter categoryWidth;
    private RealParameter categoryGap;
    private Fill areaFill;
    private Stroke lineStroke;

    // TODO  Other style parameters.... to be defined
    //
    // TODO Add stacked bars
}
