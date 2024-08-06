package com.cylande.unitedretail.batch.mapper;

import com.cylande.unitedretail.batch.exception.StreamMapperException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Interface for using mapper in the batch engine
 */
public interface StreamMapper
{

  /**
   * Transform an InputStream.
   * @param pInputStream : InputStream to transform.
   * @return InputStream : InputStream transformed.
   * @throws StreamMapperException : Exception
   */
  BufferedReader transformInputStream(InputStream pInputStream, String pCharset) throws StreamMapperException, UnsupportedEncodingException, IOException;

  /**
   * Transform an OutputStream.
   * @param pOutputStream : OutputStream to transform.
   * @return BufferedWriter : BufferedWriter transformed.
   * @throws StreamMapperException : Exception
   */
  BufferedWriter transformOutputStream(OutputStream pOutputStream) throws StreamMapperException, IOException;
}
