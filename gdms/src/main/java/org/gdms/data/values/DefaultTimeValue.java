/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
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
package org.gdms.data.values;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.types.IncompatibleTypesException;
import org.orbisgis.utils.ByteUtils;

/**
 * Wrapper for times
 *
 * @author Fernando Gonzalez Cortes
 */
class DefaultTimeValue extends AbstractValue implements Serializable, TimeValue {

        private static final String NOTTIME = "The specified value is not a time:";
        private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        private Time value;

        /**
         * Creates a new TimeValue object.
         *
         * @param d
         *            Time value
         */
        DefaultTimeValue(Time d) {
                value = d;
        }

        public static TimeValue parseString(String text) throws ParseException {
                SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
                return new DefaultTimeValue(new Time(sdf.parse(text).getTime()));
        }

        private SimpleDateFormat getDateFormat() {
                return new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        }

        /**
         * Sets the value of the TimeValue
         *
         * @param d
         *            valor
         */
        @Override
        public void setValue(Time d) {
                value = d;
        }

        @Override
        public BooleanValue equals(Value value) {
                if (value instanceof NullValue) {
                        return ValueFactory.createNullValue();
                }

                if (value instanceof TimeValue) {
                        return new DefaultBooleanValue(this.value.equals(((TimeValue) value).getAsTime()));
                } else {
                        throw new IncompatibleTypesException(
                                NOTTIME
                                + TypeFactory.getTypeName(value.getType()));
                }
        }

        @Override
        public BooleanValue greater(Value value) {
                if (value instanceof NullValue) {
                        return ValueFactory.createNullValue();
                }

                if (value instanceof TimeValue) {
                        return new DefaultBooleanValue(this.value.compareTo(((TimeValue) value).getAsTime()) > 0);
                } else {
                        throw new IncompatibleTypesException(
                                NOTTIME
                                + TypeFactory.getTypeName(value.getType()));
                }
        }

        @Override
        public BooleanValue greaterEqual(Value value) {
                if (value instanceof NullValue) {
                        return ValueFactory.createNullValue();
                }

                if (value instanceof TimeValue) {
                        return new DefaultBooleanValue(this.value.compareTo(((TimeValue) value).getAsTime()) >= 0);
                } else {
                        throw new IncompatibleTypesException(
                                NOTTIME
                                + TypeFactory.getTypeName(value.getType()));
                }
        }

        @Override
        public BooleanValue less(Value value) {
                if (value instanceof NullValue) {
                        return ValueFactory.createNullValue();
                }

                if (value instanceof TimeValue) {
                        return new DefaultBooleanValue(this.value.compareTo(((TimeValue) value).getAsTime()) < 0);
                } else {
                        throw new IncompatibleTypesException(
                                NOTTIME
                                + TypeFactory.getTypeName(value.getType()));
                }
        }

        @Override
        public BooleanValue lessEqual(Value value) {
                if (value instanceof NullValue) {
                        return ValueFactory.createNullValue();
                }

                if (value instanceof TimeValue) {
                        return new DefaultBooleanValue(this.value.compareTo(((TimeValue) value).getAsTime()) <= 0);
                } else {
                        throw new IncompatibleTypesException(
                                NOTTIME
                                + TypeFactory.getTypeName(value.getType()));
                }
        }

        @Override
        public BooleanValue notEquals(Value value) {
                if (value instanceof NullValue) {
                        return ValueFactory.createNullValue();
                }

                if (value instanceof TimeValue) {
                        return new DefaultBooleanValue(!this.value.equals(((TimeValue) value).getAsTime()));
                } else {
                        throw new IncompatibleTypesException(
                                NOTTIME
                                + TypeFactory.getTypeName(value.getType()));
                }
        }

        /**
         * Returns a string representation of this TimeValue
         *
         * @return a string formatted as yyyy-MM-dd HH:mm:ss
         */
        @Override
        public String toString() {
                return getDateFormat().format(value);
        }

        @Override
        public int hashCode() {
                return value.hashCode();
        }

        /**
         * @return the content
         */
        public Time getValue() {
                return value;
        }

        @Override
        public String getStringValue(ValueWriter writer) {
                return writer.getStatementString(value);
        }

        @Override
        public int getType() {
                return Type.TIME;
        }

        @Override
        public byte[] getBytes() {
                return ByteUtils.longToBytes(value.getTime());
        }

        public static Value readBytes(byte[] buffer) {
                return new DefaultTimeValue(new Time(ByteUtils.bytesToLong(buffer)));
        }

        @Override
        public Time getAsTime() {
                return value;
        }

        @Override
        public Value toType(int typeCode) {
                switch (typeCode) {
                        case Type.DATE:
                                return ValueFactory.createValue(new Date(value.getTime()));
                        case Type.TIME:
                                return this;
                        case Type.TIMESTAMP:
                                return ValueFactory.createValue(new Timestamp(value.getTime()));
                        case Type.STRING:
                                return ValueFactory.createValue(toString());
                        default:
                                throw new IncompatibleTypesException("Cannot cast to type: " + typeCode);
                }

        }

        @Override
        public int compareTo(Value o) {
                if (o.isNull()) {
                        // by default, NULL FIRST
                        return -1;
                } else if (o instanceof TimeValue) {
                        TimeValue dv = (TimeValue) o;
                        return value.compareTo(dv.getAsTime());
                } else {
                        return super.compareTo(o);
                }
        }
}
