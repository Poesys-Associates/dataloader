/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader;


import com.poesys.accounting.dataloader.newaccounting.IDataAccessService;
import com.poesys.accounting.dataloader.newaccounting.IStorageManager;
import com.poesys.accounting.dataloader.properties.IParameters;


/**
 * Director component of a Builder pattern for data loading
 * 
 * @author Robert J. Muller
 */
public interface IDirector {
  /**
   * Construct the accounting system. This method parameterizes several
   * interfaces that provide the basic services used in system construction to
   * enable unit testing with specialized implementations of the interfaces.
   * 
   * @param parameters the object containing the program parameters
   * @param builder the IBuilder implementation that builds the system
   * @param storageManager the IStorageManager implementation that stores the
   *          in-memory system
   * @param dbService the IDataAccessService implementation that actually stores
   *          the data in the repository
   */
  public void construct(IParameters parameters, IBuilder builder,
                        IStorageManager storageManager,
                        IDataAccessService dbService);
}
