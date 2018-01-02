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
package com.poesys.accounting.dataloader.oldaccounting;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;

/**
 * Abstract superclass for DTOs initialized by reading lines from a Reader
 *
 * @author Robert J. Muller
 */
public abstract class AbstractReaderDto {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(AccountGroup.class);

  /** message about end of stream input from reader */
  protected static final String END_OF_STREAM_MSG =
    "reader has reached the end of the input stream";
  /** null parameter to constructor */
  protected static final String NULL_PARAMETER_ERROR = "parameters are required but are null";
  /** null reader object */
  protected static final String NULL_READER_ERROR = "tsv reader required but is null";
  /** wrong number of or invalid type of fields */
  protected static final String INVALID_FIELDS_ERROR = "input data has wrong number of fields: ";
  /** reader I/O exception */
  protected static final String IO_EXCEPTION_ERROR = "I/O exception reading account group";
  /** regular expression for field delimiter */
  protected static final String DELIMITER = "\t";

  /**
   * Create an AbstractReaderDto object.
   */
  public AbstractReaderDto() {
    // nothing to do, used for explicit field constructor
  }

  /**
   * Create a DTO object reading from a tab-delimited line. The client is responsible for opening
   * and closing the reader. The client should catch the EndOfStream throwable to determine when
   * reading is complete and to then close the reader.
   *
   * @param reader the buffered reader set at the current line to read
   */
  public AbstractReaderDto(BufferedReader reader) {
    if (reader == null) {
      throw new InvalidParametersException(NULL_READER_ERROR);
    }

    try {
      String line = reader.readLine();
      if (line == null) {
        // end of stream reached
        throw new EndOfStream(END_OF_STREAM_MSG);
      }
      // Use Apache split to handle null values correctly.
      String[] fields = StringUtils.splitPreserveAllTokens(line, DELIMITER);
      if (fields.length != numberOfFields()) {
        throw new InvalidParametersException(
          INVALID_FIELDS_ERROR + fields.length + " (" + line + ")");
      }
      init(fields);
    } catch (IOException e) {
      logger.error(IO_EXCEPTION_ERROR, e);
      throw new RuntimeException(IO_EXCEPTION_ERROR, e);
    }
  }

  /**
   * Callback to initialize the fields specific to a concrete DTO subclass
   *
   * @param fields the array of data values
   */
  abstract protected void init(String[] fields);

  /**
   * Get the number of fields expected from a line of input data. The concrete subclass overrides
   * this to return the correct integer.
   *
   * @return the number of fields
   */
  abstract protected int numberOfFields();

  @Override
  abstract public int hashCode();

  @Override
  abstract public boolean equals(Object obj);
}