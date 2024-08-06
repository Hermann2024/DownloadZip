package com.cylande.unitedretail.batch.mapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.cylande.unitedretail.batch.exception.StreamMapperException;
import com.cylande.unitedretail.batch.mapper.flatmapper.MapperReaderImpl;
import com.cylande.unitedretail.batch.mapper.flatmapper.MapperWriterImpl;
import com.cylande.unitedretail.message.batch.CylandeTemplateType;

public class CylandeTemplateImpl implements StreamMapper
{
  private String _templateFile;
  private CylandeTemplateType _template;

  public CylandeTemplateImpl(String pFile, CylandeTemplateType pTemplate)
  {
    _templateFile = pFile;
    _template = pTemplate;
  }

  public BufferedReader transformInputStream(InputStream pInputStream, String pCharset) throws StreamMapperException, UnsupportedEncodingException, IOException
  {
    return new BufferedReader(new MapperReaderImpl(pInputStream, _templateFile, pCharset, _template.getLineFilter()));
  }

  public BufferedWriter transformOutputStream(OutputStream pOutputStream) throws StreamMapperException, IOException
  {
    return new BufferedWriter(new MapperWriterImpl(pOutputStream, _templateFile, _template));
  }
}
