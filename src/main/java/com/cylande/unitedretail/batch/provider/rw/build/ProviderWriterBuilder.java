package com.cylande.unitedretail.batch.provider.rw.build;

import com.cylande.unitedretail.batch.provider.rw.impl.StorelandProviderWriter;
import com.cylande.unitedretail.message.batch.STORELANDPROVIDER;
import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.impl.DynaFileProvider;
import com.cylande.unitedretail.batch.provider.impl.FileProvider;
import com.cylande.unitedretail.batch.provider.impl.XMLStringProvider;
import com.cylande.unitedretail.batch.provider.rw.ProviderRW;
import com.cylande.unitedretail.batch.provider.rw.impl.JMSProviderWriter;
import com.cylande.unitedretail.batch.provider.rw.impl.ProviderWriterImpl;
import com.cylande.unitedretail.message.batch.DYNAFILEPROVIDER;
import com.cylande.unitedretail.message.batch.FILEPROVIDER;
import com.cylande.unitedretail.message.batch.JMSPROVIDER;
import com.cylande.unitedretail.message.batch.TaskRunType;

/**
 * Builder from builder and loader pattern
 * <br>Build a 'providerWriter' with informations initialized by the ProviderLoader
 */
public class ProviderWriterBuilder extends ProviderRWBuilder
{
  private static final Logger LOGGER = Logger.getLogger(ProviderWriterBuilder.class);

  /**
   * Default constructor
   */
  public ProviderWriterBuilder()
  {
  }

  /**
   * Provider and provider's objects execution building orchestration
   * @return an instance of Provider
   * @throws ProviderException exception
   */
  public ProviderRW buildProvider(String pProviderName, TaskRunType pTaskRun) throws ProviderException
  {
    init();
    LOGGER.debug("----------> construction d'un provider en écriture dans le domaine " + getCurrentDomain());
    if (getProviderDef() == null)
    {
      throw new ProviderException(BatchErrorDetail.NODEFPROVIDER_PROVIDER_WRITER_ERR);
    }
    ProviderRW providerWriter = null;
    if (getProviderDef() instanceof FILEPROVIDER)
    {
      // instanciation des objets nécessaires à l'utilisation d'un flux d'écriture de type FILE
      FileProvider fp = new FileProvider(getProviderDef(), pProviderName, pTaskRun, getPropertiesManager(), getCurrentDomain(), getAlternativeDomain());
      providerWriter = new ProviderWriterImpl(fp);
    }
    else if (getProviderDef() instanceof DYNAFILEPROVIDER)
    {
      String filename = getName();
      if (filename != null && !filename.equals(""))
      {
        filename = getName() + ".xml";
        DynaFileProvider provider = new DynaFileProvider(filename, getProviderDef(), getPropertiesManager(), getCurrentDomain(), getAlternativeDomain());
        providerWriter = new ProviderWriterImpl(provider);
      }
      else
      {
        throw new ProviderException(BatchErrorDetail.DYNAFILEPROVIDER_NO_FILENAME);
      }
    }
    else if (getProviderDef() instanceof JMSPROVIDER)
    {
      XMLStringProvider provider = new XMLStringProvider(getProviderDef(), getPropertiesManager(), getCurrentDomain(), getAlternativeDomain(), null);
      providerWriter = new JMSProviderWriter(provider);
    }
    else if (getProviderDef() instanceof STORELANDPROVIDER)
    {
      XMLStringProvider provider = new XMLStringProvider(getProviderDef(), getPropertiesManager(), getCurrentDomain(), getAlternativeDomain(), null);
      providerWriter = new StorelandProviderWriter(provider);
    }
    return providerWriter;
  }
}
