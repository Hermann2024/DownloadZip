package com.cylande.unitedretail.batch.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLEncodingDetector
{
  private static final Pattern XML_ENCODING_PATTERN = Pattern.compile("<\\?xml.*[\\s]+encoding=['\"]([\\w0-9-]*)['\"].*");

  public static String detect(InputStream pInputStream, int pDefaultEncodingLimit, String pDefaultEncoding) throws IOException
  {
    if (!pInputStream.markSupported())
    {
      throw new IllegalArgumentException("input stream does not support mark");
    }
    pInputStream.mark(1000);
    byte[] bytes = new byte[1000];
    pInputStream.read(bytes);
    String xml = new String(bytes);
    pInputStream.reset();
    String[] xmlSplit = xml.split("\\?>");
    if (xmlSplit.length > 1)
    {
      Matcher matcher = XML_ENCODING_PATTERN.matcher(xmlSplit[0]);
      if (matcher.matches())
      {
        return matcher.group(1);
      }
    }
    return pDefaultEncoding;
  }
}
