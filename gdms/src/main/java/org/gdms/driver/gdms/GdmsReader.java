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

package org.gdms.driver.gdms;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.gdms.data.metadata.DefaultMetadata;
import org.gdms.data.metadata.Metadata;
import org.gdms.data.types.Constraint;
import org.gdms.data.types.ConstraintFactory;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.ByteProvider;
import org.gdms.data.values.RasterValue;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DriverException;
import org.gdms.driver.ReadAccess;
import org.gdms.driver.ReadBufferManager;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
/**
 * Reader dedicated to the GDMS file format. Used by the GdmsDriver to retrieve informations.
 *
 */
public class GdmsReader {

	private FileInputStream fis;
	private ReadBufferManager rbm;
	private int rowCount;
	private int fieldCount;
	private Envelope fullExtent;
	private DefaultMetadata metadata;
	private int[] rowIndexes;
	private HashMap<Point, Value> rasterValueCache = new HashMap<Point, Value>();
	private byte version;

        /**
         * Create a new GdmsReader instance
         * @param file
         *              the GDMS file.
         * @throws IOException if there is a problem when opening the file.
         */
	public GdmsReader(File file) throws IOException {
		fis = new FileInputStream(file);
		rbm = new ReadBufferManager(fis.getChannel());
	}

        /**
         * Close the reader. It will close the inpu stream associated with the reader.
         * @throws IOException
         */
	public void close() throws IOException {
		fis.close();
		fis = null;
		rbm = null;
	}

        /**
         * Retrieve the metadatas contained in the gdms file.
         * @throws IOException
         *                  If the file format is not supported
         * @throws DriverException
         *                  If there is a problem while reading the metadatas.
         */
	public void readMetadata() throws IOException, DriverException {
		// Read version
		version = rbm.get();
		if ((version != 2) && (version != 3)) {
			throw new IOException("Unsupported gdms format version: " + version);
		}

		// read dimensions
		rowCount = rbm.getInt();
		fieldCount = rbm.getInt();

		// read Envelope
		Coordinate min = new Coordinate(rbm.getDouble(), rbm.getDouble());
		Coordinate max = new Coordinate(rbm.getDouble(), rbm.getDouble());
		fullExtent = new Envelope(min, max);

		// read field metadata
		String[] fieldNames = new String[fieldCount];
		Type[] fieldTypes = new Type[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			// read name
			int nameLength = rbm.getInt();
			byte[] nameBytes = new byte[nameLength];
			rbm.get(nameBytes);
			fieldNames[i] = new String(nameBytes);

			// read type
			int typeCode = rbm.getInt();
			int numConstraints = rbm.getInt();
			Constraint[] constraints = new Constraint[numConstraints];
			for (int j = 0; j < numConstraints; j++) {
				int type = rbm.getInt();
				int size = rbm.getInt();
				byte[] constraintBytes = new byte[size];
				rbm.get(constraintBytes);
				constraints[j] = ConstraintFactory.createConstraint(type,
						constraintBytes);
			}
			fieldTypes[i] = TypeFactory.createType(typeCode, constraints);
		}
		metadata = new DefaultMetadata();
		for (int i = 0; i < fieldTypes.length; i++) {
			Type type = fieldTypes[i];
			metadata.addField(fieldNames[i], type);
		}

		this.rowIndexes = new int[rowCount];
		if (version == 2) {
			// read row indexes after metadata
			for (int i = 0; i < rowCount; i++) {
				this.rowIndexes[i] = rbm.getInt();
			}
		} else if (version == GdmsDriver.VERSION_NUMBER) {
			if (rowCount > 0) {
				// read row indexes at the end of the file
				int rowIndexesDir = rbm.getInt();
				rbm.position(rowIndexesDir);
				for (int i = 0; i < rowCount; i++) {
					this.rowIndexes[i] = rbm.getInt();
				}
			}
		}
	}

        /**
         * get the metadatas contained in the GDMS file.
         * @return
         */
	public Metadata getMetadata() {
		return metadata;
	}

        /**
         * Get the value stored at row rowIndex, in the field fieldId
         * @param rowIndex
         * @param fieldId
         * @return
         * @throws DriverException
         */
	public Value getFieldValue(long rowIndex, int fieldId)
			throws DriverException {
		synchronized (this) {
			int fieldType = metadata.getFieldType(fieldId).getTypeCode();
			if (fieldType == Type.RASTER) {
				Point point = new Point((int) rowIndex, fieldId);
				Value ret = rasterValueCache.get(point);
				if (ret != null) {
					return ret;
				} else {
					try {
						// ignore value size
						moveBufferAndGetSize(rowIndex, fieldId);
						int valueType = rbm.getInt();
						if (valueType == Type.NULL) {
							return ValueFactory.createNullValue();
						} else {
							// Read header
							byte[] valueBytes = new byte[RasterValue.HEADER_SIZE];
							rbm.get(valueBytes);
							Value lazyRasterValue = ValueFactory
									.createLazyValue(fieldType, valueBytes,
											new RasterByteProvider(rowIndex,
													fieldId));
							lazyRasterValue.getAsRaster().open();
							rasterValueCache.put(point, lazyRasterValue);
							return lazyRasterValue;
						}
					} catch (IOException e) {
						throw new DriverException(e.getMessage(), e);
					}
				}
			} else {
				return getFullValue(rowIndex, fieldId);
			}
		}
	}

	private Value getFullValue(long rowIndex, int fieldId)
			throws DriverException {
		try {
			int valueSize = moveBufferAndGetSize(rowIndex, fieldId);
			int valueType = rbm.getInt();
			byte[] valueBytes = new byte[valueSize];
			rbm.get(valueBytes);
			return ValueFactory.createValue(valueType, valueBytes);
		} catch (IOException e) {
			throw new DriverException(e.getMessage(), e);
		}
	}

	private int moveBufferAndGetSize(long rowIndex, int fieldId)
			throws IOException {
		int rowBytePosition = rowIndexes[(int) rowIndex];
		rbm.position(rowBytePosition + 4 * fieldId);
		int fieldBytePosition = rbm.getInt();
		rbm.position(fieldBytePosition);

		// read byte array size
		int valueSize = rbm.getInt();
		return valueSize;
	}

	private class RasterByteProvider implements ByteProvider {

		private long rowIndex;
		private int fieldId;

		public RasterByteProvider(long rowIndex, int fieldId) {
			this.rowIndex = rowIndex;
			this.fieldId = fieldId;
		}

		public byte[] getBytes() throws IOException {
			synchronized (GdmsReader.this) {
				int valueSize = moveBufferAndGetSize(rowIndex, fieldId);
				// Ignore type. If it's null it's not read lazily
				rbm.getInt();
				byte[] valueBytes = new byte[valueSize];
				rbm.get(valueBytes);

				// Restore buffer size
				moveBufferAndGetSize(rowIndex, fieldId);
				rbm.get();

				return valueBytes;
			}
		}
	}

        /**
         * Get the envelope which contains these datas
         * @return
         */
	public Envelope getFullExtent() {
		return fullExtent;
	}

        /**
         * Get the number of rows in the table
         * @return
         */
	public long getRowCount() {
		return rowCount;
	}

	public Number[] getScope(int dimension) {
		if (dimension == ReadAccess.X) {
			return new Number[] { getFullExtent().getMinX(),
					getFullExtent().getMaxX() };
		} else if (dimension == ReadAccess.Y) {
			return new Number[] { getFullExtent().getMinY(),
					getFullExtent().getMaxY() };
		} else {
			return null;
		}
	}
}
