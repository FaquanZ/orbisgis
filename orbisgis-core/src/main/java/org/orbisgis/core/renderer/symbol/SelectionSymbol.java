/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.core.renderer.symbol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.PathIterator;

import org.gdms.driver.DriverException;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.renderer.RenderContext;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.gdms.data.types.Type;

public class SelectionSymbol extends AbstractGeometrySymbol {

	private Stroke handleStroke = new BasicStroke(1);
	private Stroke lineStroke = new BasicStroke(2);
	private Stroke fillStroke = new BasicStroke(1);
	private Color handleFillColor;
	private Color lineColor;
	private Color fillColor;
	private boolean paintingHandles;
	private boolean filling;

	public SelectionSymbol(Color color, boolean paintingHandles, boolean filling) {
		handleFillColor = color;
		lineColor = color;
		fillColor = color;
		this.paintingHandles = paintingHandles;
		this.filling = filling;
	}

	@Override
	protected boolean willDrawSimpleGeometry(Geometry geom) {
		return true;
	}

	@Override
	public boolean acceptGeometryType(Type geomType) {
		return true;
	}

	@Override
	public Symbol cloneSymbol() {

		return null;
	}

	@Override
	public Symbol deriveSymbol(Color color) {

		return null;
	}

	@Override
	public Envelope draw(Graphics2D g, Geometry geom, MapTransform mt,
			RenderContext permission) throws DriverException {

		if ((geom.getDimension() == 1) || (!paintingHandles)) {
			SymbolUtil.paint(geom, g, mt, false, fillStroke, fillColor, true,
					lineStroke, lineColor);
		}

		else if ((geom.getDimension() == 2) || (!paintingHandles)) {
			SymbolUtil.paint(geom, g, mt, filling, fillStroke, new Color(
					fillColor.getRed(), fillColor.getGreen(), fillColor
							.getBlue(), 50), true, lineStroke, lineColor);
		}

		if (paintingHandles) {
			// LiteShape ls = new LiteShape(geom, at, false);
			Shape ls = mt.getShapeWriter().toShape(geom);
			PathIterator pi = ls.getPathIterator(null);
			double[] coords = new double[6];

			int drawingSize = 5;

			while (!pi.isDone()) {
				pi.currentSegment(coords);
				paintSquare(g, (int) coords[0], (int) coords[1], drawingSize);
				pi.next();
			}
		}
		return null;
	}

	protected void paintSquare(Graphics2D g, int x, int y, int size) {
		x = x - size / 2;
		y = y - size / 2;
		if (fillColor != null) {
			g.setPaint(new Color(fillColor.getRed(), fillColor.getGreen(),
					fillColor.getBlue(), 50));
			g.fillRect(x, y, size, size);
		}
		if (lineColor != null) {
			g.setStroke(new BasicStroke(1));
			g.setColor(lineColor);
			g.drawRect(x, y, size, size);
		}
	}

	@Override
	public String getClassName() {
		return "Selection";
	}

	@Override
	public String getId() {
		return "org.orbisgis.symbol.SelectionSymbol";
	}

}
