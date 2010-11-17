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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.orbisgis.core.ui.preferences.lookandfeel.OrbisGISIcon;
import org.orbisgis.core.renderer.se.Rule;

/**
 *
 * @author maxence
 */
public class LegendUIRuleListPanel extends JPanel implements LegendUIComponentListener {
	
	private final JList list;
    private DefaultListModel model;

	private JButton btnUp;
	private JButton btnDown;
	private JButton btnAdd;
	private JButton btnRm;
	private LegendUIController controller;

	private JPanel tools;



	public LegendUIRuleListPanel(final LegendUIController ctrl, ArrayList<LegendUIRulePanel> rulesUI){
		super(new BorderLayout());
		this.controller = ctrl;

		// Make this accessible from inner classes !(ugly hack)
		model = new DefaultListModel();

		model.addElement("General");

		// First step: create the list model
		for (LegendUIRulePanel r : rulesUI) {
			model.addElement(r);
			r.register(this);
		}

		list = new JList(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		this.add(list, BorderLayout.NORTH);
		this.setBorder(BorderFactory.createTitledBorder("Rules"));


		btnRm = new JButton(OrbisGISIcon.REMOVE);
		btnAdd = new JButton(OrbisGISIcon.ADD);
		btnDown = new JButton(OrbisGISIcon.GO_DOWN);
		btnUp = new JButton(OrbisGISIcon.GO_UP);

		btnRm.setMargin(new Insets(0, 0, 0, 0));
		btnAdd.setMargin(new Insets(0, 0, 0, 0));
		btnUp.setMargin(new Insets(0, 0, 0, 0));
		btnDown.setMargin(new Insets(0, 0, 0, 0));



		btnUp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int i = list.getSelectedIndex() - 1;
				if (controller.moveRuleUp(i)){
					// reflect the new order in the UI
					DefaultListModel model = (DefaultListModel) list.getModel();
					Rule r = (Rule) model.remove(i);
					model.add(i - 1, r);
					list.setSelectedIndex(i - 1);
				}
			}
		});

		btnDown.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int i = list.getSelectedIndex() - 1;
				if (controller.moveRuleDown(i)){
					// reflect the new order in the UI
					DefaultListModel model = (DefaultListModel) list.getModel();
					Rule r = (Rule) model.remove(i);
					model.add(i + 1, r);
					list.setSelectedIndex(i + 1);
				}
			}
		});

		btnAdd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Rule r = controller.createNewRule();

				DefaultListModel model = (DefaultListModel) list.getModel();
				model.addElement(r);
				int index = model.getSize() -2;

				list.setSelectedIndex(index);

				controller.getRulePanel(index).register(LegendUIRuleListPanel.this);
				controller.editRule(index);
			}
		});

		btnRm.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int confirmation = JOptionPane.showConfirmDialog(controller.getMainPanel(), "Sur ?", "Sur ?", JOptionPane.YES_NO_OPTION);

				if (confirmation == 0) {
					int i = list.getSelectedIndex() -1;
					if (controller.deleteRule(i)){
						DefaultListModel model = (DefaultListModel) list.getModel();
						model.remove(i);
						list.setSelectedIndex(-1);
					}
				}
			}
		});

		btnUp.setEnabled(false);
		btnDown.setEnabled(false);
		btnRm.setEnabled(false);

		tools = new JPanel();

		tools.add(btnUp);
		tools.add(btnAdd);
		tools.add(btnRm);
		tools.add(btnDown);

		this.add(tools, BorderLayout.SOUTH);


		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e){
				int index = list.locationToIndex(e.getPoint());
				list.setSelectedIndex(index);


				btnUp.setEnabled(false);
				btnDown.setEnabled(false);
				btnRm.setEnabled(false);

				if (index > 0) {
					// First, update button status
					if (list.getModel().getSize() > 2){ //dont delete the last rule
						btnRm.setEnabled(true);
					}

					if (index > 1) {
						// Turn the move down button on
						btnUp.setEnabled(true);
					}
					if (index < list.getModel().getSize() - 1) {
						btnDown.setEnabled(true);
					}

				}

				controller.editRule(index - 1);
			}
		});


	}

	@Override
	public void nameChanged() {
		list.updateUI();
	}
}
