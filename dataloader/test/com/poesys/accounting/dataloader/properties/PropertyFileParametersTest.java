/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.properties;


import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * CUT: DataLoaderProperties
 * 
 * @author Robert J. Muller
 */
public class PropertyFileParametersTest {
  /**
   * Tests the properties read from a file statically at startup; requires that
   * a properties file exists on the classpath with the name
   * "dataloader.properties"; the test just verifies value existence, it does
   * not test for specific values
   */
  @Test
  public void testProperties() {
    PropertyFileParameters parameters = new PropertyFileParameters();

    assertTrue("No path found", parameters.getPath() != null);
    assertTrue("No start year found", parameters.getStartYear() != null);
    assertTrue("No end year found", parameters.getEndYear() != null);
    assertTrue("No entity name found", parameters.getEntity() != null);
  }
}
