package com.cylande.unitedretail.batch.provider.rw.build;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.cylande.unitedretail.message.batch.ProviderWrapperType;
import org.apache.log4j.Logger;

import com.cylande.unitedretail.batch.exception.BatchErrorDetail;
import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.Provider;
import com.cylande.unitedretail.batch.provider.WithCrcProvider;
import com.cylande.unitedretail.batch.provider.impl.DynaFileProvider;
import com.cylande.unitedretail.batch.provider.impl.FileProvider;
import com.cylande.unitedretail.batch.provider.impl.WithCrcFileProvider;
import com.cylande.unitedretail.batch.provider.impl.XMLStringProvider;
import com.cylande.unitedretail.batch.provider.rw.ProviderRW;
import com.cylande.unitedretail.batch.provider.rw.impl.JMSProviderReader;
import com.cylande.unitedretail.batch.provider.rw.impl.ProviderReaderImpl;
import com.cylande.unitedretail.batch.service.ProviderCrcManagerServiceImpl;
import com.cylande.unitedretail.framework.context.URContext;
import com.cylande.unitedretail.framework.service.BusinessServiceException;
import com.cylande.unitedretail.framework.service.TechnicalServiceException;
import com.cylande.unitedretail.message.batch.DYNAFILEPROVIDER;
import com.cylande.unitedretail.message.batch.FILEPROVIDER;
import com.cylande.unitedretail.message.batch.JMSPROVIDER;
import com.cylande.unitedretail.message.batch.ProviderCrcKeyType;
import com.cylande.unitedretail.message.batch.ProviderCrcScenarioType;
import com.cylande.unitedretail.message.batch.ProviderCrcType;
import com.cylande.unitedretail.message.batch.TaskRunType;
import com.cylande.unitedretail.message.batch.WITHCRCFILEPROVIDER;
import com.cylande.unitedretail.message.common.context.ContextType;

/**
 * Builder from builder and loader pattern
 * <br>Build a 'providerReader' with informations initialized by the ProviderLoader
 */
public class ProviderReaderBuilder extends ProviderRWBuilder
{
  private static final Logger LOGGER = Logger.getLogger(ProviderReaderBuilder.class);
  private int _buffersize = 0;
  private ContextType _ctx = null;

  /**
   * Default constructor
   */
  public ProviderReaderBuilder()
  {
  }

  /**
   *  Constructor with bufferSize parameter
   *  @param pBuffersize
   */
  public ProviderReaderBuilder(int pBuffersize)
  {
    _buffersize = pBuffersize;
  }

  /**
   * Getter sur le context
   * @return ContextType
   */
  private ContextType getContext()
  {
    if (_ctx == null)
    {
      // TODO : Traitement du Context Cylande dans le moteur de batch.
      _ctx = new ContextType();
      _ctx.setIsoLanguage(Locale.FRANCE.getLanguage());
      _ctx.setUserCode(URContext.getSecurityContext().getUserName());
    }
    return _ctx;
  }

  /**
   * Appel du Service CRUD d'histo CRC
   * methode get
   * @param pCrcKey
   * @return résultat
   * @throws TechnicalServiceException exception
   * @throws BusinessServiceException exception
   * @throws RemoteException exception
   */
  private ProviderCrcType searchCrcHistory(ProviderCrcKeyType pCrcKey) throws TechnicalServiceException, BusinessServiceException, RemoteException
  {
    ProviderCrcManagerServiceImpl histoCrcService = new ProviderCrcManagerServiceImpl();
    ProviderCrcScenarioType scenario = new ProviderCrcScenarioType();
    return histoCrcService.getProviderCrc(pCrcKey, scenario, getContext());
  }

  /**
   * Appel du Service CRUD d'histo CRC
   * methode post
   * @param pCrcInfo
   * @return résultat
   * @throws TechnicalServiceException exception
   * @throws BusinessServiceException exception
   * @throws RemoteException exception
   */
  private ProviderCrcType postCrcInfo(ProviderCrcType pCrcInfo) throws TechnicalServiceException, BusinessServiceException, RemoteException
  {
    ProviderCrcManagerServiceImpl histoCrcService = new ProviderCrcManagerServiceImpl();
    ProviderCrcScenarioType scenario = new ProviderCrcScenarioType();
    return histoCrcService.postProviderCrc(pCrcInfo, scenario, getContext());
  }

  /**
   * Appel du Service CRUD d'histo CRC
   * methode create
   * @param pCrcInfo
   * @return résultat
   * @throws TechnicalServiceException exception
   * @throws BusinessServiceException exception
   * @throws RemoteException exception
   */
  private ProviderCrcType createCrcInfo(ProviderCrcType pCrcInfo) throws TechnicalServiceException, BusinessServiceException, RemoteException
  {
    ProviderCrcManagerServiceImpl histoCrcService = new ProviderCrcManagerServiceImpl();
    ProviderCrcScenarioType scenario = new ProviderCrcScenarioType();
    return histoCrcService.createProviderCrc(pCrcInfo, scenario, getContext());
  }

  /**
   * Algo de verification du CRC et mise à jour de son histo
   * @param pProvider
   * @throws IOException exception
   * @throws TechnicalServiceException exception
   * @throws BusinessServiceException exception
   * @throws ProviderException exception
   */
  private void checkCrc(WithCrcProvider pProvider) throws IOException, TechnicalServiceException, BusinessServiceException, ProviderException
  {
    // information de Crc actuelles
    ProviderCrcType crcInfo = new ProviderCrcType();
    crcInfo.setCrc(pProvider.getCrc32());
    crcInfo.setBatchName((String)getPropertiesManager().getSysObject("batchParentPath"));
    crcInfo.setModificationTime(new GregorianCalendar());
    // recheche de CRC existant
    ProviderCrcType previousCRC = searchCrcHistory(crcInfo);
    if (previousCRC != null)
    {
      // crc existant : données probablement déja intégrées on verifie si on override
      if ("true".equals(getPropertiesManager().getProperty("overrideCRCCheck", getCurrentDomain(), getAlternativeDomain())))
      {
        // mode override : mise à jour des infos
        crcInfo.setOverridden(true);
        postCrcInfo(crcInfo);
      }
      else
      {
        // pas d'override demandé : création d'exception
        Object[] params = new Object[6];
        params[0] = getProviderDef().getName();
        params[1] = previousCRC.getModificationUserCode();
        params[2] = previousCRC.getModificationTime();
        params[3] = previousCRC.getBatchName();
        params[4] = previousCRC.getCrc();
        params[5] = previousCRC.getOverridden();
        throw new ProviderException(BatchErrorDetail.PROVIDER_CRC_ALREADY_EXISTS, params);
      }
    }
    else
    {
      // crc inconnu : creation du crc avec forceage du flag override = false
      crcInfo.setOverridden(false);
      createCrcInfo(crcInfo);
    }
  }

  /**
   * Provider and provider's objects execution building orchestration
   * @return an instance of ProviderReader as ProviderRW
   * @throws ProviderException exception
   */
  public ProviderRW buildProvider(String pProviderName, TaskRunType pTaskRun) throws ProviderException
  {
    init();
    LOGGER.debug("-------> construction d'un provider de lecture dans le domaine " + getCurrentDomain());
    if (getProviderDef() == null)
    {
      throw new ProviderException(BatchErrorDetail.NODEFPROVIDER_PROVIDER_READER_ERR);
    }
    ProviderRW result = null;
    Provider provider = null;
    try
    {
      final ProviderWrapperType wrapper = getProviderDef().getWrap();
      //construction du provider
      if (getProviderDef() instanceof FILEPROVIDER)
      {
        if (getProviderDef() instanceof WITHCRCFILEPROVIDER)
        {
          provider = new WithCrcFileProvider(getProviderDef(), getPropertiesManager(), getCurrentDomain(), getAlternativeDomain());
        }
        else
        {
          provider = new FileProvider(getProviderDef(), pProviderName, pTaskRun, getPropertiesManager(), getCurrentDomain(), getAlternativeDomain());
        }
        result = new ProviderReaderImpl(provider, wrapper, _buffersize, null, null);
      }
      else if (getProviderDef() instanceof DYNAFILEPROVIDER)
      {
        String filename = getName();
        if (filename != null && !filename.equals(""))
        {
          filename += ".xml";
          provider = new DynaFileProvider(filename, getProviderDef(), getPropertiesManager(), getCurrentDomain(), getAlternativeDomain());
        }
        else
        {
          throw new ProviderException(BatchErrorDetail.DYNAFILEPROVIDER_NO_FILENAME);
        }
        result = new ProviderReaderImpl(provider, wrapper, _buffersize, null, null);
      }
      else if (getProviderDef() instanceof JMSPROVIDER)
      {
        final String batchName = (String)getPropertiesManager().getSysObject("batchParentPath");
        provider = new XMLStringProvider(getProviderDef(), getPropertiesManager(), getCurrentDomain(), getAlternativeDomain(), null);
        result = new JMSProviderReader(provider, wrapper, _buffersize, null, null, batchName);
      }
      //provider.setCurrentDomain(getCurrentDomain()); -> pas besoin car passé au constructeur
      // Vérification du CRC le cas échéant
      if ((provider instanceof WithCrcProvider) && ((WithCrcProvider)provider).crcCheckisActive())
      {
        checkCrc((WithCrcProvider)provider);
      }
    }
    catch (ProviderException e)
    {
      throw e;
    }
    catch (FileNotFoundException e)
    {
      throw new ProviderException(BatchErrorDetail.FILE_PROVIDER_READER_NOTFOUND, new Object[] { getProviderDef().getName() }, e);
    }
    catch (Exception e)
    {
      throw new ProviderException(BatchErrorDetail.BUILD_PROVIDER_READER_ERR, new Object[] { getProviderDef().getName() }, e);
    }
    return result;
  }
}
