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

/**
 * Checked exception indicating complete program failure
 *
 * @author Robert J. Muller
 */
public class FatalProgramException extends Exception {

  /** serial version UID for serializable exception */
  private static final long serialVersionUID = 1L;

  private static final String MSG = "Fatal program exception";

  /**
   * Create a FatalProgramException object with default message.
   */
  public FatalProgramException() {
    super(MSG);
  }

  /**
   * Create a FatalProgramException object.
   *
   * @param message the exception message
   */
  public FatalProgramException(String message) {
    super(MSG + ": " + message);
  }

  /**
   * Create a FatalProgramException object.
   *
   * @param cause the causing exception
   */
  public FatalProgramException(Throwable cause) {
    super(MSG, cause);
  }

  /**
   * Create a FatalProgramException object.
   *
   * @param message the exception message
   * @param cause   the causing exception
   */
  public FatalProgramException(String message, Throwable cause) {
    super(MSG + ": " + message, cause);
  }

  /**
   * Create a FatalProgramException object.
   *
   * @param message            the exception message
   * @param cause              the causing exception
   * @param enableSuppression  whether to enable suppression
   * @param writableStackTrace whether to generate a writable stacktrace
   */
  public FatalProgramException(String message, Throwable cause, boolean enableSuppression,
                               boolean writableStackTrace) {
    super(MSG + ": " + message, cause, enableSuppression, writableStackTrace);
  }
}
