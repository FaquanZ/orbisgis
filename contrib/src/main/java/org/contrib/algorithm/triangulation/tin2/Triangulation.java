package org.contrib.algorithm.triangulation.tin2;

import org.gdms.data.SpatialDataSourceDecorator;
import org.gdms.driver.DriverException;
import org.orbisgis.progress.IProgressMonitor;

import com.vividsolutions.jts.geom.Geometry;

public class Triangulation {
	private SetOfVertices vertices;
	private SetOfTriangles triangles;

	public Triangulation(final SpatialDataSourceDecorator inSds)
			throws DriverException {
		this(inSds, null);
	}

	public Triangulation(final SpatialDataSourceDecorator inSds,
			final String gidFieldName) throws DriverException {
		final long rowCount = inSds.getRowCount();

		long t0 = System.currentTimeMillis(); // REMOVE

		// 1st step: add all the (constraining) vertices
		if (null == gidFieldName) {
			vertices = new SetOfVertices();
			for (long rowIndex = 0; rowIndex < rowCount; rowIndex++) {
				final Geometry geometry = inSds.getGeometry(rowIndex);
				vertices.addAll(geometry.getCoordinates());
			}
		} else {
			int gidFieldIndex = inSds.getFieldIndexByName(gidFieldName);

			vertices = new SetOfVertices();
			for (long rowIndex = 0; rowIndex < rowCount; rowIndex++) {
				final Geometry geometry = inSds.getGeometry(rowIndex);
				final int gid = inSds.getFieldValue(rowIndex, gidFieldIndex)
						.getAsInt();
				vertices.addAll(geometry.getCoordinates(), gid);
			}
		}

		System.err.println((System.currentTimeMillis() - t0) + " ms"); // REMOVE
		System.err.println(vertices.getCoordinate(0)); // REMOVE
		System.err.println((System.currentTimeMillis() - t0) + " ms"); // REMOVE

	}

	public void mesh(IProgressMonitor pm) {
		// build the 1st triangle
		triangles = new SetOfTriangles();
		triangles.add(new Triangle(vertices, 0, 1, 2));

		// then iterate over each already sorted set of vertices
		for (int i = 3; i < vertices.size(); i++) {
			triangles.mesh(vertices, i);
		}
	}
}