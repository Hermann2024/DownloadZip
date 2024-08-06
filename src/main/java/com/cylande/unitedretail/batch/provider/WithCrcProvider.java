package com.cylande.unitedretail.batch.provider;

import com.cylande.unitedretail.batch.exception.ProviderException;

import java.io.IOException;

/**
 * Interface for providers which implements CrcCheck Ability
 */
public interface WithCrcProvider
{
  /**
   * Return computed CRC32 on the provider data content
   * @return résultat
   * @throws IOException exception
   */
  String getCrc32() throws IOException, ProviderException;

  /**
   * crcCheckisActive
   * @return true if the crc Checked is active
   */
  boolean crcCheckisActive();
}
