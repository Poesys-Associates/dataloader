/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.util.List;


/**
 * Implementation of the IStorageManager interface that validates but does not
 * actually persist the data; used for system testing
 * 
 * @author Robert J. Muller
 */
public class NonStoringStorageManager extends AbstractValidatingStorageManager implements IStorageManager {
  @Override
  public void store(String entityName, List<FiscalYear> years,
                    IDataAccessService storageService) {
    // do nothing
  }
}
