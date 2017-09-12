/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
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
   * @param cause the causing exception
   */
  public FatalProgramException(String message, Throwable cause) {
    super(MSG + ": " + message, cause);
  }

  /**
   * Create a FatalProgramException object.
   * 
   * @param message the exception message
   * @param cause the causing exception
   * @param enableSuppression whether to enable suppression
   * @param writableStackTrace whether to generate a writable stacktrace
   */
  public FatalProgramException(String message,
                               Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
    super(MSG + ": " + message, cause, enableSuppression, writableStackTrace);
  }

}
