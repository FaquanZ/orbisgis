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
package org.orbisgis.core.ui.plugins.editors.tableEditor;

import javax.swing.JButton;

import org.gdms.data.DataSource;
import org.gdms.driver.DriverException;
import org.orbisgis.core.ui.editor.IEditor;
import org.orbisgis.core.ui.pluginSystem.AbstractPlugIn;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.pluginSystem.message.ErrorMessages;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
import org.orbisgis.core.ui.preferences.lookandfeel.OrbisGISIcon;
import org.orbisgis.utils.I18N;

public class UndoTablePlugIn extends AbstractPlugIn {

	private JButton btn;

	public UndoTablePlugIn() {
		btn = new JButton(OrbisGISIcon.UNDO_ICON);
		btn.setToolTipText(I18N.getString("orbisgis.org.orbisgis.core.ui.plugins.editors.tableEditor.undo"));
	}

	@Override
	public boolean execute(PlugInContext context) throws Exception {
		IEditor editor = context.getActiveEditor();
		DataSource dataSource = (DataSource) editor.getElement().getObject();
		try {
			dataSource.undo();
		} catch (DriverException e) {
			ErrorMessages.error(ErrorMessages.CannotUndo, e);
		}
		return true;
	}

	@Override
	public void initialize(PlugInContext context) throws Exception {
		WorkbenchContext wbcontext = context.getWorkbenchContext();
		wbcontext.getWorkbench().getFrame().getEditionTableToolBar().addPlugIn(
				this, btn, context);
	}

	public boolean isEnabled() {
		boolean isEnabled = false;
		IEditor editor = null;
		if((editor=getPlugInContext().getTableEditor()) != null){
			DataSource dataSource = (DataSource) editor.getElement()
					.getObject();
			isEnabled =  dataSource.canUndo();
		}
		btn.setEnabled(isEnabled);
		return isEnabled;
	}
}
