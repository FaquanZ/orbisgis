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
/*
 * JPanelUniqueSymbolLegend.java
 *
 * Created on 27 de febrero de 2008, 18:20
 */

package org.orbisgis.editorViews.toc.actions.cui.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.gdms.data.types.GeometryConstraint;
import org.orbisgis.editorViews.toc.actions.cui.gui.widgets.table.ClassifiedLegendTableModel;
import org.orbisgis.editorViews.toc.actions.cui.gui.widgets.table.SymbolValueCellRenderer;
import org.orbisgis.editorViews.toc.actions.cui.ui.CompositeSymbolFilter;
import org.orbisgis.editorViews.toc.actions.cui.ui.ConstraintSymbolFilter;
import org.orbisgis.editorViews.toc.actions.cui.ui.EditableSymbolFilter;
import org.orbisgis.editorViews.toc.actions.cui.ui.SymbolEditor;
import org.orbisgis.editorViews.toc.actions.cui.ui.SymbolFilter;
import org.orbisgis.images.IconLoader;
import org.orbisgis.renderer.legend.Legend;
import org.orbisgis.renderer.legend.carto.ClassifiedLegend;
import org.orbisgis.renderer.symbol.Symbol;
import org.orbisgis.renderer.symbol.SymbolFactory;
import org.sif.CRFlowLayout;
import org.sif.CarriageReturn;
import org.sif.UIFactory;

/**
 *
 */
public abstract class PnlAbstractClassifiedLegend extends javax.swing.JPanel
		implements ILegendPanelUI {

	private ClassifiedLegend legend;
	protected LegendContext legendContext;
	protected ClassifiedLegendTableModel tableModel;

	public PnlAbstractClassifiedLegend(LegendContext legendContext,
			ClassifiedLegendTableModel tableModel,
			ClassifiedLegend initialLegend) {
		this.tableModel = tableModel;
		this.legend = initialLegend;
		legend.setName(getLegendTypeName());
		this.legendContext = legendContext;
		initComponents();
		initList();
	}

	private void refreshButtons() {
		boolean someField = canAdd();
		jButtonAddAll.setEnabled(someField);
		jButtonAddOne.setEnabled(someField);

		jButtonDel.setEnabled(table.getSelectedRow() != -1);
	}

	protected abstract boolean canAdd();

	/**
	 * init the table and their events
	 */
	private void initList() {
		table.setModel(tableModel);
		table.setRowHeight(25);

		table.setDefaultRenderer(Symbol.class, new SymbolValueCellRenderer());

		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					int col = table.getSelectedColumn();
					if (col == 0) {
						int row = table.getSelectedRow();
						SymbolEditor symbolEditor = new SymbolEditor(true,
								legendContext, getSymbolFilter());
						symbolEditor.setSymbol((Symbol) tableModel.getValueAt(
								row, 0));

						if (UIFactory.showDialog(symbolEditor)) {
							Symbol sym = symbolEditor.getSymbolComposite();
							tableModel.setValueAt(sym, row, col);
						}
					}
				}
			}

			private SymbolFilter getSymbolFilter() {
				return new CompositeSymbolFilter(new EditableSymbolFilter(),
						new ConstraintSymbolFilter(legendContext
								.getGeometryConstraint()));
			}

		});

		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					public void valueChanged(ListSelectionEvent e) {
						refreshButtons();
					}

				});
	}

	/**
	 * Creates a random symbol with a random color for the fill and black
	 * outline.
	 *
	 * @param constraint
	 * @return Symbol
	 */
	protected Symbol createRandomSymbol() {
		Random rand = new Random();

		int g2 = rand.nextInt(255);
		int r2 = rand.nextInt(255);
		int b2 = rand.nextInt(255);

		Color outline = Color.black;
		Color fill = new Color(r2, g2, b2);

		Symbol lineSymbol = SymbolFactory.createLineSymbol(fill, 1);
		Symbol pointSymbol = SymbolFactory.createCirclePointSymbol(outline,
				fill, 10);
		Symbol polygonSymbol = SymbolFactory.createPolygonSymbol(outline, fill);
		GeometryConstraint geometryConstraint = legendContext
				.getGeometryConstraint();
		Symbol s;
		if (geometryConstraint == null) {
			s = SymbolFactory.createSymbolComposite(polygonSymbol, lineSymbol,
					pointSymbol);
		} else {
			switch (geometryConstraint.getGeometryType()) {
			case GeometryConstraint.LINESTRING:
			case GeometryConstraint.MULTI_LINESTRING:
				s = lineSymbol;
				break;
			case GeometryConstraint.POINT:
			case GeometryConstraint.MULTI_POINT:
				s = pointSymbol;
				break;
			case GeometryConstraint.POLYGON:
			case GeometryConstraint.MULTI_POLYGON:
				s = polygonSymbol;
				break;
			default:
				throw new RuntimeException("bug");
			}
		}

		return s;
	}

	private void initComponents() {
		jCheckBoxRestOfValues = new javax.swing.JCheckBox();
		jCheckBoxOrder = new javax.swing.JCheckBox();
		jPanelTable = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		table = new javax.swing.JTable();
		jPanelButtons = new javax.swing.JPanel();
		jButtonAddAll = new javax.swing.JButton();
		jButtonAddOne = new javax.swing.JButton();
		jButtonDel = new javax.swing.JButton();

		setLayout(new CRFlowLayout());

		add(getTopPanel());
		add(new CarriageReturn());

		JPanel pnlChecks = new JPanel();
		jCheckBoxRestOfValues.setText("rest of values");
		jCheckBoxRestOfValues.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				jCheckBoxRestOfValuesActionPerformed();
			}
		});
		pnlChecks.add(jCheckBoxRestOfValues);
		pnlChecks.add(new CarriageReturn());

		jCheckBoxOrder.setText("order");
		jCheckBoxOrder.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxOrderActionPerformed(evt);
			}
		});
		pnlChecks.add(jCheckBoxOrder);
		add(pnlChecks);
		add(new CarriageReturn());

		jScrollPane1.setPreferredSize(new java.awt.Dimension(454, 175));

		table.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

		}, new String[] { "Symbol", "Value", "Label" }));
		jScrollPane1.setViewportView(table);

		jPanelTable.add(jScrollPane1);

		add(jPanelTable);
		add(new CarriageReturn());

		jButtonAddAll.setIcon(IconLoader.getIcon("addall.png"));
		jButtonAddAll.setToolTipText("Add all");
		jButtonAddAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addAllAction();
			}
		});
		jPanelButtons.add(jButtonAddAll);

		jButtonAddOne.setIcon(IconLoader.getIcon("add.png"));
		jButtonAddOne.setToolTipText("Add");
		jButtonAddOne.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addOneAction();
			}
		});
		jPanelButtons.add(jButtonAddOne);

		jButtonDel.setIcon(IconLoader.getIcon("delete.png"));
		jButtonDel.setToolTipText("Delete");
		jButtonDel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonDelActionPerformed(evt);
			}
		});
		jPanelButtons.add(jButtonDel);

		add(jPanelButtons);
	}

	protected abstract JPanel getTopPanel();

	/**
	 * adds all the values in the layer to the table.
	 *
	 * @param evt
	 */
	protected abstract void addAllAction();

	/**
	 * adds one more value to the table. will be a copy of the last (if exists
	 * any) or a new one with a NullValue.
	 *
	 * @param evt
	 */
	protected abstract void addOneAction();

	private void jButtonDelActionPerformed(java.awt.event.ActionEvent evt) {
		int[] rows = table.getSelectedRows();
		tableModel.deleteRows(rows);
	}

	/**
	 *
	 * @param evt
	 */
	private void jCheckBoxRestOfValuesActionPerformed() {
		boolean isSelected = jCheckBoxRestOfValues.isSelected();
		tableModel.setShowRestOfValues(isSelected);
	}

	private void jCheckBoxOrderActionPerformed(java.awt.event.ActionEvent evt) {
		tableModel.setOrdered(jCheckBoxOrder.isSelected());
	}

	private javax.swing.JButton jButtonAddAll;
	private javax.swing.JButton jButtonAddOne;
	private javax.swing.JButton jButtonDel;
	private javax.swing.JCheckBox jCheckBoxOrder;
	private javax.swing.JCheckBox jCheckBoxRestOfValues;
	private javax.swing.JPanel jPanelButtons;
	private javax.swing.JPanel jPanelTable;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTable table;

	public Component getComponent() {
		return this;
	}

	public Legend getLegend() {
		return legend;
	}

	public boolean acceptsGeometryType(int geometryType) {
		return true;
	}

	public void setLegend(Legend legend) {
		this.legend = (ClassifiedLegend) legend;
		this.tableModel.setLegend(this.legend);
		table.setModel(this.tableModel);

		jCheckBoxRestOfValues
				.setSelected(!(this.legend.getDefaultSymbol() == null));
		refreshButtons();
	}

	public void setLegendContext(LegendContext lc) {
		this.legendContext = lc;
	}

	public String validateInput() {
		if (legend.getClassificationCount() == 0) {
			return "At least a value classification should be added";
		}
		return null;
	}
}
