package com.poesys.accounting.dataloader.newaccounting;


import java.util.List;


/**
 * Manages storing the new accounting data into the accounting database;
 * interface to Accounting/DB; implementations may be production or test to
 * support fake-based unit testing
 * 
 * @author Robert J. Muller
 */
public interface IStorageManager {
  /**
   * Validate a set of fiscal years by creating balance sheet and income
   * statements and checking the balances for each year. Returns true if all
   * years balance, false if at least one year does not balance.
   * 
   * @param years the set of fiscal years
   * @return true if all data are valid, false if any are not
   */

  public Boolean validate(List<FiscalYear> years);

  /**
   * Store the data in a set of fiscal years to the database.
   * 
   * @param entityName the name of the accounting entity being stored
   * @param years the list of new years (order is preserved)
   * @param storageService the storage service to call; permits unit testing
   *          through a fake storage service object
   */
  public void store(String entityName, List<FiscalYear> years,
                    IDataAccessService storageService);
}