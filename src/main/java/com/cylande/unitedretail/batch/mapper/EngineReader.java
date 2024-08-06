package com.cylande.unitedretail.batch.mapper;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * EngineReader
 */
public abstract class EngineReader extends FilterReader
{

  /**
   * current line
   */
  protected String _curLine;

  /**
   */
  protected boolean _emitNewline;

  /**
   * indicate if is the first line read.
   */
  protected boolean _firstRead = true;

  /**
   * indicate if is the last line read.
   */
  protected boolean _lastRead = false;

  /**
   * Coonstructor.
   * @param pIn : BufferedReader.
   */
  public EngineReader(BufferedReader pIn)
  {
    super(pIn);
  }

  /**
   * Constructor.
   * Transform inputStream to BufferedReader
   * @param pIn InputStream.
   */
  public EngineReader(InputStream pIn, String pCharset) throws UnsupportedEncodingException, IOException
  {
    this(new BufferedReader(new InputStreamReader(pIn, pCharset)));
  }

  /**
   * @throws IOException exception
   */
  protected void getNextLine() throws IOException
  {
    _curLine = ((BufferedReader)in).readLine();
    while (_curLine != null)
    {
      _emitNewline = true;
      return;
    }
  }

  /**
   * Tell whether this stream is ready to be read.
   * @return boolean.
   * @throws IOException exception
   */
  public boolean ready() throws IOException
  {
    return _curLine != null || _emitNewline || in.ready();
  }

  /**
   * @return résultat
   * @throws IOException exception
   */
  public int read() throws IOException
  {
    return in.read();
  }

  /**
   * Read characters into a portion of an array.
   * @param pBuf
   * @param pOff
   * @param pLen
   * @return résultat
   * @throws IOException exception
   */
  public abstract int read(char[] pBuf, int pOff, int pLen) throws IOException;
}
