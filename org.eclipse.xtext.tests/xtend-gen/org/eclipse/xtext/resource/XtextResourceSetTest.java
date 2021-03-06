/**
 * Copyright (c) 2012, 2018 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.resource;

import org.eclipse.xtext.resource.AbstractXtextResourceSetTest;
import org.eclipse.xtext.resource.XtextResourceSet;

@SuppressWarnings("all")
public class XtextResourceSetTest extends AbstractXtextResourceSetTest {
  @Override
  protected XtextResourceSet createEmptyResourceSet() {
    return new XtextResourceSet();
  }
}
