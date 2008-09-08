package org.orbisgis.geocognition.symbology;

import java.util.HashSet;

import org.orbisgis.PersistenceException;
import org.orbisgis.Services;
import org.orbisgis.geocognition.GeocognitionElementFactory;
import org.orbisgis.geocognition.GeocognitionExtensionElement;
import org.orbisgis.renderer.legend.Legend;
import org.orbisgis.renderer.legend.carto.LegendManager;

public class GeocognitionLegendFactory implements GeocognitionElementFactory {

	@Override
	public String getJAXBContextPath() {
		HashSet<String> contexts = new HashSet<String>();
		Legend[] availableLegends = getAvailableLegends();
		for (Legend legend : availableLegends) {
			String context = legend.getJAXBContext();
			if (context != null) {
				contexts.add(context);
			}
		}

		String ret = "";
		String separator = "";
		for (String context : contexts) {
			ret += separator + context;
			separator = ":";
		}

		if (ret.length() == 0) {
			return null;
		} else {
			return ret;
		}
	}

	private Legend[] getAvailableLegends() {
		LegendManager lm = (LegendManager) Services
				.getService("org.orbisgis.LegendManager");
		Legend[] availableLegends = lm.getAvailableLegends();
		return availableLegends;
	}

	@Override
	public GeocognitionExtensionElement createElementFromXML(Object xmlObject,
			String contentTypeId) throws PersistenceException {
		Legend[] availableLegends = getAvailableLegends();
		for (Legend legend : availableLegends) {
			if (legend.getLegendTypeId().equals(contentTypeId)) {
				Legend newInstance = legend.newInstance();
				newInstance.setJAXBObject(xmlObject);
				return new GeocognitionLegend(newInstance, this);
			}
		}

		throw new PersistenceException("Unrecognized legend: " + contentTypeId);
	}

	@Override
	public GeocognitionExtensionElement createGeocognitionElement(Object object) {
		return new GeocognitionLegend((Legend) object, this);
	}

	@Override
	public boolean accepts(Object o) {
		return o instanceof Legend;
	}

	@Override
	public boolean acceptContentTypeId(String typeId) {
		Legend[] availableLegends = getAvailableLegends();
		for (Legend legend : availableLegends) {
			if (legend.getLegendTypeId().equals(typeId)) {
				return true;
			}
		}

		return false;
	}

}
