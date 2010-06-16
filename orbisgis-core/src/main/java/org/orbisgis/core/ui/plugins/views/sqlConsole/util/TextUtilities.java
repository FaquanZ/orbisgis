/*
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
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * Copyright (C) 2010 Erwan BOCHER, Pierre-Yves FADET, Alexis GUEGANNO, Maxence LAURENT
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
 * erwan.bocher _at_ ec-nantes.fr
 * gwendall.petit _at_ ec-nantes.fr
 */

package org.orbisgis.core.ui.plugins.views.sqlConsole.util;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Class with several utility functions used by the text area component.
 * 
 * @author Slava Pestov
 * @version $Id: TextUtilities.java,v 1.4 1999/12/13 03:40:30 sp Exp $
 */
public class TextUtilities {
	/**
	 * Returns the offset of the bracket matching the one at the specified
	 * offset of the document, or -1 if the bracket is unmatched (or if the
	 * character is not a bracket).
	 * 
	 * @param doc
	 *            The document
	 * @param offset
	 *            The offset
	 * @exception BadLocationException
	 *                If an out-of-bounds access was attempted on the document
	 *                text
	 */
	public static int findMatchingBracket(Document doc, int offset)
			throws BadLocationException {
		if (doc.getLength() == 0)
			return -1;
		char c = doc.getText(offset, 1).charAt(0);
		char cprime; // c` - corresponding character
		boolean direction; // true = back, false = forward

		switch (c) {
		case '(':
			cprime = ')';
			direction = false;
			break;
		case ')':
			cprime = '(';
			direction = true;
			break;
		case '[':
			cprime = ']';
			direction = false;
			break;
		case ']':
			cprime = '[';
			direction = true;
			break;
		case '{':
			cprime = '}';
			direction = false;
			break;
		case '}':
			cprime = '{';
			direction = true;
			break;
		default:
			return -1;
		}

		int count;

		// How to merge these two cases is left as an exercise
		// for the reader.

		// Go back or forward
		if (direction) {
			// Count is 1 initially because we have already
			// `found' one closing bracket
			count = 1;

			// Get text[0,offset-1];
			String text = doc.getText(0, offset);

			// Scan backwards
			for (int i = offset - 1; i >= 0; i--) {
				// If text[i] == c, we have found another
				// closing bracket, therefore we will need
				// two opening brackets to complete the
				// match.
				char x = text.charAt(i);
				if (x == c)
					count++;

				// If text[i] == cprime, we have found a
				// opening bracket, so we return i if
				// --count == 0
				else if (x == cprime) {
					if (--count == 0)
						return i;
				}
			}
		} else {
			// Count is 1 initially because we have already
			// `found' one opening bracket
			count = 1;

			// So we don't have to + 1 in every loop
			offset++;

			// Number of characters to check
			int len = doc.getLength() - offset;

			// Get text[offset+1,len];
			String text = doc.getText(offset, len);

			// Scan forwards
			for (int i = 0; i < len; i++) {
				// If text[i] == c, we have found another
				// opening bracket, therefore we will need
				// two closing brackets to complete the
				// match.
				char x = text.charAt(i);

				if (x == c)
					count++;

				// If text[i] == cprime, we have found an
				// closing bracket, so we return i if
				// --count == 0
				else if (x == cprime) {
					if (--count == 0)
						return i + offset;
				}
			}
		}

		// Nothing found
		return -1;
	}

	/**
	 * Locates the start of the word at the specified position.
	 * 
	 * @param line
	 *            The text
	 * @param pos
	 *            The position
	 */
	public static int findWordStart(String line, int pos, String noWordSep) {
		char ch = line.charAt(pos - 1);

		if (noWordSep == null)
			noWordSep = "";
		boolean selectNoLetter = (!Character.isLetterOrDigit(ch) && noWordSep
				.indexOf(ch) == -1);

		int wordStart = 0;
		for (int i = pos - 1; i >= 0; i--) {
			ch = line.charAt(i);
			if (selectNoLetter
					^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1)) {
				wordStart = i + 1;
				break;
			}
		}

		return wordStart;
	}

	/**
	 * Locates the end of the word at the specified position.
	 * 
	 * @param line
	 *            The text
	 * @param pos
	 *            The position
	 */
	public static int findWordEnd(String line, int pos, String noWordSep) {
		char ch = line.charAt(pos);

		if (noWordSep == null)
			noWordSep = "";
		boolean selectNoLetter = (!Character.isLetterOrDigit(ch) && noWordSep
				.indexOf(ch) == -1);

		int wordEnd = line.length();
		for (int i = pos; i < line.length(); i++) {
			ch = line.charAt(i);
			if (selectNoLetter
					^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1)) {
				wordEnd = i;
				break;
			}
		}
		return wordEnd;
	}

	public static int getWordPosition(String text, String word) {

		return text.indexOf(word);
	}
}
