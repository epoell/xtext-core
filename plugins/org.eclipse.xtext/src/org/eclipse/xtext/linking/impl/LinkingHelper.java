/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.linking.impl;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.ValueConverterWithValueException;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.google.inject.Inject;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class LinkingHelper {
	
	@Inject
	private IValueConverterService valueConverter;
	
	public void setValueConverter(IValueConverterService valueConverter) {
		this.valueConverter = valueConverter;
	}

	/**
	 * @param grammarElement
	 *            may be any crossreferencable element, i.e. a keyword or a rulecall
	 */
	public String getRuleNameFrom(EObject grammarElement) {
		if (!(grammarElement instanceof Keyword || grammarElement instanceof RuleCall || grammarElement instanceof CrossReference))
			throw new IllegalArgumentException("grammarElement is of type: '" + grammarElement.eClass().getName() + "'");
		AbstractRule rule = null;
		EObject elementToUse = grammarElement;
		if (grammarElement instanceof CrossReference)
			elementToUse = ((CrossReference) grammarElement).getTerminal();
		if (elementToUse instanceof RuleCall)
			rule = ((RuleCall) elementToUse).getRule();
		return rule != null ? rule.getName() : null;
	}

	public String getCrossRefNodeAsString(INode node, boolean convert) {
		String convertMe = NodeModelUtils.getTokenText(node);
		if (!convert)
			return convertMe;
		try {
			String ruleName = getRuleNameFrom(node.getGrammarElement());
			if (ruleName == null)
				return convertMe;
			Object result = valueConverter.toValue(convertMe, ruleName, node);
			return result != null ? result.toString() : null;
		} catch (ValueConverterWithValueException ex) {
			return String.valueOf(ex.getValue());
		} catch (ValueConverterException ex) {
			throw new IllegalNodeException(node, ex);
		}
	}
}
