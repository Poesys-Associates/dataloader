package com.poesys.accounting.dataloader.newaccounting;


import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.properties.IParameters;


/**
 * Interface for Strategy pattern that updates a FiscalYear with additional
 * elements based on the build process implemented by IBuilder implementations;
 * specific implementations apply to the different entities, each of which may
 * have a unique situation to handle
 * 
 * @author Robert J. Muller
 */
public interface IFiscalYearUpdater {
  /**
   * Update a fiscal year with additional constructs based on the built content
   * of the fiscal year.
   * 
   * @param fiscalYear the fiscal year to udpate
   * @param builder the Builder containing built elements of the accounting
   *          system that the Updater will use to update the fiscal year
   * @param parameters the set of program parameters containing the account
   *          names required
   */
  void update(FiscalYear fiscalYear, IBuilder builder, IParameters parameters);
}
