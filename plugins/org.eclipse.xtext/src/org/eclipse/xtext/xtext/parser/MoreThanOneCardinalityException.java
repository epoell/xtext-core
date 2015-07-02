/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xtext.parser;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.nodemodel.INode;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * @since 2.9
 */
public class MoreThanOneCardinalityException extends ValueConverterException {

	private static final long serialVersionUID = 1L;

	public MoreThanOneCardinalityException(String newCardinality, String mergedCardinality, INode node) {
		super(String.format("More than one cardinality was set. Merging '%s' with previously assigned cardinality to '%s'.", newCardinality, mergedCardinality),
						node, null);
	}
	
}
