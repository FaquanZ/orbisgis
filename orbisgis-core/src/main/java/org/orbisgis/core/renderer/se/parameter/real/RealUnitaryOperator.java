package org.orbisgis.core.renderer.se.parameter.real;

import javax.xml.bind.JAXBElement;
import org.gdms.data.DataSource;
import org.orbisgis.core.renderer.persistance.ogc.ExpressionType;
import org.orbisgis.core.renderer.persistance.se.ObjectFactory;
import org.orbisgis.core.renderer.persistance.se.ParameterValueType;
import org.orbisgis.core.renderer.persistance.se.UnitaryOperatorType;
import org.orbisgis.core.renderer.se.parameter.ParameterException;

public class RealUnitaryOperator implements RealParameter {

    public enum RealUnitaryOperatorType {

        SQRT, LOG;
    }

    public RealUnitaryOperator() {
    }

    public RealUnitaryOperator(RealParameter value, RealUnitaryOperatorType op) {
        v = value;
        this.op = op;
    }

    public RealParameter getOperand() {
        return v;
    }

    public void setOperand(RealParameter value) {
        v = value;
    }

    public void setOperator(RealUnitaryOperatorType operator) {
        op = operator;
    }

    public RealUnitaryOperatorType getOperator() {
        return op;
    }

    @Override
    public boolean dependsOnFeature() {
        return v.dependsOnFeature();
    }

    @Override
    public double getValue(DataSource ds, long fid) throws ParameterException {
        double value = v.getValue(ds, fid);

        switch (op) {
            case SQRT:
                return Math.sqrt(value);
            case LOG:
                return Math.log10(value); // TODO quelle base ?
            default:
                return value;
        }
    }

    @Override
    public ParameterValueType getJAXBParameterValueType() {
        ParameterValueType p = new ParameterValueType();
        p.getContent().add(this.getJAXBExpressionType());
        return p;
    }

    @Override
    public JAXBElement<? extends ExpressionType> getJAXBExpressionType() {

        UnitaryOperatorType o = new UnitaryOperatorType();

        o.setExpression(this.getOperand().getJAXBExpressionType());

        ObjectFactory of = new ObjectFactory();

        switch (op) {
            case SQRT:
                return of.createSqrt(o);
            case LOG:
                return of.createLog10(o);
        }
        return null;
    }

    private RealParameter v;
    private RealUnitaryOperatorType op;
}
