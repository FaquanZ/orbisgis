/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the geo-informatic team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 * 
 *  
 *  Lead Erwan BOCHER, scientific researcher, 
 *
 *  Developer lead : Pierre-Yves FADET, computer engineer. 
 *  
 *  User support lead : Gwendall Petit, geomatic engineer. 
 * 
 * Previous computer developer : Thomas LEDUC, scientific researcher, Fernando GONZALEZ
 * CORTES, computer engineer.
 * 
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 * 
 * Copyright (C) 2010 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
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
 * For more information, please consult: <http://orbisgis.cerma.archi.fr/>
 * <http://sourcesup.cru.fr/projects/orbisgis/>
 * 
 * or contact directly: 
 * erwan.bocher _at_ ec-nantes.fr 
 * Pierre-Yves.Fadet _at_ ec-nantes.fr
 * gwendall.petit _at_ ec-nantes.fr
 **/

package org.orbisgis.core.ui.plugins.views.sqlConsole.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;

import org.orbisgis.core.Services;
import org.orbisgis.core.background.BackgroundManager;
import org.orbisgis.core.sif.SaveFilePanel;
import org.orbisgis.core.sif.UIFactory;
import org.orbisgis.core.ui.plugins.views.sqlConsole.blockComment.QuoteSQL;
import org.orbisgis.core.ui.plugins.views.sqlConsole.codereformat.CodeReformator;
import org.orbisgis.core.ui.plugins.views.sqlConsole.ui.SQLConsolePanel;

public class SQLConsoleKeyListener extends KeyAdapter {

	private SQLConsolePanel panel;
	private CodeReformator codeReformator;
        private ActionsListener actionsListener;

	public SQLConsoleKeyListener(SQLConsolePanel panel,
			CodeReformator codeReformator, ActionsListener listener) {
		this.panel = panel;
		this.codeReformator = codeReformator;
                this.actionsListener = listener;
	}

	public void keyPressed(KeyEvent e) {
		String originalText = panel.getText();
		if ((e.getKeyCode() == KeyEvent.VK_ENTER) && e.isControlDown()) {
			BackgroundManager bm = (BackgroundManager) Services
					.getService(BackgroundManager.class);
			bm.backgroundOperation(new ExecuteScriptProcess(originalText));

		} else if ((e.getKeyCode() == KeyEvent.VK_S) && e.isControlDown()) {
                    ActionEvent ev = new ActionEvent(this, ConsoleAction.SAVE, String.valueOf(ConsoleAction.SAVE));
                    actionsListener.actionPerformed(ev);
		}
		// Format SQL code
		else if ((e.getKeyCode() == KeyEvent.VK_F) && e.isControlDown()) {
			panel.getScriptPanel().setText(
					codeReformator.reformat(panel.getCurrentSQLStatement()));

		}
		// Quote SQL
		else if ((e.getKeyCode() == KeyEvent.VK_SLASH) && e.isControlDown()) {
			QuoteSQL.quoteSQL(panel, false);

		}
		// Unquote SQL
		else if ((e.getKeyCode() == KeyEvent.VK_BACK_SLASH)
				&& e.isControlDown()) {
			QuoteSQL.unquoteSQL(panel);

		} else if ((e.getKeyCode() == KeyEvent.VK_O) && e.isControlDown()) {
			ActionEvent ev = new ActionEvent(this, ConsoleAction.OPEN, String.valueOf(ConsoleAction.OPEN));
                    actionsListener.actionPerformed(ev);
		}
	}
}