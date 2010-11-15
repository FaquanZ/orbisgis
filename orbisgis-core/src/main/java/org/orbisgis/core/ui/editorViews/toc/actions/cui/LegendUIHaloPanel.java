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
package org.orbisgis.core.ui.editorViews.toc.actions.cui;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import org.orbisgis.core.images.OrbisGISIcon;
import org.orbisgis.core.renderer.se.common.Halo;
import org.orbisgis.core.renderer.se.parameter.real.RealParameter;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.components.UomInput;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.fill.LegendUIMetaFillPanel;
import org.orbisgis.core.ui.editorViews.toc.actions.cui.parameter.real.LegendUIMetaRealPanel;

/**
 *
 * @author maxence
 */
public abstract class LegendUIHaloPanel extends LegendUIComponent {

	protected Halo halo;
	private UomInput uomInput;
	private LegendUIMetaRealPanel radius;
	private LegendUIMetaFillPanel fill;

	public LegendUIHaloPanel(LegendUIController ctrl, LegendUIComponent parent, Halo h) {
		super("halo", ctrl, parent, 0, true);

		this.setBorder(BorderFactory.createTitledBorder(getName()));

		this.halo = h;

		if (this.halo == null) {
			this.halo = new Halo();
			this.isNullComponent = true;
		}

		this.radius = new LegendUIMetaRealPanel("Radius", controller, this, halo.getRadius(), false) {

			@Override
			public void realChanged(RealParameter newReal) {
				halo.setRadius(newReal);
			}
		};

		radius.init();

		uomInput = new UomInput(halo);

		fill = new LegendUIMetaFillPanel(controller, this, halo, false);
		fill.init();
	}

	@Override
	public Icon getIcon() {
		return OrbisGISIcon.PALETTE;
	}

	@Override
	protected void mountComponent() {
		editor.add(uomInput, BorderLayout.EAST);
		editor.add(radius, BorderLayout.CENTER);
		editor.add(fill, BorderLayout.SOUTH);
	}

	@Override
	public Class getEditedClass() {
		return Halo.class;
	}

	@Override
	protected void turnOff() {
		haloChanged(null);
	}

	@Override
	protected void turnOn() {
		haloChanged(halo);
	}

	protected abstract void haloChanged(Halo halo);
}
