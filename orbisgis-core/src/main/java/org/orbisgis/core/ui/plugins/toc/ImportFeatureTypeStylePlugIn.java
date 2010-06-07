package org.orbisgis.core.ui.plugins.toc;

import javax.swing.JOptionPane;

import org.gdms.data.types.Constraint;
import org.gdms.data.types.GeometryConstraint;
import org.gdms.data.types.Type;
import org.gdms.driver.DriverException;
import org.orbisgis.core.Services;
import org.orbisgis.core.images.IconNames;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.MapContext;

import org.orbisgis.core.renderer.se.FeatureTypeStyle;
import org.orbisgis.core.sif.OpenFilePanel;
import org.orbisgis.core.sif.SaveFilePanel;
import org.orbisgis.core.sif.UIFactory;

import org.orbisgis.core.ui.pluginSystem.AbstractPlugIn;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.pluginSystem.PlugInContext.SelectionAvailability;
import org.orbisgis.core.ui.pluginSystem.PlugInContext.LayerAvailability;

import org.orbisgis.core.ui.pluginSystem.workbench.Names;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchFrame;

public class ImportFeatureTypeStylePlugIn extends AbstractPlugIn {

	public boolean execute(PlugInContext context) {
		MapContext mapContext = getPlugInContext().getMapContext();
		ILayer[] selectedResources = mapContext.getSelectedLayers();

		if (selectedResources.length == 0) {
			execute(mapContext, null);
		} else {
			for (ILayer resource : selectedResources) {
				execute(mapContext, resource);
			}
		}
		return true;
	}

	public void initialize(PlugInContext context) throws Exception {
		WorkbenchContext wbContext = context.getWorkbenchContext();
		WorkbenchFrame frame = wbContext.getWorkbench().getFrame().getToc();

		context.getFeatureInstaller().addPopupMenuItem(frame, this,
				new String[] { Names.POPUP_TOC_FEATURETYPESTYLE_IMPORT },
				Names.POPUP_TOC_LEGEND_GROUP, false,
				getIcon(IconNames.POPUP_TOC_LEGEND_ICON), wbContext);
	}

	public void execute(MapContext mapContext, ILayer layer) {
		try {
			Type typ = layer.getDataSource().getMetadata().getFieldType(
					layer.getDataSource().getSpatialFieldIndex());
			GeometryConstraint cons = (GeometryConstraint) typ
					.getConstraint(Constraint.GEOMETRY_TYPE);

            final OpenFilePanel inputXMLPanel = new OpenFilePanel(
					"org.orbisgis.core.ui.editorViews.toc.actions.ImportStyle",
					"Youpiiiiii ICI");

            inputXMLPanel.addFilter("se", "Symbology Encoding FeatureTypeStyle");

			if (UIFactory.showDialog(inputXMLPanel)) {
				String seFile = inputXMLPanel.getSelectedFile().getAbsolutePath();
                layer.setFeatureTypeStyle(new FeatureTypeStyle(layer, seFile));
			}

		} catch (DriverException e) {
			Services.getErrorManager().error(Names.ERROR_EDIT_LEGEND_LAYER, e);
		}
	}

	public boolean isEnabled() {
		return getPlugInContext().checkLayerAvailability(
				new SelectionAvailability[] {SelectionAvailability.EQUAL},
				1,
				new LayerAvailability[] {LayerAvailability.VECTORIAL});
	}

	public boolean isSelected() {
		// TODO Auto-generated method stub
		return false;
	}
}
