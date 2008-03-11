/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at french IRSTV institute and is able
 * to manipulate and create vectorial and raster spatial information. OrbisGIS
 * is distributed under GPL 3 license. It is produced  by the geomatic team of
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
 *    <http://listes.cru.fr/sympa/info/orbisgis-developers/>
 *    <http://listes.cru.fr/sympa/info/orbisgis-users/>
 *
 * or contact directly:
 *    erwan.bocher _at_ ec-nantes.fr
 *    fergonco _at_ gmail.com
 *    thomas.leduc _at_ cerma.archi.fr
 */
/**
 *
 */
package org.orbisgis.geoview;

import java.awt.Component;
import java.io.ObjectInputStream;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;

class ViewDecorator {
	private String id;
	private String title;
	private String icon;
	private IView view;
	private View dockingView;
	private GeoView2D geoview;
	private Component component;

	public ViewDecorator(IView view, String id, String title, String icon,
			GeoView2D geoview) {
		super();
		this.view = view;
		this.id = id;
		this.title = title;
		this.icon = icon;
		this.geoview = geoview;
	}

	public IView getView() {
		return view;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getIcon() {
		return icon;
	}

	public View getDockingView() {
		return dockingView;
	}

	public void close() {
		if (isOpen()) {
			dockingView.close();
		}
	}

	public void loadStatus(ObjectInputStream ois) {
		view.loadStatus(ois);
		component = view.getComponent(geoview);
		dockingView = new View(title, getImageIcon(), component);
	}

	public void open(RootWindow root) {
		if (dockingView == null) {
			component = view.getComponent(geoview);
			dockingView = new View(title, getImageIcon(), component);
			TabWindow tab = (TabWindow) findWindow(root, TabWindow.class);
			if (tab != null) {
				tab.addTab(dockingView);
			} else {
				View view = (View) findWindow(root, View.class);
				if (view == null) {
					root.setWindow(dockingView);
				} else {
					DockingWindow parent = view.getWindowParent();
					tab = new TabWindow();
					tab.addTab(view);
					tab.addTab(dockingView);
					parent.replaceChildWindow(view, tab);
				}
			}
		} else {
			if (!isOpen()) {
				getDockingView().restore();
				if (!isOpen()) {
					dockingView = null;
					open(root);
				}
			}
		}
	}

	private DockingWindow findWindow(DockingWindow wnd,
			Class<? extends DockingWindow> clazz) {
		if (wnd.getClass().equals(clazz)) {
			return wnd;
		} else {
			for (int i = 0; i < wnd.getChildWindowCount(); i++) {
				DockingWindow ret = findWindow(wnd.getChildWindow(i), clazz);
				if (ret != null) {
					return ret;
				}
			}

			return null;
		}
	}

	private Icon getImageIcon() {
		if (icon != null) {
			URL url = ViewDecorator.class.getResource(icon);
			if (url != null) {
				return new ImageIcon(url);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public Component getViewComponent() {
		return component;
	}

	public boolean isOpen() {
		if (dockingView == null) {
			return false;
		} else {
			return getParent(dockingView) instanceof RootWindow;
		}
	}

	private DockingWindow getParent(DockingWindow window) {
		DockingWindow parent = window.getWindowParent();
		if (parent == null) {
			return window;
		} else {
			return getParent(parent);
		}
	}

}