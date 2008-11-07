package org.orbisgis.editorViews.toc;

import org.orbisgis.editors.table.Selection;
import org.orbisgis.editors.table.SelectionListener;
import org.orbisgis.layerModel.ILayer;
import org.orbisgis.layerModel.LayerListenerAdapter;
import org.orbisgis.layerModel.SelectionEvent;

public class LayerSelection implements Selection {

	private ILayer layer;
	private LayerListenerAdapter layerListener = null;

	public LayerSelection(ILayer layer) {
		this.layer = layer;
	}

	@Override
	public int[] getSelectedRows() {
		return layer.getSelection();
	}

	@Override
	public void setSelectedRows(int[] indexes) {
		layer.setSelection(indexes);
	}

	@Override
	public void selectInterval(int init, int end) {
		int[] selection = new int[end - init + 1];
		for (int i = init; i <= end; i++) {
			selection[i - init] = i;
		}
		layer.setSelection(selection);
	}

	@Override
	public void clearSelection() {
		layer.setSelection(new int[0]);
	}

	@Override
	public void setSelectionListener(final SelectionListener listener) {
		if (layerListener != null) {
			removeSelectionListener(listener);
		}
		layerListener = new LayerListenerAdapter() {

			@Override
			public void selectionChanged(SelectionEvent e) {
				listener.selectionChanged();
			}
		};
		layer.addLayerListener(layerListener);
	}

	@Override
	public void removeSelectionListener(SelectionListener listener) {
		layer.removeLayerListener(layerListener);
		layerListener = null;
	}

}
