package com.cylande.unitedretail.common.transformer;

import com.cylande.unitedretail.common.business.exception.ContextBusinessUnitLoadException;
import com.cylande.unitedretail.common.business.exception.ContextException;
import com.cylande.unitedretail.common.exception.CommonErrorDetail;
import com.cylande.unitedretail.common.tools.IntegerManager;
import com.cylande.unitedretail.common.tools.ServiceDelegateClient;
import com.cylande.unitedretail.common.tools.StringManager;
import com.cylande.unitedretail.framework.service.ServiceException;
import com.cylande.unitedretail.framework.service.TechnicalServiceNotDeliveredException;
import com.cylande.unitedretail.message.common.codification.CodificationKeyType;
import com.cylande.unitedretail.message.common.codification.CodificationMultipleKeyType;
import com.cylande.unitedretail.message.common.context.ContextType;
import com.cylande.unitedretail.message.common.enums.EnumBuModule;
import com.cylande.unitedretail.message.network.businessunit.BusinessUnitKeyType;
import com.cylande.unitedretail.message.network.businessunit.BusinessUnitScenarioType;
import com.cylande.unitedretail.message.network.businessunit.BusinessUnitType;
import com.cylande.unitedretail.message.network.businessunitmodulereferencingmode.BusinessUnitModuleReferencingModeType;
import com.cylande.unitedretail.message.network.enums.EnumReferencingMode;
import com.cylande.unitedretail.message.resource.BusinessFunctionScenarioType;
import com.cylande.unitedretail.message.resource.BusinessFunctionType;
import com.cylande.unitedretail.network.service.BusinessUnitEngineServiceDelegate;
import com.cylande.unitedretail.portal.service.BusinessFunctionManagerServiceDelegate;

import java.rmi.RemoteException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Transformations liées à la gestion du contexte.
 * @author Cylande
 * @version 1.0
 * @since 17/06/2008
 */
public class ContextTransformer
{
  /**
   * Map de correspondance entre la fonction métier et son module
   */
  private static final Map<String, String> BF_MODULE_MAP = new HashMap<String, String>();

  /**
   * Complète le context avec les données par défaut.
   * @param pContext : le contexte d'utilisation
   * @return le contexte renseigné
   * @throws RemoteException
   * @throws ServiceException
   * @since 17/06/2008
   */
  public static ContextType getDefault(ContextType pContext) throws RemoteException, ServiceException
  {
    if (pContext == null)
    {
      pContext = new ContextType();
    }
    if (pContext.getLanguage() == null)
    {
      getDefaultLanguageContext(pContext);
    }
    if (pContext.getSite() == null)
    {
      pContext.setSite(getLocalSite(pContext));
    }
    return pContext;
  }

  /**
   * Récupération de la langue par défaut.
   * @param pContext : le context d'utilisation
   * @throws RemoteException
   * @throws ServiceException
   * @since 17/06/2008
   */
  public static void getDefaultLanguageContext(ContextType pContext) throws RemoteException, ServiceException
  {
    if (pContext != null)
    {
      // le iso language doit toujours être renseigné
      // Si non renseigné en base, on ne peut pas prendre le code de la langue par défaut en base qui peut être un numérique (paramétrage Storeland bridge)
      pContext.setLanguage(Locale.getDefault().getLanguage().toUpperCase());
      pContext.setIsoLanguage(Locale.getDefault().getLanguage());
    }
  }

  /**
   * Récupération du site local
   * @param pContext le contexte d'utilisation
   */
  public static CodificationMultipleKeyType getLocalSite(ContextType pContext)
  {
    CodificationMultipleKeyType result = null;
    if (pContext != null)
    {
      result = new CodificationMultipleKeyType();
      result.setId(1);
      result.setCode("1");
    }
    return result;
  }

  /**
   * Récupération de la businessUnit du contexte.
   * @param pContext : le context d'utilisation
   * @throws RemoteException
   * @throws ServiceException
   * @since 01/07/2008
   */
  public static BusinessUnitType getBusinessUnitContext(ContextType pContext)
  {
    if (pContext != null && pContext.getBusinessUnit() != null)
    {
      BusinessUnitEngineServiceDelegate businessUnitEngineService = new BusinessUnitEngineServiceDelegate();
      BusinessUnitKeyType buKey = new BusinessUnitKeyType();
      buKey.setId(pContext.getBusinessUnit());
      BusinessUnitType contextBusinessUnit;
      BusinessUnitScenarioType pScenario = new BusinessUnitScenarioType();
      pScenario.setManageBarCodeType(true);
      pScenario.setManageModuleReferencingModes(true);
      try
      {
        contextBusinessUnit = businessUnitEngineService.getBusinessUnit(buKey, pScenario, pContext);
      }
      catch (ServiceException e)
      {
        throw new ContextBusinessUnitLoadException(CommonErrorDetail.CONTEXT_BUSINESS_UNIT_LOAD_ERROR);
      }
      return contextBusinessUnit;
    }
    else
    {
      return null;
    }
  }

  /**
   * Transformation du contexte en localisation.
   * @param pContext : le contexte d'utilisation
   * @return : la localisation corrrespondante au contexte
   * @since 17/06/2008
   */
  public static Locale toLocale(ContextType pContext)
  {
    Locale locale = null;
    if (pContext != null && pContext.getIsoLanguage() != null)
    {
      String[] locParams = pContext.getIsoLanguage().split("_");
      if (locParams.length > 1)
      {
        locale = new Locale(locParams[0], locParams[1], "");
      }
      else if (locParams.length > 0)
      {
        locale = new Locale(locParams[0], "", "");
      }
    }
    if (locale == null && pContext != null && pContext.getLanguage() != null)
    {
      locale = new Locale(pContext.getLanguage().toLowerCase());
    }
    if (locale == null)
    {
      locale = Locale.getDefault();
    }
    return locale;
  }

  /**
   * Initialise un contexte vierge à partir de la localisation.
   * @return un contexte d'utilisation
   * @since 17/06/2008
   */
  public static ContextType fromLocale()
  {
    ContextType result = new ContextType();
    result.setLanguage(Locale.getDefault().getLanguage().toUpperCase());
    return result;
  }

  /** Définit en fonction du Type de Référencement la BusinessUnit à utiliser
   * Si le mode de référencement est Autonome ou Mixte, on retourne la BU du contexte
   * Si le mode de référencement est Centralisé, on retourne la BU de référece du module dans la BU du contexte
   * @param pContext Contexte d'utilisation
   * @return Integer BU du contexte ou BU de référence
   * @since 24/09/22008
   */
  public static Integer getReferencingBusinessUnit(String pModule, ContextType pContext)
  {
    Integer result = null;
    if (pContext != null)
    {
      // Récupération de la BU du contexte.
      BusinessUnitType businessUnit = getBusinessUnitContext(pContext);
      if (businessUnit != null)
      {
        // Récupération du mode de référencement du module métier dans la BU du contexte.
        BusinessUnitModuleReferencingModeType refMode = getReferencingModeForModule(getModule(pModule, pContext), businessUnit);
        // Si le mode de référencement est centralisé
        if (refMode != null && EnumReferencingMode.CONCENTRATED.value().equals(refMode.getReferencingMode()))
        {
          result = refMode.getReferencingBusinessUnit();
        }
      }
      // par défaut la BU est la BU du contexte
      if (result == null)
      {
        result = pContext.getBusinessUnit();
      }
    }
    return result;
  }

  /** Définit en fonction du Type de Référencement la BusinessUnit à utiliser
   * Si le mode de référencement est Autonome ou Mixte, on retourne la Bu en paramètre
   * Si le mode de référencement est Centralisé, on retourne la BU de référece du module dans la BU en paramètre
   * @param pBusinessUnitId Bu à utiliser
   * @param pContext Contexte d'utilisation
   * @return Integer BU en paramètre ou BU de référence
   */
  public static Integer getReferencingBusinessUnit(String pModule, Integer pBusinessUnitId, ContextType pContext)
  {
    Integer result = null;
    if (!IntegerManager.isZero(pBusinessUnitId))
    {
      // Récupération de la BU en paramètre
      BusinessUnitScenarioType scenario = new BusinessUnitScenarioType();
      scenario.setManageModuleReferencingModes(true);
      BusinessUnitType businessUnit = ServiceDelegateClient.getBusinessUnit(pBusinessUnitId, scenario, pContext);
      if (businessUnit != null)
      {
        // Récupération du mode de référencement du module métier pour la bu en paramètree.
        BusinessUnitModuleReferencingModeType refMode = getReferencingModeForModule(getModule(pModule, pContext), businessUnit);
        // Si le mode de référencement est centralisé
        if (refMode != null && EnumReferencingMode.CONCENTRATED.value().equals(refMode.getReferencingMode()))
        {
          result = refMode.getReferencingBusinessUnit();
        }
      }
      // par défaut la BU est celle en paramètre
      if (result == null)
      {
        result = pBusinessUnitId;
      }
    }
    return result;
  }

  /**
   * Recherche du module
   * @param pModule module
   * @param pContext Contexte d'utilisation
   * @return String
   */
  private static String getModule(String pModule, ContextType pContext)
  {
    String module = pModule;
    if (module == null)
    {
      BusinessFunctionType businessFunction = getBusinessFunction(pContext);
      if (businessFunction != null)
      {
        module = businessFunction.getModule();
      }
    }
    return module;
  }

  /**
   * Recherche de l'identifiant de la BU autonome de référence correspondant au contexte
   * @param pContext Contexte d'utilisation
   * @return Integer Identifiant de la BU autonome de référence
   */
  public static Integer getIndependantBusinessUnit(String pModule, ContextType pContext)
  {
    Integer result = null;
    if (pContext != null)
    {
      // Récupération de la BU du contexte.
      BusinessUnitType businessUnit = getBusinessUnitContext(pContext);
      if (businessUnit != null)
      {
        // Récupération du mode de référencement du module métier dans la BU du contexte.
        BusinessUnitModuleReferencingModeType refMode = getReferencingModeForModule(getModule(pModule, pContext), businessUnit);
        // Si le mode de référencement est centralisé ou mixte
        if ((refMode != null) && (refMode.getParentBusinessUnit() != null) && (refMode.getParentBusinessUnit().size() > 0))
        {
          result = refMode.getParentBusinessUnit().get(refMode.getParentBusinessUnit().size() - 1);
        }
      }
      // par défaut la BU est la BU du contexte
      if (result == null)
      {
        result = pContext.getBusinessUnit();
      }
    }
    return result;
  }

  /**
   * Retourne la valeur du champ synchro du contexte
   * @param pContext contexte
   * @return boolean
   */
  public static boolean isSynchro(ContextType pContext)
  {
    return pContext != null && pContext.getSynchro() != null && pContext.getSynchro().booleanValue();
  }

  /**
   * Retourne le paramétrage de la BusinessFunction incluse dans le contexte
   * @param pContext le contexte
   * @return la BusinessFunction
   */
  public static BusinessFunctionType getBusinessFunction(ContextType pContext)
  {
    BusinessFunctionType result = null;
    if (pContext != null && pContext.getBusinessFunction() != null)
    {
      try
      {
        BusinessFunctionManagerServiceDelegate businessFunctionService = new BusinessFunctionManagerServiceDelegate();
        CodificationKeyType bfKey = new CodificationKeyType();
        bfKey.setCode(pContext.getBusinessFunction());
        BusinessFunctionScenarioType scenario = new BusinessFunctionScenarioType();
        scenario.setManageBusinessParameter(false);
        scenario.setManageCustomize(false);
        scenario.setManageReportParameter(false);
        scenario.setManageRole(false);
        scenario.setManageScreenParameter(false);
        scenario.setNoDescription(true);
        result = businessFunctionService.getBusinessFunction(bfKey, scenario, pContext);
      }
      catch (TechnicalServiceNotDeliveredException ndException)
      {
      }
      catch (ServiceException e)
      {
        throw new ContextException(CommonErrorDetail.FIND_BUSINESS_FUNCTION_SERVICE_DELEGATE_BUSINESS_ERROR);
      }
    }
    return result;
  }

  /**
   * Retourne le mode de référencement lié à une businessUnit pour un module métier donné
   * @param pModule le module
   * @param pBusinessUnit la businessUnit
   * @return Le mode de référencement
   */
  public static BusinessUnitModuleReferencingModeType getReferencingModeForModule(String pModule, BusinessUnitType pBusinessUnit)
  {
    BusinessUnitModuleReferencingModeType result = null;
    if (pModule != null && pBusinessUnit != null && pBusinessUnit.getModuleReferencingModes() != null)
    {
      // parcours des modes de référencement inclus dans la BU
      int len = pBusinessUnit.getModuleReferencingModes().size();
      int i = 0;
      BusinessUnitModuleReferencingModeType refMode = null;
      while (i < len && result == null)
      {
        refMode = pBusinessUnit.getModuleReferencingModes().get(i);
        if (refMode != null && refMode.getModule() != null && pModule.equals(refMode.getModule().getCode()))
        {
          result = refMode;
        }
        i++;
      }
    }
    return result;
  }

  /**
   * Retourne le mode de référencement lié à une businessUnit
   * La méthode récupère la fonction métier du contexte pour déterminer le module
   * métier pour lequel il faut ramener le mode de référencement
   * @param pModule Module pour lequel il faut retrouver le mode de référence
   * @param pContext Contexte qui indique la bu concernée
   * @return Le mode de référencement
   */
  public static BusinessUnitModuleReferencingModeType getReferencingMode(EnumBuModule pModule, ContextType pContext)
  {
    BusinessUnitModuleReferencingModeType result = null;
    if (pContext != null)
    {
      BusinessUnitType businessUnit = getBusinessUnitContext(pContext);
      if (businessUnit != null)
      {
        // Récupération du mode de référencement du module métier dans la BU du contexte.
        result = getReferencingModeForModule(pModule.value(), businessUnit);
      }
    }
    return result;
  }

  /**
   * Retourne le code du module défini pour la fonction métier du contexte.
   * @param pContext Contexte d'utilisation
   * @return String
   */
  public static String getContextBusinessFunctionModule(ContextType pContext)
  {
    String result = null;
    if (pContext != null && !StringManager.isEmpty(pContext.getBusinessFunction()))
    {
      if (BF_MODULE_MAP.get(pContext.getBusinessFunction()) == null)
      {
        BusinessFunctionType businessFunction = getBusinessFunction(pContext);
        if (businessFunction != null)
        {
          BF_MODULE_MAP.put(pContext.getBusinessFunction(), businessFunction.getModule());
        }
      }
      result = BF_MODULE_MAP.get(pContext.getBusinessFunction());
    }
    return result;
  }
}
