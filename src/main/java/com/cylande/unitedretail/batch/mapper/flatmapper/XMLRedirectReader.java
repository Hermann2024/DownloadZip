package com.cylande.unitedretail.batch.mapper.flatmapper;

import java.io.IOException;
import java.io.Reader;

/** {@inheritDoc} */
public class XMLRedirectReader extends Reader
{
  private StringBuilder _xmlBatchBuffer;
  private int _xmlBufferOff = 0;
  private int _xmlBufferLen = 0;

  /**Constructeur*/
  public XMLRedirectReader(StringBuilder pBuff)
  {
    setBuffer(pBuff);
  }

  /**
   * Charge/met � jour le buffer
   * @param pBuff Le buffer � charger/mettre � jour
   */
  public void setBuffer(StringBuilder pBuff)
  {
    _xmlBatchBuffer = pBuff;
    _xmlBufferLen = pBuff.length();
  }

  /**
   * Rempli le staxBuffer, afin qu'il puisse �tre lu par la suite
   * @param pStaxBuffer Le buffer � lire
   * @param pOff Offset
   * @param pLen Nombre d'octets � lire
   * @return le nombre d'octets lus
   * @throws IOException exception
   */
  public int read(char[] pStaxBuffer, int pOff, int pLen) throws IOException
  {
    int i = pOff;
    // On va lire le nombre d'octet qu'on nous demande de lire, on s'arr�te si
    // notre buffer est plein, si c'est le cas, on reprendra la lecture par la suite
    for (; i < (pLen + pOff) && _xmlBufferOff < _xmlBufferLen; i++)
    {
      pStaxBuffer[i] = _xmlBatchBuffer.charAt(_xmlBufferOff++);
    }
    int read = i - pOff;
    if (read == 0)
    {
      return -1;
    }
    return read;
  }

  /** {@inheritDoc} */
  public void close()
  {
  }
}
