/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.orbisgis.core.renderer.se;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Point;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.bind.JAXBElement;
import org.orbisgis.core.renderer.persistance.se.SymbolizerType;

import org.gdms.data.feature.Feature;
import org.gdms.driver.DriverException;
import org.orbisgis.core.map.MapTransform;

import org.orbisgis.core.renderer.se.common.Uom;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.transform.Transform;

/**
 * This class contains common element shared by Point,Line,Area 
 * and Text Symbolizer. Those vector layers all contains :
 *   - an unit of measure (Uom)
 *   - an affine transformation def (transform)
 *
 * @author maxence
 */
public abstract class VectorSymbolizer extends Symbolizer implements UomNode {

	protected Transform transform;
	protected Uom uom;

	protected VectorSymbolizer() {
	}

	protected VectorSymbolizer(JAXBElement<? extends SymbolizerType> st) {
		super(st);
	}

	@Override
	public abstract void draw(Graphics2D g2, Feature feat, boolean selected, MapTransform mt)
			throws ParameterException, IOException, DriverException;

	/**
	 * Convert a spatial feature into a LiteShape, should add parameters to handle
	 * the scale and to perform a scale dependent generalization !
	 *
	 * @param sds the data source
	 * @param fid the feature id
	 * @throws ParameterException
	 * @throws IOException
	 * @throws DriverException
	 */
	public ArrayList<Shape> getShape(Feature feat, MapTransform mt) throws ParameterException, IOException, DriverException {

		Geometry geom = this.getTheGeom(feat); // geom + function

		ArrayList<Shape> shapes = new ArrayList<Shape>();

		ArrayList<Geometry> geom2Process = new ArrayList<Geometry>();

		geom2Process.add(geom);

		while (!geom2Process.isEmpty()) {
			geom = geom2Process.remove(0);
			if (geom instanceof GeometryCollection) {
				for (int i = 0; i < geom.getNumGeometries(); i++) {
					geom2Process.add(geom.getGeometryN(i));
				}
			} else {
				Shape shape = mt.getShape(geom);

				if (transform != null) {
					System.out.println (transform.getGraphicalAffineTransform(feat, false, mt, (double)mt.getWidth(), (double)mt.getHeight()));
					shape = transform.getGraphicalAffineTransform(feat, false, mt, (double)mt.getWidth(),(double) mt.getHeight()).createTransformedShape(shape); // TODO widht and height?
				}
				shapes.add(shape);
			}
		}

		//Rectangle2D bounds2D = shape.getBounds2D();

		/*
		if (bounds2D.getHeight() + bounds2D.getWidth() < 5){
		return null;
		}
		 */
		return shapes;
	}

	public Point2D getPointShape(Feature feat, MapTransform mt) throws ParameterException, IOException, DriverException {
		Geometry geom = this.getTheGeom(feat); // geom + function


		AffineTransform at = mt.getAffineTransform();

		System.out.println ("Transform : " + transform);
		if (transform != null) {
			System.out.println ("Extent : " + mt.getAdjustedExtent());
			at.preConcatenate(transform.getGraphicalAffineTransform(feat, false, mt, (double)mt.getWidth(), (double)mt.getHeight()));
		}

		Point point = geom.getInteriorPoint();
		//Point point = geom.getCentroid();

		return at.transform(new Point2D.Double(point.getX(), point.getY()), null);
	}

	public Point2D getFirstPointShape(Feature feat, MapTransform mt) throws ParameterException, IOException, DriverException {
		Geometry geom = this.getTheGeom(feat); // geom + function

		AffineTransform at = mt.getAffineTransform();
		if (transform != null) {
			at.preConcatenate(transform.getGraphicalAffineTransform(feat, false, mt, (double)mt.getWidth(), (double)mt.getHeight())); // TODO width and height
		}

		Coordinate[] coordinates = geom.getCoordinates();

		return at.transform(new Point2D.Double(coordinates[0].x, coordinates[0].y), null);
	}

	public Transform getTransform() {
		return transform;
	}

	@Override
	public Uom getUom() {
		return uom;
	}

	@Override
	public Uom getOwnUom() {
		return uom;
	}

	@Override
	public void setUom(Uom uom) {
		if (uom != null) {
			this.uom = uom;
		} else {
			this.uom = Uom.MM;
		}
	}

	public void setTransform(Transform transform) {
		this.transform = transform;
		transform.setParent(this);
	}
}
