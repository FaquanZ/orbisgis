/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able
 * to manipulate and create vector and raster spatial information. OrbisGIS
 * is distributed under GPL 3 license. It is produced  by the geo-informatic team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
 *    Erwan BOCHER, scientific researcher,
 *    Thomas LEDUC, scientific researcher,
 *    Fernando GONZALEZ CORTES, computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OrbisGIS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult:
 *    <http://orbisgis.cerma.archi.fr/>
 *    <http://sourcesup.cru.fr/projects/orbisgis/>
 *
 * or contact directly:
 *    erwan.bocher _at_ ec-nantes.fr
 *    fergonco _at_ gmail.com
 *    thomas.leduc _at_ cerma.archi.fr
 */
package org.orbisgis.core.renderer.symbol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;

import org.gdms.driver.DriverException;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.renderer.RenderPermission;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class LineSymbol extends AbstractLineSymbol {

	private int drawingSize;

	LineSymbol(Color outline, int lineWidth, boolean mapUnits) {
		super(outline, lineWidth, mapUnits);
	}

	public Envelope draw(Graphics2D g, Geometry geom, MapTransform mt,
			RenderPermission permission) throws DriverException {
		Shape ls = mt.getShape(geom, true);

		drawingSize = lineWidth;
		if (mapUnits) {
			try {
				drawingSize = (int) toPixelUnits(lineWidth, mt
						.getAffineTransform());
			} catch (NoninvertibleTransformException e) {
				throw new DriverException("Cannot convert to map units", e);
			}
		}

		g.setStroke(new BasicStroke(drawingSize, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		g.setColor(outline);
		g.setPaint(null);
		g.draw(ls);

		return null;
	}

	public String getClassName() {
		return "Line";
	}

	public StandardSymbol cloneSymbol() {
		return new LineSymbol(outline, lineWidth, mapUnits);
	}

	public String getId() {
		return "org.orbisgis.symbol.Line";
	}

	@Override
	public Symbol deriveSymbol(Color color) {
		return new LineSymbol(color, lineWidth, mapUnits);
	}

}
