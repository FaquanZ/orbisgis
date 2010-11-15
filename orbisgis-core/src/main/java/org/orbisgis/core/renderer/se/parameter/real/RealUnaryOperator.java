package org.orbisgis.core.renderer.se.parameter.real;

import javax.xml.bind.JAXBElement;
import org.gdms.data.feature.Feature;
import org.orbisgis.core.renderer.persistance.ogc.ExpressionType;
import org.orbisgis.core.renderer.persistance.se.ObjectFactory;
import org.orbisgis.core.renderer.persistance.se.ParameterValueType;
import org.orbisgis.core.renderer.persistance.se.UnitaryOperatorType;
import org.orbisgis.core.renderer.se.parameter.ParameterException;
import org.orbisgis.core.renderer.se.parameter.SeParameterFactory;

public final class RealUnaryOperator implements RealParameter {

	private Double min;
	private Double max;

    private RealParameter v;
    private RealUnitaryOperatorType op;
	private RealParameterContext ctx;

    public enum RealUnitaryOperatorType {
        SQRT, LOG;
    }

    public RealUnaryOperator() {
		ctx = RealParameterContext.realContext;
    }

    public RealUnaryOperator(RealParameter value, RealUnitaryOperatorType op) {
		this();
        this.op = op;
		setOperand(value);
    }

    public RealUnaryOperator(JAXBElement<UnitaryOperatorType> expr) {
		this();
        UnitaryOperatorType t = expr.getValue();

        this.setOperand(SeParameterFactory.createRealParameter((JAXBElement<? extends ExpressionType>)t.getExpression()));

        String operator = expr.getName().getLocalPart();

        if (operator.equals("Log10")){
            this.op = RealUnitaryOperatorType.LOG;
        }
        else if (operator.equals("Sqrt")){
            this.op = RealUnitaryOperatorType.SQRT;
        }
    }

    public RealParameter getOperand() {
        return v;
    }

    public void setOperand(RealParameter value) {
        v = value;

		if (v != null){
			v.setContext(ctx);
		}
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
    public double getValue(Feature feat) throws ParameterException {
        double value = v.getValue(feat);

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

	@Override
	public String toString(){
		return this.op.toString() + "(" + this.v.toString() + ")";
	}

	@Override
	public void setContext(RealParameterContext ctx) {
		this.ctx = ctx;
		if (v != null){
			v.setContext(ctx);
		}
	}

	@Override
	public RealParameterContext getContext() {
		return ctx;
	}
}
