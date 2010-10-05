/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 *
 *  Team leader Erwan BOCHER, scientific researcher,
 *
 *  User support leader : Gwendall Petit, geomatic engineer.
 *
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * Copyright (C) 2010 Erwan BOCHER, Pierre-Yves FADET, Alexis GUEGANNO, Maxence LAURENT
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
 *
 * or contact directly:
 * erwan.bocher _at_ ec-nantes.fr
 * gwendall.petit _at_ ec-nantes.fr
 */



package org.orbisgis.core.ui.editorViews.toc.actions.cui.stroke;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.orbisgis.core.images.OrbisGISIcon;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.color.ColorParameter;
import org.orbisgis.core.renderer.se.parameter.real.RealParameter;
import org.orbisgis.core.renderer.se.parameter.string.StringLiteral;
import org.orbisgis.core.renderer.se.stroke.PenStroke;
import org.orbisgis.core.renderer.se.stroke.PenStroke.LineCap;
import org.orbisgis.core.renderer.se.stroke.PenStroke.LineJoin;
import org.orbisgis.core.renderer.se.stroke.Stroke;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.LegendUIAbstractPanel;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.LegendUIComponent;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.LegendUIController;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.components.ComboBoxInput;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.components.TextInput;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.components.UomInput;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.parameter.color.LegendUIMetaColorPanel;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.parameter.real.LegendUIMetaRealPanel;

/**
 *
 * @author maxence
 */
public class LegendUIPenStrokePanel extends LegendUIComponent implements LegendUIStrokeComponent{
	private final PenStroke penStroke;
	private final LegendUIMetaColorPanel color;
	private final LegendUIMetaRealPanel strokeWidth;

	private final LegendUIMetaRealPanel opacity;

	private final LegendUIMetaRealPanel dashOffset;


	private final LineCap[] lCapValues;
	private final LineJoin[] lJoinValues;

	private LegendUIAbstractPanel content;
	private LegendUIAbstractPanel content2;
	private LegendUIAbstractPanel toolbar;

	private UomInput uom;

	private ComboBoxInput lineCap;
	private ComboBoxInput lineJoin;

	private TextInput dashArray;


	public LegendUIPenStrokePanel(LegendUIController controller, LegendUIComponent parent, PenStroke pStroke) {
		super("pen stroke", controller, parent, 0);
		//this.setLayout(new GridLayout(0,2));
		this.toolbar = new LegendUIAbstractPanel(controller);
		this.penStroke = pStroke;

		this.color = new LegendUIMetaColorPanel("color", controller, this, penStroke.getColor()) {

			@Override
			public void colorChanged(ColorParameter newColor) {
				penStroke.setColor(newColor);
			}
		};
		color.init();

		this.opacity = new LegendUIMetaRealPanel("opacity", controller, this, penStroke.getOpacity()) {

			@Override
			public void realChanged(RealParameter newReal) {
				penStroke.setOpacity(newReal);
			}
		};
		this.opacity.init();

		this.strokeWidth = new LegendUIMetaRealPanel("width", controller, this, penStroke.getWidth()) {

			@Override
			public void realChanged(RealParameter newReal) {
				penStroke.setWidth(newReal);
			}
		};
		strokeWidth.init();

		this.dashOffset = new LegendUIMetaRealPanel("dash offset", controller, this, penStroke.getWidth()) {

			@Override
			public void realChanged(RealParameter newReal) {
				penStroke.setDashOffset(newReal);
			}
		};
		dashOffset.init();
		try {
			dashArray = new TextInput("DashArray", penStroke.getDashArray().getValue(null), 10) {

				@Override
				protected void valueChanged(String s) {
					StringLiteral l = (StringLiteral) penStroke.getDashArray();
					l.setValue(s);
				}
			};
		} catch (ParameterException ex) {
			Logger.getLogger(LegendUIPenStrokePanel.class.getName()).log(Level.SEVERE, null, ex);
		}

		//penStroke.setDashArray(StringParameter);

		lCapValues = LineCap.values();
		lJoinValues = LineJoin.values();

		lineCap = new ComboBoxInput(lCapValues, penStroke.getLineCap().ordinal()) {

			@Override
			protected void valueChanged(int i) {
				penStroke.setLineCap(lCapValues[i]);
			}
		};


		lineJoin = new ComboBoxInput(lJoinValues, penStroke.getLineJoin().ordinal()) {

			@Override
			protected void valueChanged(int i) {
				penStroke.setLineJoin(lJoinValues[i]);
			}
		};

		uom = new UomInput(pStroke);

		this.content = new LegendUIAbstractPanel(controller);
		this.content2 = new LegendUIAbstractPanel(controller);
	}

	@Override
	public Icon getIcon() {
		return OrbisGISIcon.PENCIL;
	}

	@Override
	protected void mountComponent() {
		toolbar.removeAll();
		toolbar.add(uom, BorderLayout.WEST);
		toolbar.add(lineCap, BorderLayout.CENTER);
		toolbar.add(lineJoin, BorderLayout.EAST);

		this.add(toolbar, BorderLayout.NORTH);

		content.removeAll();

		content.add(color, BorderLayout.NORTH);
		content.add(strokeWidth, BorderLayout.CENTER);
		content.add(opacity, BorderLayout.SOUTH);

		content2.removeAll();
		content2.add(content, BorderLayout.NORTH);
		content2.add(dashArray, BorderLayout.CENTER);
		content2.add(dashOffset, BorderLayout.SOUTH);

		this.add(content2, BorderLayout.SOUTH);
	}

	@Override
	public Stroke getStroke() {
		return this.penStroke;
	}
}
