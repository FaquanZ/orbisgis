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
package org.orbisgis.view.toc.actions.cui.legends.ui;

import net.miginfocom.swing.MigLayout;
import org.orbisgis.legend.Legend;
import org.orbisgis.legend.thematic.constant.UniqueSymbolLine;
import org.orbisgis.view.toc.actions.cui.LegendContext;
import org.orbisgis.view.toc.actions.cui.SimpleGeometryType;
import org.orbisgis.view.toc.actions.cui.legends.panels.LinePanel;
import org.orbisgis.view.toc.actions.cui.legends.panels.PreviewPanel;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.*;

/**
 * "Unique Symbol - Line" UI.
 *
 * {@code JPanel} that ca nbe used to configure simple constant {@code
 * LineSymbolizer} instances that have been recognized as unique symbols made
 * justof one simple {@code PenStroke}.
 * @author Alexis Guéganno
 */
public final class PnlUniqueLineSE extends PnlUniqueSymbolSE {
        private static final I18n I18N = I18nFactory.getI18n(PnlUniqueLineSE.class);

        public static final String LINE_SETTINGS = I18n.marktr("Line settings");

        private UniqueSymbolLine uniqueLine;
        private final boolean displayUom;

        /**
         * Builds a panel based on a new legend.
         */
        public PnlUniqueLineSE() {
            this(new UniqueSymbolLine());
        }

        /**
         * Builds a panel based on the given legend, displaying the UOM.
         *
         * @param legend Legend
         */
        public PnlUniqueLineSE(UniqueSymbolLine legend) {
            this(legend, true);
        }

        /**
         * Builds a panel based on the given legend, optionally displaying the
         * UOM.
         *
         * @param legend Legend
         * @param uom    True if the UOM should be displayed
         */
        public PnlUniqueLineSE(UniqueSymbolLine legend, boolean uom){
            this.uniqueLine = legend;
            this.displayUom = uom;
            initPreview();
            initializeLegendFields();
        }

        @Override
        public UniqueSymbolLine getLegend() {
                return uniqueLine;
        }

        @Override
        public void setLegend(Legend legend) {
                throw new UnsupportedOperationException("No longer setting " +
                        "legends this way for unique lines.");
        }

        /**
         * Initialize the panel. This method is called just after the panel
         * creation.
         * @param lc LegendContext is useful to get some information about the
         * layer in edition.
         */
        @Override
        public void initialize(LegendContext lc) {
            initialize(lc, new UniqueSymbolLine());
        }

        @Override
        public boolean acceptsGeometryType(int geometryType) {
                return geometryType == SimpleGeometryType.LINE ||
                        geometryType == SimpleGeometryType.POLYGON||
                        geometryType == SimpleGeometryType.ALL;
        }

        @Override
        public Legend copyLegend() {
                UniqueSymbolLine usl = new UniqueSymbolLine();
                usl.getPenStroke().setDashArray(uniqueLine.getPenStroke().getDashArray());
                usl.getPenStroke().setLineWidth(uniqueLine.getPenStroke().getLineWidth());
                usl.getPenStroke().setLineColor(uniqueLine.getPenStroke().getLineColor());
                return usl;
        }

        @Override
        public void initializeLegendFields() {
                this.removeAll();
                JPanel glob = new JPanel(new MigLayout());
                glob.add(new LinePanel(uniqueLine,
                        getPreview(),
                        I18N.tr(LINE_SETTINGS),
                        false,
                        displayUom));
                glob.add(new PreviewPanel(getPreview()));
                this.add(glob);
        }

        // ************************* UIPanel ***************************
        @Override
        public String getTitle() {
            return UniqueSymbolLine.NAME;
        }
}
