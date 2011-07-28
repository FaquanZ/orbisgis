/** OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 *
 * Team leader : Erwan BOCHER, scientific researcher,
 *
 * User support leader : Gwendall Petit, geomatic engineer.
 *
 * Previous computer developer : Pierre-Yves FADET, computer engineer, Thomas LEDUC,
 * scientific researcher, Fernando GONZALEZ CORTES, computer engineer.
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
 */

package org.gdms.sql.engine

import org.gdms.data.SQLDataSourceFactory
import org.gdms.data.schema.Metadata
import org.gdms.data.values.Value
import scala.collection.mutable.ArrayOps
import scalaz.concurrent.Strategy
import org.gdms.sql.engine.commands.Row
import org.gdms.sql.engine.commands.SQLMetadata

/**
 * Holds all the predefined implicits and values for use in GdmSQL
 *
 * @author Antoine Gourlay
 * @since 0.1
 */
object GdmSQLPredef extends GdmSQLPredefLow {

  // Strategy
  implicit val st: Strategy = Strategy.Executor(SQLDataSourceFactory.getThreadExecutor)

  // Rows
  implicit def rowToInnerArray(row: Row): Array[Value] = row.array
  implicit def arraytoRow(a: Array[Value])(implicit r: Row) = new Row(a, r.rowId)
  implicit def listtoRow(a: List[Value])(implicit r: Row) = new Row(a toArray, r.rowId)

  // SQLMetadata
  implicit def meToSQLMe(m: Metadata)(implicit s: SQLMetadata) = SQLMetadata(s.table, m)
  implicit def sqlMeTome(s: SQLMetadata) = s.m
}

/**
 * Holds the predefined implicits with a lower visibility than the ones in {@link GdmSQLPredef}.
 *
 * @author Antoine Gourlay
 * @since 0.1
 */
sealed class GdmSQLPredefLow extends GdmsSQLPredefVeryLow {

  // Rows
  //implicit def rowToSeq(row: Row): Seq[Value] = row.array
  implicit def rowToInnerArrayOps(row: Row): ArrayOps[Value] = refArrayOps(row.array)
}

sealed class GdmsSQLPredefVeryLow {
  // Rows
  implicit def rowToSeq(row: Row): Seq[Value] = row.array
}