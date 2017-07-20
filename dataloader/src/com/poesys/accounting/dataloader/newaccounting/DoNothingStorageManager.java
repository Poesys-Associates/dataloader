package com.poesys.accounting.dataloader.newaccounting;


import java.util.List;


/**
 * Implementation of IStorageManager for unit testing that does nothing and does
 * not throw any exceptions; validation is always true
 * 
 * @author Robert J. Muller
 */
public class DoNothingStorageManager implements IStorageManager {

  @Override
  public Boolean validate(List<FiscalYear> years) {
    // No validation, assume true
    return true;
  }

  @Override
  public void store(String entityName, List<FiscalYear> years,
                    IDataAccessService storageService) {
    // Does nothing and throws no exceptions
  }
}
