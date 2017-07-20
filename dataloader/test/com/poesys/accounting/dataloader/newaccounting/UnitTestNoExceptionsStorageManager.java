/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.util.List;


/**
 * Implementation of the IStorageManager interface for unit testing; throws no
 * exceptions and tracks calls to the methods.
 * 
 * @author Robert J. Muller
 */
public class UnitTestNoExceptionsStorageManager implements IStorageManager {
  private int validateCalls = 0;
  private int storeCalls = 0;

  @Override
  public Boolean validate(List<FiscalYear> years) {
    validateCalls++;
    return Boolean.TRUE;
  }

  @Override
  public void store(String entityName, List<FiscalYear> years,
                    IDataAccessService storageService) {
    storeCalls++;
  }

  /**
   * Get the count of the calls to validate().
   * 
   * @return a count
   */
  public int getValidateCalls() {
    return validateCalls;
  }

  /**
   * Get the count of the calls to store().
   * 
   * @return a count
   */
  public int getStoreCalls() {
    return storeCalls;
  }
}
