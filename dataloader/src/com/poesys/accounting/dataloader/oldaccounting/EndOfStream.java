/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
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
   * @param cause the cause of the exception
   */
  public EndOfStream(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a EndOfStream object with all parameters.
   * 
   * @param message the message for the exception
   * @param cause the cause of the exception
   * @param enableSuppression enables suppression of exception
   * @param writableStackTrace produce a writable stack trace
   */
  public EndOfStream(String message,
                     Throwable cause,
                     boolean enableSuppression,
                     boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
