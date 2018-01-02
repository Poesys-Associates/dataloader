/*
 * Copyright (c) 2018 Poesys Associates. All rights reserved.
 *
 * This file is part of Poesys/Dataloader.
 *
 * Poesys/Dataloader is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Poesys/Dataloader is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Poesys/Dataloader. If not, see <http://www.gnu.org/licenses/>.
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
   * Construct the accounting system. This method parameterizes several interfaces that provide the
   * basic services used in system construction to enable unit testing with specialized
   * implementations of the interfaces.
   *
   * @param parameters     the object containing the program parameters
   * @param builder        the IBuilder implementation that builds the system
   * @param storageManager the IStorageManager implementation that stores the in-memory system
   * @param dbService      the IDataAccessService implementation that actually stores the data in
   *                       the repository
   */
  void construct(IParameters parameters, IBuilder builder, IStorageManager storageManager,
                 IDataAccessService dbService);
}
