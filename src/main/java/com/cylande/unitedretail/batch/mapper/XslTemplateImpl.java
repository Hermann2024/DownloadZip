package com.cylande.unitedretail.batch.mapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;

public class XslTemplateImpl implements StreamMapper
{
  public XslTemplateImpl(String pFile)
  {
  }

  public BufferedReader transformInputStream(InputStream pInputStream, String pCharset)
  {
    //TODO
    return null;
  }

  public BufferedWriter transformOutputStream(OutputStream pOutputStream)
  {
    //TODO
    return null;
  }
}
