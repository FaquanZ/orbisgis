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
import org.gdms.driver.ReadAccess
import org.gdms.data.schema.Metadata
import org.gdms.sql.engine.commands.OutputCommand
import org.gdms.sql.engine.operations.Operation
import org.gdms.sql.engine.physical.PhysicalPlanBuilder

/**
 * Represents a ready-to-execute execution graph.
 *
 * <ul>
 * <li>The <tt>prepare</tt> method must be called first.</li>
 * <li>Then <tt>getResultMetadata</tt> can be called to get the result metadata
 * before actually running the query.</li>
 * <li>Then <tt>execute can be called (once)</tt> and returns a <code>ReadAccess</code> object
 * ready to be read.</tt>
 * <li>Then <tt>cleanUp</tt> must be called to free any remaining resources. Note
 * that the <code>ReadAccess</code> object returned by <tt>execute</tt> remains
 * accessible after this call.
 * </ul>
 *
 * The above list can be repeted any number of times in order to re-execute
 * a query.
 *
 * @author Antoine Gourlay
 * @since 0.1
 */
class ExecutionGraph(op: Operation) {

  private var r: ReadAccess = null
  private var dsf: SQLDataSourceFactory = null
  private var start: OutputCommand = null

  /**
   * Prepares the query for execution.
   * @param dsf the <code>SQLDataSourceFactory</code> against which this query
   *       will be executed.
   */
  def prepare(dsf: SQLDataSourceFactory): Unit = {
    if (start == null || dsf != this.dsf) {
      start = PhysicalPlanBuilder.buildPhysicalPlan(op).asInstanceOf[OutputCommand]
    }

    start.prepare(dsf)
    r = start.getResult
  }

  /**
   * Runs the query and returns the result.
   * @return the result of the query
   */
  def execute(): ReadAccess = {
    start.execute
    r
  }

  /**
   * Cleans up the query and any associatd resource.
   */
  def cleanUp() = {
    start.cleanUp
    r = null
  }

  /**
   * Gets the result metadata of this query.
   * @return the metadata of the result of this query
   */
  def getResultMetadata(): Metadata = { r match {
      case null => null
      case _ => r.getMetadata
    }
  }
}