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
package org.orbisgis.core.ui.editors.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.Timer;

import org.gdms.data.ClosedDataSourceException;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceListener;
import org.gdms.data.edition.EditionEvent;
import org.gdms.data.edition.EditionListener;
import org.gdms.data.edition.MultipleEditionEvent;
import org.orbisgis.core.Services;
import org.orbisgis.core.background.BackgroundJob;
import org.orbisgis.core.background.BackgroundListener;
import org.orbisgis.core.background.BackgroundManager;
import org.orbisgis.core.background.DefaultJobId;
import org.orbisgis.core.edition.EditableElement;
import org.orbisgis.core.errorManager.ErrorManager;
import org.orbisgis.core.geocognition.LeafElement;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.LayerCollectionEvent;
import org.orbisgis.core.layerModel.LayerListener;
import org.orbisgis.core.layerModel.LayerListenerEvent;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.layerModel.SelectionEvent;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.map.TransformListener;
import org.orbisgis.core.ui.editors.map.tool.Automaton;
import org.orbisgis.core.ui.editors.map.tool.ToolListener;
import org.orbisgis.core.ui.editors.map.tool.ToolManager;
import org.orbisgis.core.ui.editors.map.tool.TransitionException;
import org.orbisgis.core.ui.pluginSystem.workbench.Names;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchToolBar;
import org.orbisgis.progress.IProgressMonitor;
import org.orbisgis.progress.ProgressMonitor;

import com.vividsolutions.jts.geom.Envelope;

/**
 * MapControl.
 * 
 */

public class MapControl extends JComponent implements ComponentListener {

	private static int lastProcessId = 0;

	private int processId;

	/** The map will draw the last generated image without querying the data. */
	public static final int UPDATED = 0;

	/** The map will query the data to obtain a new image. */
	public static final int DIRTY = 1;

	private int status = DIRTY;

	private ToolManager toolManager;

	private Color backColor = Color.white;

	private MapTransform mapTransform = new MapTransform();

	private MapContext mapContext;

	private Drawer drawer;

	private boolean showCoordinates = true;

	// 250 ms wasn't as good as 1 s because less got painted on each
	// repaint,
	// making rendering appear to be slower. [Jon Aquino]
	// LDB: 400 ms is better when using mouse wheel zooming
	//int timerToDraw = 400;

	

	/**
	 * Creates a new NewMapControl.
	 * 
	 * @param mapContext
	 * @param defaultTool
	 * 
	 * @param ec
	 * @throws TransitionException
	 */
	public MapControl(final MapContext mapContext, EditableElement element,
			Automaton defaultTool) throws TransitionException {
		synchronized (this) {
			this.processId = lastProcessId++;
		}
		this.mapContext = mapContext;
		setDoubleBuffered(true);
		setOpaque(true);
		status = DIRTY;

		toolManager = new ToolManager(defaultTool, mapContext, mapTransform,
				this);
		toolManager.addToolListener(new ToolListener() {

			@Override
			public void transitionException(ToolManager toolManager,
					TransitionException e) {
				Services.getService(ErrorManager.class).error("Tool error", e);
			}

			@Override
			public void stateChanged(ToolManager toolManager) {
			}

			@Override
			public void currentToolChanged(Automaton previous,
					ToolManager toolManager) {
			}
		});
		this.addMouseListener(toolManager);
		this.addMouseWheelListener(toolManager);
		this.addMouseMotionListener(toolManager);

		// events
		this.addComponentListener(this);

		mapTransform.addTransformListener(new TransformListener() {

			public void imageSizeChanged(int oldWidth, int oldHeight,
					MapTransform mapTransform) {
				invalidateImage();
			}

			public void extentChanged(Envelope oldExtent,
					MapTransform mapTransform) {
				invalidateImage();
				// Record new BoundingBox value for map context
				mapContext.setBoundingBox(mapTransform.getExtent());
			}

		});

		// Add editable element listen transform event
		if (element instanceof LeafElement)
			mapTransform.addTransformListener((LeafElement) element);

		// Set extent with BoundingBox value
		ILayer rootLayer = mapContext.getLayerModel();
		Envelope boundingBox = mapContext.getBoundingBox();
		if (boundingBox != null) {
			mapTransform.setExtent(boundingBox);
		} else {
			mapTransform.setExtent(rootLayer.getEnvelope());
		}

		// Add refresh listener
		addLayerListenerRecursively(rootLayer, new RefreshLayerListener());
	}

	public MapContext getMapContext() {
		return mapContext;
	}

	private void addLayerListenerRecursively(ILayer rootLayer,
			RefreshLayerListener refreshLayerListener) {
		rootLayer.addLayerListener(refreshLayerListener);
		DataSource dataSource = rootLayer.getDataSource();
		if (dataSource != null) {
			dataSource.addEditionListener(refreshLayerListener);
			dataSource.addDataSourceListener(refreshLayerListener);
		}
		for (int i = 0; i < rootLayer.getLayerCount(); i++) {
			addLayerListenerRecursively(rootLayer.getLayer(i),
					refreshLayerListener);
		}
	}

	private void removeLayerListenerRecursively(ILayer rootLayer,
			RefreshLayerListener refreshLayerListener) {
		rootLayer.removeLayerListener(refreshLayerListener);
		DataSource dataSource = rootLayer.getDataSource();
		if (dataSource != null) {
			dataSource.removeEditionListener(refreshLayerListener);
			dataSource.removeDataSourceListener(refreshLayerListener);
		}
		for (int i = 0; i < rootLayer.getLayerCount(); i++) {
			removeLayerListenerRecursively(rootLayer.getLayer(i),
					refreshLayerListener);
		}
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		BufferedImage mapTransformImage = mapTransform.getImage();

		// if (status == UPDATED) {
		// If not waiting for an image
		if (mapTransformImage != null) {
			g.drawImage(mapTransformImage, 0, 0, null);
			toolManager.paintEdition(g);
		} else {
			g.setColor(backColor);
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		if (status == DIRTY) {
			int width = this.getWidth();
			int height = this.getHeight();
			GraphicsConfiguration configuration = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration();
			BufferedImage inProcessImage = configuration.createCompatibleImage(
					width, height, BufferedImage.TYPE_INT_ARGB);

			Graphics gImg = inProcessImage.createGraphics();
			gImg.setColor(backColor);
			gImg.fillRect(0, 0, getWidth(), getHeight());
			status = UPDATED;

			if (mapTransform.getAdjustedExtent() != null) {
				mapTransform.setImage(inProcessImage);
				repaint();
				mapContext.draw(mapTransform.getImage(), mapTransform
						.getAdjustedExtent(), new ProgressMonitor(""));
				mapContext.setBoundingBox(mapTransform.getExtent());
				/*Timer timer = new Timer(getTimerToDraw(), new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						repaint();
						
					}
				});*/
				//timer.start();
				drawer = new Drawer(null);
				BackgroundManager bm = (BackgroundManager) Services
						.getService(BackgroundManager.class);
				bm.nonBlockingBackgroundOperation(new DefaultJobId(
						"org.orbisgis.jobs.MapControl-" + processId), drawer);
				bm.addBackgroundListener( Services.getService( WorkbenchContext.class ) );
			}
		}

		if (showCoordinates) {
			Point2D point = toolManager.getLastRealMousePosition();
			String xCoord = "X:" + (int) point.getX();
			String yCoord = "Y:" + (int) point.getY();
			String scale = "1:" + (int) mapTransform.getScaleDenominator();
			FontMetrics fm = g.getFontMetrics();
			Rectangle coords = new Rectangle(0, getHeight() - fm.getHeight(),
					Math.max(fm.stringWidth(xCoord), fm.stringWidth(yCoord)),
					2 * fm.getHeight());
			Rectangle scaleRect = new Rectangle(getWidth()
					- fm.stringWidth(scale), getHeight(),
					fm.stringWidth(scale), fm.getHeight());
			g.setColor(Color.white);
			g.fillRect(coords.x, coords.y - fm.getHeight(), coords.width,
					coords.height);
			g.fillRect(scaleRect.x, scaleRect.y - fm.getHeight(),
					scaleRect.width, scaleRect.height);
			g.setColor(Color.black);
			g.drawString(xCoord, coords.x, coords.y);
			g.drawString(yCoord, 0, getHeight());
			g.drawString(scale, scaleRect.x, scaleRect.y);
		}

	}

/*	public int getTimerToDraw() {
		return timerToDraw;
	}

	public void setTimerToDraw(int timerToDraw) {
		this.timerToDraw = timerToDraw;
	}*/

	/**
	 * Returns the drawn image
	 * 
	 * @return imagen.
	 */
	public BufferedImage getImage() {
		return mapTransform.getImage();
	}

	/**
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {
	}

	/**
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e) {
	}

	/**
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
		mapTransform.resizeImage(getWidth(), getHeight());
	}

	/**
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {
		repaint();
	}

	public Color getBackColor() {
		return backColor;
	}

	public void setBackColor(Color backColor) {
		this.backColor = backColor;
	}

	public void setTool(Automaton tool) throws TransitionException {
		toolManager.setTool(tool);
	}

	private void invalidateImage() {
		status = DIRTY;
		repaint();
	}

	public class Drawer implements BackgroundJob {

		private boolean cancel = false;
		private CancellablePM pm;
		private Timer timer;

		public Drawer(Timer timer) {
			this.timer = timer;
		}

		public String getTaskName() {
			return "Drawing";
		}

		public void run(IProgressMonitor pm) {
			synchronized (this) {
				this.pm = new CancellablePM(cancel, pm);
				pm = this.pm;
			}
			try {
				mapContext.draw(mapTransform.getImage(), mapTransform
						.getAdjustedExtent(), pm);

			} catch (ClosedDataSourceException e) {
				if (!cancel) {
					throw e;
				}
			} catch (RuntimeException e) {
				throw e;
			} catch (Error e) {
				throw e;
			} finally {
				//timer.stop();
				MapControl.this.repaint();
				mapContext.setBoundingBox(mapTransform.getExtent());
				WorkbenchContext wbContext = Services.getService(WorkbenchContext.class);
				wbContext.setLastAction("Update toolbar");
			}
		}

		public void cancel() {
			synchronized (this) {
				if (pm != null) {
					pm.cancel = true;
					cancel = true;
				} else {
					cancel = true;
				}
			}
		}
	}

	private class CancellablePM implements IProgressMonitor {

		private IProgressMonitor decoratedPM;
		private boolean cancel;

		public CancellablePM(boolean cancel, IProgressMonitor pm) {
			this.decoratedPM = pm;
			this.cancel = cancel;
		}

		public void endTask() {
			decoratedPM.endTask();
		}

		public int getCurrentProgress() {
			return decoratedPM.getCurrentProgress();
		}

		public String getCurrentTaskName() {
			return decoratedPM.getCurrentTaskName();
		}

		public int getOverallProgress() {
			return decoratedPM.getOverallProgress();
		}

		public void init(String taskName) {
			decoratedPM.init(taskName);
		}

		public boolean isCancelled() {
			return cancel || decoratedPM.isCancelled();
		}

		public void progressTo(int progress) {
			decoratedPM.progressTo(progress);
		}

		public void startTask(String taskName) {
			decoratedPM.startTask(taskName);
		}

	}

	public MapTransform getMapTransform() {
		return mapTransform;
	}

	public ToolManager getToolManager() {
		return toolManager;
	}

	public Automaton getTool() {
		return toolManager.getTool();
	}

	private class RefreshLayerListener implements LayerListener,
			EditionListener, DataSourceListener {
		public void layerAdded(LayerCollectionEvent listener) {
			for (ILayer layer : listener.getAffected()) {
				addLayerListenerRecursively(layer, this);
				if (mapTransform.getAdjustedExtent() == null) {
					final Envelope e = layer.getEnvelope();
					if (e != null) {
						mapTransform.setExtent(e);
					}
				} else {
					invalidateImage();
				}
			}
		}

		public void layerMoved(LayerCollectionEvent listener) {
			invalidateImage();
		}

		@Override
		public boolean layerRemoving(LayerCollectionEvent layerCollectionEvent) {
			return true;
		}

		public void layerRemoved(LayerCollectionEvent listener) {
			for (ILayer layer : listener.getAffected()) {
				removeLayerListenerRecursively(layer, this);
				invalidateImage();
			}
		}

		public void nameChanged(LayerListenerEvent e) {
		}

		public void visibilityChanged(LayerListenerEvent e) {
			invalidateImage();
		}

		public void styleChanged(LayerListenerEvent e) {
			invalidateImage();
		}

		public void selectionChanged(SelectionEvent e) {
			invalidateImage();
		}

		public void multipleModification(MultipleEditionEvent e) {
			invalidateImage();
		}

		public void singleModification(EditionEvent e) {
			invalidateImage();
		}

		public void cancel(DataSource ds) {
		}

		public void commit(DataSource ds) {
		}

		public void open(DataSource ds) {
			invalidateImage();
		}

	}

	public void closing() {
		/*if (drawer != null) {
			drawer.cancel();
		}*/
		toolManager.freeResources();
		toolManager = null;
	}

	public void setShowCoordinates(boolean showCoordinates) {
		this.showCoordinates = showCoordinates;
		repaint();
	}

	public boolean getShowCoordinates() {
		return showCoordinates;
	}

}