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
package org.orbisgis.views.geocatalog;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import org.orbisgis.resource.IResource;

public class TransferableResource implements Transferable {

	private static DataFlavor resourceFlavor = new DataFlavor(IResource.class,
			"Resource");

	private IResource[] nodes = null;

	public TransferableResource(IResource[] node) {

		// Delete the nodes contained by other nodes

		ArrayList<IResource> nodes = new ArrayList<IResource>();
		for (int i = 0; i < node.length; i++) {
			if (!contains(nodes, node[i])) {
				removeContained(nodes, node[i]);
				nodes.add(node[i]);
			}
		}
		this.nodes = nodes.toArray(new IResource[0]);
	}

	private boolean contains(ArrayList<IResource> nodes, IResource resource) {
		for (int i = 0; i < nodes.size(); i++) {
			IResource[] subtree = nodes.get(i).getResourcesRecursively();
			for (IResource descendant : subtree) {
				if (descendant == resource) {
					return true;
				}
			}
		}

		return false;
	}

	private void removeContained(ArrayList<IResource> nodes, IResource resource) {
		for (int i = 0; i < nodes.size(); i++) {
			if (resource == nodes.get(i)) {
				nodes.remove(i);
				i--;
			} else {
				IResource[] children = resource.getResources();
				for (IResource child : children) {
					removeContained(nodes, child);
				}
			}
		}
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		Object ret = null;
		if (flavor.equals(resourceFlavor)) {
			ret = nodes;
		} else if (flavor.equals(DataFlavor.stringFlavor)) {
			String retString = "";
			String separator = "";
			for (IResource node : nodes) {
				retString = retString + separator + node.getName();
				separator = ", ";
			}
			ret = retString;
		}

		return ret;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return (new DataFlavor[] { resourceFlavor, DataFlavor.stringFlavor });
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.equals(getResourceFlavor()) || flavor
				.equals(DataFlavor.stringFlavor));
	}

	public static DataFlavor getResourceFlavor() {
		return resourceFlavor;
	}
}
