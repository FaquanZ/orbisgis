package org.orbisgis.core.ui.plugins.editors.mapEditor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;

import javax.swing.JLabel;

import org.orbisgis.core.Services;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.ui.editor.IEditor;
import org.orbisgis.core.ui.editors.map.tool.ToolManager;
import org.orbisgis.core.ui.pluginSystem.AbstractPlugIn;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.pluginSystem.workbench.Names;
import org.orbisgis.core.ui.plugins.editor.PlugInEditorListener;
import org.orbisgis.core.ui.plugins.views.MapEditorPlugIn;
import org.orbisgis.core.ui.plugins.views.editor.EditorManager;

public class ShowXYPlugIn extends AbstractPlugIn {	
	
	private JLabel showXY;
	
	public boolean execute(PlugInContext context) throws Exception {
		return true;
	}	
	
	private MouseMotionAdapter mouseMotionAdapter =
		new MouseMotionAdapter()
	{
		public void mouseMoved(MouseEvent e)
		{
			ToolManager toolManager =null;
			String xCoord="", yCoord="",scale="";
			if(getPlugInContext().getMapEditor()!=null) {
				toolManager = getPlugInContext().getMapEditor().getMapControl().getToolManager();
				Point2D point = toolManager.getLastRealMousePosition();
				xCoord = "X:" + (int) point.getX();
				yCoord = "Y:" + (int) point.getY();
				scale = "1:"+ (int)toolManager.getMapTransform().getScaleDenominator();				
				showXY.setText(xCoord +  "  "  + yCoord + "  " + scale);

			}
			xCoord=null;
			yCoord=null;scale=null;
		}
	};
	
	public void initialize(final PlugInContext context) throws Exception {		
		showXY = new JLabel();
		showXY.setEnabled(false);		
		EditorManager em = Services.getService(EditorManager.class);
		em.addEditorListener(new PlugInEditorListener(this,showXY,Names.MAP_TOOLBAR_PROJECTION,
								mouseMotionAdapter,context,true));
		
	}	

	public boolean isEnabled() {
		showXY.setText("X:0.0     Y:0.0" );
		boolean isVisible = false;
		IEditor editor = Services.getService(EditorManager.class).getActiveEditor();
		if (editor != null && editor instanceof MapEditorPlugIn && getPlugInContext().getMapEditor()!=null) {
			MapContext mc = (MapContext) editor.getElement().getObject();
			isVisible = mc.getLayerModel().getLayerCount() > 0;			
		}	
		showXY.setEnabled(isVisible);		
		return isVisible;
	}
	
	public boolean isSelected() {		
		if (getPlugInContext().getMapEditor() != null) {			
			return getPlugInContext().getMapEditor().getShowInfo();
		}
		return false;
	}
}
