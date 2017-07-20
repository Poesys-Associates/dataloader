/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

import com.poesys.db.InvalidParametersException;


/**
 * CUT: AccountGroup
 * 
 * @author Robert J. Muller
 */
public class AccountGroupTest {
  private static final Integer YEAR = 2017;
  private static final String NAME = "Cash";
  private static final Float START = 100.00F;
  private static final Float END = 109.99F;
  private static final String STRING_REP = "AccountGroup [year=" + YEAR
                                           + ", name=" + NAME + ", start="
                                           + START + ", end=" + END + "]";

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.lang.String, java.lang.Float, java.lang.Float)}
   * . Tests field constructor and getters.
   */
  @Test
  public void testAccountGroup() {
    AccountGroup group = new AccountGroup(YEAR, NAME, START, END);
    assertTrue("year getter failed", YEAR.equals(group.getYear()));
    assertTrue("name getter failed", NAME.equals(group.getName()));
    assertTrue("start account getter failed", START.equals(group.getStart()));
    assertTrue("end account getter failed", END.equals(group.getEnd()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.lang.String, java.lang.Float, java.lang.Float)}
   * . Tests constructor required year exception.
   */
  @Test
  public void testAccountGroupRequiredYear() {
    try {
      new AccountGroup(null, NAME, START, END);
      fail("No exception for null year");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null year");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.lang.String, java.lang.Float, java.lang.Float)}
   * . Tests constructor required name exception.
   */
  @Test
  public void testAccountGroupRequiredName() {
    try {
      new AccountGroup(YEAR, null, START, END);
      fail("No exception for null name");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null name");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.lang.String, java.lang.Float, java.lang.Float)}
   * . Tests constructor required start exception.
   */
  @Test
  public void testAccountGroupRequiredStart() {
    try {
      new AccountGroup(YEAR, NAME, null, END);
      fail("No exception for null start account");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null start account");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.lang.String, java.lang.Float, java.lang.Float)}
   * . Tests constructor required end exception.
   */
  @Test
  public void testAccountGroupRequiredEnd() {
    try {
      new AccountGroup(YEAR, NAME, null, END);
      fail("No exception for null end account");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null end account");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor and getters.
   */
  @Test
  public void testAccountGroupReaderValid() {
    String input = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    AccountGroup group = new AccountGroup(YEAR, reader);
    assertTrue("year getter failed", YEAR.equals(group.getYear()));
    assertTrue("name getter failed", NAME.equals(group.getName()));
    assertTrue("start account getter failed", START.equals(group.getStart()));
    assertTrue("end account getter failed", END.equals(group.getEnd()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor required year.
   */
  @Test
  public void testAccountGroupReaderNullYear() {
    String input = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    try {
      new AccountGroup(null, reader);
      fail("No exception for null fiscal year");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null fiscal year: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor required reader.
   */
  @Test
  public void testAccountGroupReaderNullReader() {
    try {
      new AccountGroup(YEAR, null);
      fail("No exception for null reader");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null reader: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor reader end-of-stream.
   */
  @Test
  public void testAccountGroupReaderEndOfStream() {
    String input = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    try {
      // Read the line before passing it to the constructor to move the reader
      // to
      // the end of the input stream.
      reader.readLine();
      new AccountGroup(YEAR, reader);
      fail("No exception for end of stream");
    } catch (EndOfStream e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for end of stream: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor invalid fields error.
   */
  @Test
  public void testAccountGroupReaderInvalidFields() {
    // input stream missing third field NAME
    String input = START.toString() + "\t" + END.toString();
    BufferedReader reader = new BufferedReader(new StringReader(input));
    try {
      new AccountGroup(YEAR, reader);
      fail("No exception for end of stream");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for end of stream: " + e.getMessage());
    }
  }

  /**
   * Internal subclass for testing that throws IO exception from readLine()
   */
  public class IoExceptionBufferedReader extends BufferedReader {

    /**
     * Create an IoExceptionBufferedReader object.
     * 
     * @param in the input reader to buffer
     */
    public IoExceptionBufferedReader(Reader in) {
      super(in);
    }

    @Override
    public String readLine() throws IOException {
      throw new IOException("IO Exception in readLine()");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor I/O exception.
   */
  @Test
  public void testAccountGroupReaderIoException() {
    // input stream missing third field NAME
    String input = START.toString() + "\t" + END.toString();
    BufferedReader reader =
      new IoExceptionBufferedReader(new StringReader(input));
    try {
      new AccountGroup(YEAR, reader);
      fail("No IO exception");
    } catch (RuntimeException e) {
      if (!e.getMessage().equals("I/O exception reading account group")) {
        fail("Wrong runtime exception, expecting I/O message: "
             + e.getMessage());
      }
    } catch (Exception e) {
      fail("Wrong exception for end of stream: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#toString()}
   * . Tests string representation.
   */
  @Test
  public void testToString() {
    AbstractReaderDto group = new AccountGroup(YEAR, NAME, START, END);
    assertTrue("String representation incorrect: " + group.toString()
                   + ", expecting " + STRING_REP,
               STRING_REP.equals(group.toString()));
  }
}
