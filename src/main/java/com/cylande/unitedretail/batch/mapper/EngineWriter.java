package com.cylande.unitedretail.batch.mapper;

import java.io.BufferedWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public abstract class EngineWriter extends FilterWriter
{
  protected OutputStream _out;
  protected StringBuffer _stringBuf;

  /**
   * Constructor
   * @param pWriter
   * @throws IOException exception
   */
  public EngineWriter(Writer pWriter) throws IOException
  {
    super(pWriter);
    _stringBuf = new StringBuffer();
  }

  /**
   * Constructor
   * @param pOutputStream
   * @throws IOException exception
   */
  public EngineWriter(OutputStream pOutputStream) throws IOException
  {
    this(new BufferedWriter(new OutputStreamWriter(pOutputStream, "UTF-8")));
  }

  /**
   * Writer du buffer
   * @param pBuf
   * @param pOff
   * @param pLen
   * @throws IOException exception
   */
  public abstract void write(char[] pBuf, int pOff, int pLen) throws IOException;

  public boolean isListMode()
  {
    return false;
  }

  public String getEndElement()
  {
    return null;
  }

  public String getStartElement()
  {
    return null;
  }
}
