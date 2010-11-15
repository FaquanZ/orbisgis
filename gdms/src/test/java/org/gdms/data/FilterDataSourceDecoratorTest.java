/**
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
 * Previous computer developer : Pierre-Yves FADET, computer engineer, Thomas LEDUC, scientific researcher, Fernando GONZALEZ
 * CORTES, computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * Copyright (C) 2010 Erwan BOCHER, Alexis GUEGANNO, Maxence LAURENT, Antoine GOURLAY
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
 * info@orbisgis.org
 **/

package org.gdms.data;

import java.io.File;
import java.util.List;

import org.gdms.SourceTest;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class FilterDataSourceDecoratorTest extends SourceTest {

	public void testFilterDecorator() throws Exception {
		dsf.getSourceManager().register("landcover2000",
				new File(internalData + "landcover2000.shp"));
		DataSource original = dsf.getDataSource("landcover2000");
		FilterDataSourceDecorator decorator = new FilterDataSourceDecorator(
				original);
		decorator.setFilter("type = 'cereals'");

		original.open();
		decorator.open();

		assertTrue(original.getFieldCount() == decorator.getFieldCount());

		for (int i = 0; i < original.getMetadata().getFieldCount(); i++) {
			assertTrue(original.getFieldName(i).equals(
					decorator.getFieldName(i)));
		}

		int cols = original.getFieldCount();

		for (int i = 0; i < decorator.getRowCount() && i < 10000; i++) {
			long o = decorator.getOriginalIndex(i);
			assertTrue(decorator.getFieldValue(i,
					decorator.getFieldIndexByName("type")).toString().equals(
					"cereals"));
			for (int j = 0; j < cols; j++) {
				assertTrue(decorator.getFieldValue(i, j).doEquals(
						original.getFieldValue(o, j)));
			}
		}

		List<Integer> map = decorator.getIndexMap();
		for (int i = 0; i < map.size(); i++) {
			assertTrue(decorator.getFieldValue(i,
					decorator.getFieldIndexByName("type")).toString().equals(
					"cereals"));
			for (int j = 0; j < cols; j++) {
				assertTrue(decorator.getFieldValue(i, j).doEquals(
						original.getFieldValue(map.get(i), j)));
			}
		}

		decorator.close();
		original.close();
	}

        public void testEditableListener() throws Exception {
                dsf.getSourceManager().register("landcover2000",
				new File(internalData + "landcover2000.shp"));
                dsf.executeSQL("CREATE TABLE test AS SELECT * FROM landcover2000;");
                DataSource original = dsf.getDataSource("test", DataSourceFactory.EDITABLE);

                FilterDataSourceDecorator decorator = new FilterDataSourceDecorator(
				original);
		decorator.setFilter("runoff_win = 0.2");
                long rowC = decorator.getRowCount();
                assertFalse(rowC == 0);

                original.open();
                original.deleteRow(decorator.getOriginalIndex(0));
                original.commit();
                original.close();

                assertFalse(rowC == decorator.getRowCount());
                assertTrue(rowC - 1 == decorator.getRowCount());
                rowC = decorator.getRowCount();

                original.open();
                original.setDouble(decorator.getOriginalIndex(1), "runoff_win", 0.3);
                original.commit();
                original.close();

                assertFalse(rowC == decorator.getRowCount());
                assertTrue(rowC - 1 == decorator.getRowCount());
                rowC = decorator.getRowCount();
        }

	public void testSpatialFilter() throws Exception {

		dsf.getSourceManager().register("landcover2000",
				new File(internalData + "landcover2000.shp"));

		WKTReader wktReader = new WKTReader();
		Geometry geomExtent = wktReader
				.read("POLYGON ((183456.16879270627 2428883.34989648 0, 183461.0194286128 2428262.4685004433 0, 184467.5263792192 2428233.364685004 0, 184477.22765103227 2428883.34989648 0, 183456.16879270627 2428883.34989648 0))");

		int waintingResult = 77;
		
		Envelope extent = geomExtent.getEnvelopeInternal();
		DataSource original = dsf.getDataSource("landcover2000");

		FilterDataSourceDecorator filterDataSourceDecorator = new FilterDataSourceDecorator(
				original);

		String filter = "ST_Intersects(ST_GeomFromText('POLYGON(("
				+ extent.getMinX() + " " + extent.getMinY() + ","
				+ extent.getMinX() + " " + extent.getMaxY() + ","
				+ extent.getMaxX() + " " + extent.getMaxY() + ","
				+ extent.getMaxX() + " " + extent.getMinY() + ","
				+ extent.getMinX() + " " + extent.getMinY() + "))'), "
				+ "the_geom" + ")";
		filterDataSourceDecorator.setFilter(filter);
		
		long filterCount = filterDataSourceDecorator.getRowCount();
		
		assertTrue(filterCount==waintingResult);
		
		
		
		

	}
}
