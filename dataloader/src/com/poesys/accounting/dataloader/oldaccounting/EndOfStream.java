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

/**
 * Exception indicating a BufferedReader has reached the end of the input stream
 *
 * @author Robert J. Muller
 */
public class EndOfStream extends RuntimeException {

  /** default serial version UID for serializable object */
  private static final long serialVersionUID = 1L;

  /**
   * Create an EndOfStream object.
   */
  public EndOfStream() {
  }

  /**
   * Create a EndOfStream object with a message.
   *
   * @param message the message for the exception
   */
  public EndOfStream(String message) {
    super(message);
  }

  /**
   * Create a EndOfStream object with a cause.
   *
   * @param cause the cause of the exception
   */
  public EndOfStream(Throwable cause) {
    super(cause);
  }

  /**
   * Create a EndOfStream object with a message and cause.
   *
   * @param message the message for the exception
   * @param cause   the cause of the exception
   */
  public EndOfStream(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a EndOfStream object with all parameters.
   *
   * @param message            the message for the exception
   * @param cause              the cause of the exception
   * @param enableSuppression  enables suppression of exception
   * @param writableStackTrace produce a writable stack trace
   */
  public EndOfStream(String message, Throwable cause, boolean enableSuppression, boolean
    writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
