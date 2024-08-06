package com.cylande.unitedretail.batch.mapper.flatmapper;

import java.io.IOException;
import java.io.Writer;

/**
 * Ecrit le flux XML dans le buffer de lecture du moteur de batch
 */
public class XMLRedirectWriter extends Writer
{
  /**
   * index de lecture dans le buffer xml de
   */
  private int _bufIdx = 0;

  /**
   * determine la fin de lecture
   */
  private boolean _endBuffer = false;

  /**
   * Buffer de lecture du moteur de batch
   */
  private char[] _readBuffer;

  /**
   * Buffer du reste à écrire ( cas de dépassement du flux généré)
   */
  private char[] _remainBuffer;

  /**
   * Nombre de caractères à écrire dans le remainBuffer
   */
  private int _remainBufferLen = 0;

  /**
   * Constructeur
   * @param pBuffer buffer de lecture du provider
   */
  public XMLRedirectWriter(char[] pBuffer)
  {
    _readBuffer = pBuffer;
    _remainBuffer = new char[10000];
  }

  /**
   * close
   * @throws IOException exception
   */
  public void setBuffer(char[] pBuffer)
  {
    _readBuffer = pBuffer;
  }

  /**
   * close
   * @throws IOException exception
   */
  public void close() throws IOException
  {
  }

  /**
   * flush
   * @throws IOException exception
   */
  public void flush() throws IOException
  {
  }

  /**
   * Methode d'écriture appelée par le STAX du FlatTransfomerIn
   * redirige l'écriture de pXmlBuffer dans _readBuffer
   * @param pXmlBuffer buffer issue de la transformation
   * @param pOff offset d'écriture
   * @param pLen nombre de caractères a écrire depuis offset
   * @throws IOException exception
   */
  public void write(char[] pXmlBuffer, int pOff, int pLen) throws IOException
  {
    //cas fin de fichier
    if (_endBuffer)
    {
      //vide le pXmlBuffer dans le remain buffer
      System.arraycopy(pXmlBuffer, pOff, _remainBuffer, _remainBufferLen, pLen);
      _remainBufferLen += pLen;
      return;
    }
    if (_remainBufferLen != 0)
    {
      System.arraycopy(_remainBuffer, 0, _readBuffer, pOff + _bufIdx, _remainBufferLen);
      _bufIdx += _remainBufferLen;
      _remainBufferLen = 0;
    }
    if (_readBuffer.length < (_bufIdx + pLen))
    {
      int availableSize = _readBuffer.length - _bufIdx;
      _remainBufferLen = pLen - availableSize;
      System.arraycopy(pXmlBuffer, pOff + availableSize, _remainBuffer, 0, _remainBufferLen);
      pLen = availableSize;
    }
    //cas classique, on copie dans le buffer
    System.arraycopy(pXmlBuffer, pOff, _readBuffer, pOff + _bufIdx, pLen);
    _bufIdx += pLen;
    if (_bufIdx >= _readBuffer.length)
    {
      _bufIdx = 0;
      _endBuffer = true;
    }
  }

  /**
   * nombre de caractères écrits dans _readBuffer
   * @return résultat
   */
  public int getBufferIndex()
  {
    if (_endBuffer)
    {
      _endBuffer = false;
      return _readBuffer.length;
    }
    return _bufIdx;
  }

  /**
   * vide le dernier buffer
   * @return le nombre de carateres ecrits
   */
  public int flushRemainBuffer()
  {
    if (_remainBufferLen > 0)
    {
      int nbchar = _remainBufferLen;
      System.arraycopy(_remainBuffer, 0, _readBuffer, 0, nbchar);
      _remainBufferLen = 0;
      return nbchar;
    }
    return -1;
  }
}
