package com.cylande.unitedretail.batch.mapper;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.exception.StreamMapperException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

public class JavaclassTemplateImpl implements StreamMapper
{
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(JavaclassTemplateImpl.class);

  /** Mapper */
  private StreamMapper _streamMapper = null;

  /**
   * Constructor
   * @param pName name of class to instanciate
   * @throws ProviderException exception
   */
  public JavaclassTemplateImpl(String pName) throws ProviderException
  {
    try
    {
      Class myClass = Class.forName(pName);
      _streamMapper = (StreamMapper)myClass.newInstance();
    }
    catch (ClassNotFoundException e)
    {
      throw new ProviderException(BatchErrorDetail.FILEPROVIDER_GETMAPPERMANAGER_ERROR, e);
    }
    catch (IllegalAccessException e)
    {
      LOGGER.error("IllegalAccessException occured : " + e.getMessage());
    }
    catch (InstantiationException e)
    {
      throw new ProviderException(BatchErrorDetail.PROVIDER_INSTANTIATEMAPPER_ERROR, e);
    }
  }

  /**
   * get current Stream
   * @return résultat
   */
  public StreamMapper getStream()
  {
    return _streamMapper;
  }

  /**
   * @param pInputStream
   * @param pCharset
   * @return BufferedReader
   * @throws UnsupportedEncodingException exception
   * @throws IOException exception
   * @throws StreamMapperException exception
   */
  public BufferedReader transformInputStream(InputStream pInputStream, String pCharset) throws UnsupportedEncodingException, IOException, StreamMapperException
  {
    return new BufferedReader(getStream().transformInputStream(pInputStream, pCharset));
  }

  /**
   * @param pOutputStream
   * @return BuffuredWriter
   * @throws IOException exception
   */
  public BufferedWriter transformOutputStream(OutputStream pOutputStream) throws IOException, StreamMapperException
  {
    return new BufferedWriter(getStream().transformOutputStream(pOutputStream));
  }
}
