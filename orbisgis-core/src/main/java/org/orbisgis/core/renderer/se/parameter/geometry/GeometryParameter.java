package org.orbisgis.core.renderer.se.parameter.geometry;

import com.vividsolutions.jts.geom.Geometry;
import org.gdms.data.SpatialDataSourceDecorator;
import org.orbisgis.core.renderer.se.parameter.ParameterException;

public interface GeometryParameter{
    public Geometry getTheGeom(SpatialDataSourceDecorator ds, long fid) throws ParameterException;
}
