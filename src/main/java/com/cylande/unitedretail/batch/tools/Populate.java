package com.cylande.unitedretail.batch.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.cylande.unitedretail.framework.time.URDate;
import com.cylande.unitedretail.framework.time.URDateTime;
import com.cylande.unitedretail.framework.time.URTime;

public class Populate
{
  private static final String NS2_ATTR_REGEX = " xmlns:ns2=\".*\"";
  // Variables à modifier
  private int _nbMasterList = 1; // Nb d'objet à ajouter dans le fichier xml
  private int _nbChildList = 1; // Nb de liste enfant à générer
  private int _lengthGeneratedString = 4; // Taille des string à générer
  private int _lengthGeneratedInt = 3; // Taille des int à générer (pas plus de 8)
  private boolean _manageUnique = true; // Savoir si on gère les hashMap, mettre false dans les cas de grande génération
  // Autres
  private int _currentList = 0; // Sert à savoir à quelle liste on est (pour les translations)
  private String _masterName = ""; // Le nom de la liste maître
  private String _currentName = ""; // Le nom des différentes listes enfants
  private JAXBContext _context;
  private Object _myObject = null;
  // Le tableau de langage qui va nous permettre de remplir nos types Translations
  private List<String> _languageList = Arrays.asList(new String[] { "FR", "EN", "DE", "ES", "IT" });
  private Map<String, String> _generatedStringMap = new HashMap();
  private Map<Integer, String> _generatedIntMap = new HashMap();
  private Map<Double, String> _generatedDoubleMap = new HashMap();
  private Map<String, String> _populateCacheMap = new HashMap();
  private Map<String, Object> _beanCacheMap = new HashMap();
  // permet de sauvegarder les niveaux de profondeur des types rencontrés afin d'éviter les définitions cycliques (LinkPersonType, PersonType)
  private Map<String, Integer> _typeMap;

  public Populate()
  {
  }

  public String create(Class pClass) throws Exception
  {
    String result = _populateCacheMap.get(pClass.getName());
    _typeMap = new HashMap();
    _masterName = pClass.getName().substring(pClass.getName().lastIndexOf(".") + 1);
    if (result == null && !pClass.equals(String.class))
    {
      _currentList = 0;
      _myObject = _beanCacheMap.get(pClass.getName());
      if (_myObject == null)
      {
        _myObject = createBean(pClass, 1);
      }
      _context = JAXBContext.newInstance(pClass.getPackage().getName());
      Marshaller marshaller = _context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true); // suppression déclaration XML
      StringWriter writer = new StringWriter();
      marshaller.marshal(_myObject, writer);
      result = writer.toString();
      result = result.replaceFirst(NS2_ATTR_REGEX, "");
      _populateCacheMap.put(pClass.getName(), result);
    }
    return result;
  }

  public void create(Class pClass, String pTargetDir) throws Exception
  {
    _typeMap = new HashMap();
    _masterName = pClass.getName().substring(pClass.getName().lastIndexOf(".") + 1);
    if (!pClass.equals(String.class))
    {
      _currentList = 0;
      _myObject = _beanCacheMap.get(pClass.getName());
      if (_myObject == null)
      {
        _myObject = createBean(pClass, 1);
      }
      _context = JAXBContext.newInstance(pClass.getPackage().getName());
      Marshaller marshaller = _context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      String packageName = pClass.getPackage().getName().replace('.', '/');
      new File(pTargetDir + packageName).mkdirs();
      marshaller.marshal(_myObject, new FileWriter(pTargetDir + pClass.getName().replace('.', '/') + ".xml"));
    }
  }

  private Object createBean(Class pClass, Integer pDeep) throws Exception
  {
    _currentName = pClass.getName().substring(pClass.getName().lastIndexOf(".") + 1);
    Object result = null;
    result = getValue(pClass);
    if (result != null)
    {
      return result;
    }
    result = _beanCacheMap.get(pClass.getName());
    if (result == null)
    {
      // Si on a un type particulier
      if (pClass.getName().contains("com.cylande.unitedretail.message"))
      {
        if (pClass.getEnumConstants() != null)
        {
          return pClass.getEnumConstants()[0];
        }
        if (!Modifier.isAbstract(pClass.getModifiers()))
        {
          result = pClass.newInstance();
        }
      }
      if (result != null && (_typeMap.get(_currentName) == null || pDeep <= _typeMap.get(_currentName)))
      {
        _typeMap.put(_currentName, pDeep);
        for (Method m: pClass.getMethods())
        {
          // Si la méthode commence par Set
          if (m.getName().startsWith("set") && m.getParameterTypes().length > 0)
          {
            m.invoke(result, getParamValue(m, pDeep));
          }
        }
        _beanCacheMap.put(pClass.getName(), result);
      }
    }
    return result;
  }

  private Object getValue(Class pClass)
  {
    Object result = null;
    if (pClass == String.class)
    {
      // On set une string générée aléatoirement
      result = generateString();
      while (result == null)
      {
        result = generateString();
      }
      // Si on a un string on set le nom du param, sans le set, si on a language, on set FR
      if (pClass.getName().contains("Language"))
      {
        result = "FR";
      }
      // Si on est dans un type translation, on va différencier les languages
      if (_currentName.contains("translation") || _currentName.contains("translations") || _currentName.contains("Translation") || _currentName.contains("Translations"))
      {
        if (pClass.getName().contains("Language"))
        {
          if (_currentList < _languageList.size())
          {
            result = _languageList.get(_currentList);
          }
          else
          {
            _currentList = 0;
            result = _languageList.get(_currentList);
          }
          _currentList++;
          if (_currentList > _nbChildList - 1)
          {
            _currentList = 0;
          }
        }
      }
      if (pClass.getName().contains("Email"))
      {
        result = "test@test.fr";
      }
    }
    else if (pClass == Calendar.class)
    {
      // Si on a une deletionTime, on ne set pas de Calendar
      result = pClass.getName().contains("DeletionTime") || pClass.getName().contains("DeleteTime") ? null : Calendar.getInstance();
    }
    else if (pClass == URTime.class)
    {
      // Si on a une deletionTime, on ne set pas de Calendar
      result = pClass.getName().contains("DeletionTime") || pClass.getName().contains("DeleteTime") ? null : URTime.getInstance();
    }
    else if (pClass == URDateTime.class)
    {
      // Si on a une deletionTime, on ne set pas de Calendar
      result = pClass.getName().contains("DeletionTime") || pClass.getName().contains("DeleteTime") ? null : URDateTime.getInstance();
    }
    else if (pClass == URDate.class)
    {
      // Si on a une deletionTime, on ne set pas de Calendar
      result = pClass.getName().contains("DeletionTime") || pClass.getName().contains("DeleteTime") ? null : URDate.getInstance();
    }
    else if (pClass == Double.class || pClass == BigDecimal.class)
    {
      result = generateDouble();
      while (result == null)
      {
        result = generateDouble();
      }
      if (pClass == BigDecimal.class)
      {
        result = BigDecimal.valueOf((Double)result);
      }
    }
    else if (pClass == Integer.class || pClass == Long.class || pClass == BigInteger.class)
    {
      result = generateInt();
      if (pClass == Long.class)
      {
        result = Long.valueOf((Integer)result);
      }
      else if (pClass == BigInteger.class)
      {
        result = BigInteger.valueOf(Long.valueOf((Integer)result));
      }
    }
    else if (pClass == Boolean.class)
    {
      result = Boolean.TRUE;
    }
    return result;
  }

  private Object[] getParamValue(Method pMethod, Integer pDeep) throws Exception, ClassNotFoundException
  {
    Object[] param = new Object[1];
    if (pMethod.getParameterTypes()[0] == List.class)
    {
      if (_masterName.equals(_currentName))
      {
        // On récupère le type à setter dans la liste
        String listElement = pMethod.getGenericParameterTypes()[0].toString().replaceFirst("^.*List<(.+)>.*$", "$1");
        List list = new ArrayList();
        for (int i = 0; i < _nbMasterList; i++)
        {
          list.add(createBean(Class.forName(listElement), pDeep + 1));
        }
        // On rappel notre méthode populate, avec notre liste
        param[0] = list;
      }
      else
      {
        // On récupère le type à setter dans la liste
        String listElement = pMethod.getGenericParameterTypes()[0].toString().replaceFirst("^.*List<(.+)>.*$", "$1");
        List list = new ArrayList();
        for (int j = 0; j < _nbChildList; j++)
        {
          list.add(createBean(Class.forName(listElement), pDeep + 1));
        }
        // On rappel notre méthode populate, avec notre liste
        param[0] = list;
      }
    }
    else
    {
      String type = pMethod.getParameterTypes()[0].getName();
      if ("int".equals(type))
      {
        param[0] = generateInt();
      }
      else if ("double".equals(type))
      {
        param[0] = generateDouble();
      }
      else
      {
        Class paramClass = Class.forName(pMethod.getParameterTypes()[0].getName());
        param[0] = createBean(paramClass, pDeep + 1);
      }
    }
    return param;
  }

  private String generateString()
  {
    StringBuffer result = new StringBuffer();
    Random asciiRd = new Random();
    int ascii = 91;
    for (int i = 0; i < _lengthGeneratedString; i++)
    {
      // On récupère un chiffre aléatoire sur la plage de code ascii correspondant aux lettres majuscules et minucsules
      while (ascii > 90 && ascii < 97)
      {
        ascii = asciiRd.nextInt(122 - 65) + 65;
      }
      result.append((char)ascii);
      ascii = 91;
    }
    if (Boolean.TRUE.equals(_manageUnique))
    {
      if (_generatedStringMap.get(result.toString()) == null)
      {
        _generatedStringMap.put(result.toString(), "");
      }
      else
      {
        return null;
      }
    }
    return result.toString();
  }

  private Integer generateInt()
  {
    int result = 0;
    Random rd = new Random();
    while (result == 0)
    {
      int myInt = rd.nextInt(99999999 - 10000000) + 10000000;
      // Si le chiffre est positif, on le tronc
      if (myInt > 0)
      {
        String parseur = String.valueOf(myInt);
        parseur = parseur.substring(0, _lengthGeneratedInt);
        myInt = Integer.parseInt(parseur);
        result = myInt;
      }
    }
    if (Boolean.TRUE.equals(_manageUnique))
    {
      if (_generatedIntMap.get(result) == null)
      {
        _generatedIntMap.put(result, "");
      }
      else
      {
        return 0;
      }
    }
    return result;
  }

  private Double generateDouble()
  {
    Double result = null;
    String dblGenerate = "";
    Random rd = new Random();
    String first = String.valueOf(rd.nextInt(99 - 10) + 10);
    String sec = String.valueOf(rd.nextInt(99 - 10) + 10);
    dblGenerate = first + "." + sec;
    if (Boolean.TRUE.equals(_manageUnique))
    {
      result = Double.parseDouble(dblGenerate);
      if (_generatedDoubleMap.get(result) == null)
      {
        _generatedDoubleMap.put(result, "");
      }
    }
    return result;
  }
}
