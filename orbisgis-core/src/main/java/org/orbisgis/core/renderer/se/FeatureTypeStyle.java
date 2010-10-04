/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.orbisgis.core.renderer.se;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.validation.Schema;
import org.gdms.driver.driverManager.DriverLoadException;

import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.map.MapTransform;


import org.orbisgis.core.renderer.persistance.se.FeatureTypeStyleType;
import org.orbisgis.core.renderer.persistance.se.ObjectFactory;
import org.orbisgis.core.renderer.persistance.se.RuleType;
import org.orbisgis.core.renderer.se.common.Uom;

/**
 *
 * @author maxence
 */
public final class FeatureTypeStyle implements SymbolizerNode {

	public FeatureTypeStyle(ILayer layer) {
		rules = new ArrayList<Rule>();
		this.layer = layer;

		this.addRule(new Rule(layer));
	}

	public FeatureTypeStyle(ILayer layer, String seFile) {
		rules = new ArrayList<Rule>();
		this.layer = layer;

		JAXBContext jaxbContext;
		try {

			jaxbContext = JAXBContext.newInstance(FeatureTypeStyleType.class);

			Unmarshaller u = jaxbContext.createUnmarshaller();


			Schema schema = u.getSchema();
			ValidationEventCollector validationCollector = new ValidationEventCollector();
			u.setEventHandler(validationCollector);

			JAXBElement<FeatureTypeStyleType> fts = (JAXBElement<FeatureTypeStyleType>) u.unmarshal(
					new FileInputStream(seFile));

			for (ValidationEvent event : validationCollector.getEvents()) {
				String msg = event.getMessage();
				ValidationEventLocator locator = event.getLocator();
				int line = locator.getLineNumber();
				int column = locator.getColumnNumber();
				System.out.println("Error at line " + line + " column " + column);
			}

			this.setFromJAXB(fts);

		} catch (IOException ex) {
			Logger.getLogger(FeatureTypeStyle.class.getName()).log(Level.SEVERE, null, ex);
		} catch (DriverLoadException ex) {
			Logger.getLogger(FeatureTypeStyle.class.getName()).log(Level.SEVERE, null, ex);
		} catch (JAXBException ex) {
			Logger.getLogger(FeatureTypeStyle.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public FeatureTypeStyle(JAXBElement<FeatureTypeStyleType> ftst, ILayer layer) {
		rules = new ArrayList<Rule>();
		this.layer = layer;
		this.setFromJAXB(ftst);
	}

	private void setFromJAXB(JAXBElement<FeatureTypeStyleType> ftst) {
		FeatureTypeStyleType fts = ftst.getValue();

		if (fts.getName() != null) {
			this.name = fts.getName();
		}

		if (fts.getRule() != null) {
			for (RuleType rt : fts.getRule()) {
				this.addRule(new Rule(rt, this.layer));
			}
		}
	}

	/**
	 * This method remove everything in this feature type style
	 */
	public void clear() {
		this.rules.clear();
	}

	public void export(String seFile) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(FeatureTypeStyleType.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(getJAXBElement(), new FileOutputStream(seFile));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(FeatureTypeStyle.class.getName()).log(Level.SEVERE, null, ex);
		} catch (JAXBException ex) {
			Logger.getLogger(FeatureTypeStyle.class.getName()).log(Level.SEVERE, null, ex);
		}


	}

	public JAXBElement<FeatureTypeStyleType> getJAXBElement() {
		FeatureTypeStyleType ftst = new FeatureTypeStyleType();

		if (this.name != null) {
			ftst.setName(this.name);
		}
		List<RuleType> ruleTypes = ftst.getRule();
		for (Rule r : rules) {
			ruleTypes.add(r.getJAXBType());
		}

		ObjectFactory of = new ObjectFactory();

		return of.createFeatureTypeStyle(ftst);
	}

	/**
	 * Return all symbolizers from rules with a filter but not those from
	 * a ElseFilter (i.e. fallback) rule
	 *
	 * @param mt
	 * @param layerSymbolizers
	 * @param overlaySymbolizers
	 *
	 * @param rules
	 * @param fallbackRules
	 * @todo take into account domain constraint
	 */
	public void getSymbolizers(MapTransform mt,
			ArrayList<Symbolizer> layerSymbolizers,
			ArrayList<Symbolizer> overlaySymbolizers,
			ArrayList<Rule> rules,
			ArrayList<Rule> fallbackRules) {

		for (Rule r : this.rules) {
			// Only process visible rules
			if (r.isVisible()) {
				// first check the domain
				if (r.isDomainAllowed(mt)) {
					// Split standard rules and elseFilter rules
					if (!r.isFallbackRule()) {
						rules.add(r);
					} else {
						fallbackRules.add(r);
					}

					for (Symbolizer s : r.getCompositeSymbolizer().getSymbolizerList()) {
						// Extract TextSymbolizer into specific set =>
						// Label are always drawn on top
						if (s instanceof TextSymbolizer) {
							overlaySymbolizers.add(s);
						} else {
							layerSymbolizers.add(s);
						}
					}
				}
			}
		}

		Collections.sort(layerSymbolizers);
	}

	public void resetSymbolizerLevels() {
		int level = 1;

		for (Rule r : rules) {
			for (Symbolizer s : r.getCompositeSymbolizer().getSymbolizerList()) {
				if (s instanceof TextSymbolizer) {
					s.setLevel(Integer.MAX_VALUE);
				} else {
					s.setLevel(level);
					level++;
				}
			}
		}
	}

	public ILayer getLayer() {
		return layer;
	}

	public void setLayer(ILayer layer) {
		this.layer = layer;
	}

	@Override
	public Uom getUom() {
		return null;
	}

	@Override
	public SymbolizerNode getParent() {
		return null;
	}

	@Override
	public void setParent(SymbolizerNode node) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Rule> getRules() {
		return rules;
	}

	public boolean moveRuleUp(int i) {
		try {
			if (i > 0) {
				Rule r = rules.remove(i);
				rules.add(i - 1, r);
				return true;
			}
		} catch (IndexOutOfBoundsException ex) {
		}
		return false;
	}

	public boolean moveRuleDown(int i) {
		try {
			if (i < rules.size() - 1) {
				Rule r = rules.remove(i);
				rules.add(i + 1, r);
				return true;
			}

		} catch (IndexOutOfBoundsException ex) {
		}
		return false;
	}

	public void addRule(Rule r) {
		r.setParent(this);
		rules.add(r);
	}

	public boolean deleteRule(int i) {
		try {
			rules.remove(i);
			return true;
		} catch (IndexOutOfBoundsException ex) {
			return false;
		}
	}

	private String name;
	private ArrayList<Rule> rules;
	private ILayer layer;
}
