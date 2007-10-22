/*
 * The GDMS library (Generic Datasources Management System)
 * is a middleware dedicated to the management of various kinds of
 * data-sources such as spatial vectorial data or alphanumeric. Based
 * on the JTS library and conform to the OGC simple feature access
 * specifications, it provides a complete and robust API to manipulate
 * in a SQL way remote DBMS (PostgreSQL, H2...) or flat files (.shp,
 * .csv...). GDMS is produced  by the geomatic team of the IRSTV
 * Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
 *    Erwan BOCHER, scientific researcher,
 *    Thomas LEDUC, scientific researcher,
 *    Fernando GONZALES CORTES, computer engineer.
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALES CORTES, Thomas LEDUC
 *
 * This file is part of GDMS.
 *
 * GDMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GDMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GDMS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult:
 *    <http://orbisgis.cerma.archi.fr/>
 *    <http://sourcesup.cru.fr/projects/orbisgis/>
 *    <http://listes.cru.fr/sympa/info/orbisgis-developers/>
 *    <http://listes.cru.fr/sympa/info/orbisgis-users/>
 *
 * or contact directly:
 *    erwan.bocher _at_ ec-nantes.fr
 *    fergonco _at_ gmail.com
 *    thomas.leduc _at_ cerma.archi.fr
 */
/**
 *
 */
package org.gdms;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.gdms.data.file.FileSourceDefinition;
import org.gdms.driver.DriverUtilities;

class FileTestSource extends TestSource {

	private Logger logger = Logger.getLogger(FileTestSource.class);

	private String fileName;
	private File originalFile;

	public FileTestSource(String name, String file) {
		super(name);
		this.fileName = new File(file).getName();
		this.originalFile = new File(file);
	}

	public void backup() throws Exception {
		File dest = new File(SourceTest.backupDir.getAbsolutePath() + "/"
				+ name);
		dest.mkdirs();
		File backupFile = new File(dest, fileName);
		String prefix = originalFile.getAbsolutePath();
		prefix = prefix.substring(0, prefix.length() - 4);
		copyGroup(new File(prefix), dest);

		FileSourceDefinition def = new FileSourceDefinition(backupFile);
		SourceTest.dsf.registerDataSource(name, def);
	}

	public void copyGroup(final File prefix, File dir) throws IOException {
		File[] dbFiles = prefix.getParentFile().listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().startsWith(prefix.getName());
			}
		});

		if (dbFiles == null) {
			throw new RuntimeException("Copying group " + prefix + " to " + dir);
		}

		for (int i = 0; i < dbFiles.length; i++) {
			DriverUtilities.copy(dbFiles[i],
					new File(dir, dbFiles[i].getName()));
		}
	}

}