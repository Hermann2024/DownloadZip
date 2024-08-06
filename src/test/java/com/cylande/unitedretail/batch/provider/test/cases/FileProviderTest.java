package com.cylande.unitedretail.batch.provider.test.cases;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.cylande.unitedretail.batch.exception.ProviderException;
import com.cylande.unitedretail.batch.provider.DataPackage;
import com.cylande.unitedretail.batch.provider.impl.FileProvider;
import com.cylande.unitedretail.batch.provider.impl.FileProviderManager;
import com.cylande.unitedretail.batch.provider.rw.ProviderReader;
import com.cylande.unitedretail.batch.provider.rw.ProviderWriter;
import com.cylande.unitedretail.batch.provider.rw.impl.ProviderReaderImpl;
import com.cylande.unitedretail.batch.provider.rw.impl.ProviderWriterImpl;
import com.cylande.unitedretail.batch.service.MapperManagerServiceImpl;
import com.cylande.unitedretail.common.transformer.ContextTransformer;
import com.cylande.unitedretail.message.batch.CylandeTemplateType;
import com.cylande.unitedretail.message.batch.FILEPROVIDER;
import com.cylande.unitedretail.message.batch.FileSortEnum;
import com.cylande.unitedretail.message.batch.JSONFileType;
import com.cylande.unitedretail.message.batch.MapperLineCriteriaType;
import com.cylande.unitedretail.message.batch.MapperLineFilterType;
import com.cylande.unitedretail.message.batch.MapperType;
import com.cylande.unitedretail.message.batch.ProviderFileType;
import com.cylande.unitedretail.message.batch.ProviderMapperType;
import com.cylande.unitedretail.process.tools.PropertiesManager;

public class FileProviderTest extends TestCase
{

  protected void setUp() throws Exception
  {
    // l'impl�mentation doit �tre forc�e sinon Maven utilise celle du jar wstx-asl qui fait planter le test dans le StaxXMLParser
    System.setProperty("javax.xml.stream.XMLOutputFactory", "com.sun.xml.stream.ZephyrWriterFactory");
    FileProviderManager.setTemporyDirectoryName(new File("").getAbsolutePath());
  }

  /**
   * Test de lecture de 2 fichiers comportant 4 �l�ments chacun avec une taille de paquet de 2 : on v�rifie que la lecture ne g�n�re bien que 4 paquets de donn�es
   * Ce test correspond � la simulation d'un batch d'int�gration avec un commitFrequency de 2
   */
  public void testProviderReader()
  {
    FILEPROVIDER provider = new FILEPROVIDER();
    provider.setName("providerName");
    ProviderFileType file = new ProviderFileType();
    file.setDir(new File("").getAbsolutePath() + "/src/test/resources");
    file.setFileName("providerReader_*.xml");
    file.setSortType(FileSortEnum.NAME);
    provider.setFile(file);
    try
    {
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderReader reader = new ProviderReaderImpl(fileProvider, 2, null, null);
      DataPackage data = reader.read(null, null);
      assertEquals("providerReader_1.xml", reader.getCurrentFileName());
      assertFalse("cela ne doit pas etre le dernier paquet", data.isLastPackage());
      assertEquals("l'index du paquet n'est pas correct", 1, data.getPackageNumber());
      data = reader.read(null, null);
      assertFalse("cela ne doit pas etre le dernier paquet", data.isLastPackage());
      assertEquals("l'index du paquet n'est pas correct", 2, data.getPackageNumber());
      data = reader.read(null, null);
      assertEquals("providerReader_2.xml", reader.getCurrentFileName());
      assertFalse("cela ne doit pas etre le dernier paquet", data.isLastPackage());
      assertEquals("l'index du paquet n'est pas correct", 3, data.getPackageNumber());
      data = reader.read(null, null);
      assertEquals("l'index du paquet n'est pas correct", 4, data.getPackageNumber());
      // teste qu'il n'y a plus rien � lire apr�s la lecture du dernier paquet attendu
      data = reader.read(null, null);
      assertNull("must be null", data);
      reader.releaseProvider();
    }
    catch (Exception e)
    {
      fail();
    }
  }

  public void testProviderReaderWithFlag()
  {
    FILEPROVIDER provider = new FILEPROVIDER();
    provider.setName("providerName");
    ProviderFileType file = new ProviderFileType();
    file.setDir(new File("").getAbsolutePath() + "/src/test/resources");
    file.setFileName("providerReader_*.xml");
    file.setFlagName("${SYSTEM_inputFileNameNoExt}.flag");
    file.setSortType(FileSortEnum.NAME);
    provider.setFile(file);
    try
    {
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderReader reader = new ProviderReaderImpl(fileProvider, 0, null, null);
      DataPackage data = reader.read(null, null);
      assertEquals("providerReader_2.xml", reader.getCurrentFileName());
      assertNotNull(data);
      data = reader.read(null, null);
      assertNull("must be null", data);
      reader.releaseProvider();
      File flag = new File(file.getDir() + "/providerReader_2.flag");
      flag.createNewFile();
    }
    catch (Exception e)
    {
      fail();
    }
  }

  public void testProviderReaderWithNoProcessor()
  {
    FILEPROVIDER provider = new FILEPROVIDER();
    provider.setName("providerName");
    ProviderFileType file = new ProviderFileType();
    file.setDir(new File("").getAbsolutePath() + "/src/test/resources");
    file.setFileName("providerReader_*.xml");
    file.setSortType(FileSortEnum.NAME);
    file.setDescSort(true);
    provider.setFile(file);
    try
    {
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderReader reader = new ProviderReaderImpl(fileProvider, 0, null, null);
      DataPackage data = reader.read(true, null);
      assertEquals("providerReader_2.xml", reader.getCurrentFileName());
      assertNull("must be null", data.getValue());
      data = reader.read(true, null);
      assertEquals("providerReader_1.xml", reader.getCurrentFileName());
      assertNull("must be null", data.getValue());
      reader.releaseProvider();
    }
    catch (Exception e)
    {
      fail();
    }
  }

  /**
   * Test qu'on a bien une exception quand il n'y pas de provider d'entr�e
   * Ce test correspond � la simulation d'un batch sans provider d'entr�e
   */
  public void testProviderReaderExceptionWithoutProvider()
  {
    try
    {
      new ProviderReaderImpl(null, 0, null, null);
    }
    catch (ProviderException e)
    {
      assertEquals(e.getCanonicalCode(), "URHO-ENG-BAT-IT0132");
    }
  }

  /**
   * Le fichier personListTypeToCSV.xml contient la d�finition de plusieurs linkedPersons r�partis sur 3 niveaux de profondeur.
   * Le template associ� contenant la description des linkedPersons pour les niveaux 1 et 3, le 2�me niveau ne doit jamais apparaitre dans le fichier cr��.
   * Ce test permet ainsi de v�rifier que seuls les types d�crits dans le template pour un niveau de profondeur donn� apparaissent dans le fichier de sortie.
   * Il permet �galement de v�rifier que les valeurs d'un niveau n'�crasent pas les valeurs d'un autre.
   */
  public void testProviderWriterWithMapper()
  {
    try
    {
      MapperManagerServiceImpl serv = new MapperManagerServiceImpl();
      MapperType mapper = new MapperType();
      mapper.setName("mapper");
      mapper.setTemplate(new CylandeTemplateType());
      mapper.getTemplate().setDir(new File("").getAbsolutePath() + "/src/test/resources");
      mapper.getTemplate().setFile("personTemplate.txt");
      serv.createMapper(mapper, null, ContextTransformer.fromLocale());
      FILEPROVIDER provider = new FILEPROVIDER();
      provider.setName("providerName");
      provider.setMapper(new ProviderMapperType());
      provider.getMapper().setRef("mapper");
      ProviderFileType file = new ProviderFileType();
      file.setDir(new File("").getAbsolutePath() + "/src/test/resources");
      file.setFileName("personListType.csv");
      file.setSortType(FileSortEnum.NAME);
      file.setOverWrite(true);
      provider.setFile(file);
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderWriter writer = new ProviderWriterImpl(fileProvider);
      Reader reader = new FileReader(new File("").getAbsolutePath() + "/src/test/resources/personListTypeToCSV.xml");
      String xml = IOUtils.toString(reader).replaceAll("(?m)(?<=^ *)\\s+", "").replaceAll("[\\n\\r]", ""); // suppression de l'indentation et des sauts de ligne
      writer.write(xml);
      writer.releaseProvider();
      reader = new FileReader(new File("").getAbsolutePath() + "/src/test/resources/" + file.getFileName());
      String csv = IOUtils.toString(reader);
      assertFalse(csv.contains("LINKED PERSON NIVEAU 2|")); // test de la non pr�sence du niveau 2
      assertTrue(csv.contains("VALUES|1|France|()|defaultValue")); // test de pr�sence de la transco et de la valeur par d�faut
      assertFalse(csv.contains("VALUES|1|France|()|defaultValue|")); // test de la non pr�sence du s�parateur de ligne � la fin de la ligne
      assertTrue(csv.contains("LINKED PERSON NIVEAU 1|11|"));
      assertTrue(csv.contains("LINKED PERSON NIVEAU 3|1111|"));
      assertTrue(csv.contains("LINKED PERSON NIVEAU 1|12|"));
      assertTrue(csv.contains("LINKED PERSON NIVEAU 3|1221|"));
      assertTrue(csv.contains("VALUES|2|Belgique")); // test de pr�sence de la 2�me transco
      assertTrue(csv.contains("LINKED PERSON NIVEAU 1|21|"));
      assertTrue(csv.contains("LINKED PERSON NIVEAU 3|2111|"));
      assertTrue(csv.contains("VALUES|3|GB")); // test de la pr�sence de la donn�e non transcodifi�e
    }
    catch (Exception e)
    {
      fail();
    }
  }

  /**
   * Permet de tester le bon affichage de diff�rents formats pour les chaines, entiers, flottants et dates ainsi que :
   * - la ventilation d'une donn�es sur plusieurs champs
   * - l'application par d�faut de l'arrondi bancaire sur un nombre flottant
   * - l'application de la r�gle de r�trocompatibilit� o� si seule la largeur du champ est pr�cis�e, la donn�e est alors justifi�e � gauche
   */
  public void testProviderWriterWithMapperFormat()
  {
    try
    {
      MapperManagerServiceImpl serv = new MapperManagerServiceImpl();
      MapperType mapper = new MapperType();
      mapper.setName("mapper");
      mapper.setTemplate(new CylandeTemplateType());
      mapper.getTemplate().setDir(new File("").getAbsolutePath() + "/src/test/resources");
      mapper.getTemplate().setFile("dataFormatTemplate.txt");
      mapper.getTemplate().setHeader("HEADER1|HEADER2|HEADER3|HEADER4|");
      mapper.getTemplate().setFooter("FOOTER1|FOOTER2|FOOTER3|FOOTER4|");
      serv.createMapper(mapper, null, ContextTransformer.fromLocale());
      FILEPROVIDER provider = new FILEPROVIDER();
      provider.setName("providerName");
      provider.setMapper(new ProviderMapperType());
      provider.getMapper().setRef("mapper");
      ProviderFileType file = new ProviderFileType();
      file.setDir(new File("").getAbsolutePath() + "/src/test/resources");
      file.setFileName("dataFormatListType.csv");
      file.setSortType(FileSortEnum.NAME);
      file.setOverWrite(true);
      provider.setFile(file);
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderWriter writer = new ProviderWriterImpl(fileProvider);
      Reader reader = new FileReader(new File("").getAbsolutePath() + "/src/test/resources/dataFormatListTypeToCSV.xml");
      String xml = IOUtils.toString(reader).replaceAll("(?m)(?<=^ *)\\s+", "").replaceAll("[\\n\\r]", ""); // suppression de l'indentation et des sauts de ligne
      writer.write(xml);
      writer.releaseProvider();
      // on force la lecture du fichier de r�sultat en UTF-8 sinon le caract�re de s�parateur des milliers n'est pas correctement encod�
      reader = new InputStreamReader(new FileInputStream(new File("").getAbsolutePath() + "/src/test/resources/"  + file.getFileName()), "UTF-8");
      String csv = IOUtils.toString(reader);
      assertTrue(csv.contains("abcd|ab|abcd    |    abcd|")); // test des champs de taille fixe
      assertTrue(csv.contains("azer|azerty|er|erty|rty||  ||")); // test des champs de taille variable avec substring
      assertTrue(csv.contains("46|461012|461012    |+461012   |+000461012|          ||"));
      assertTrue(csv.contains("7266.845|7266,845000 |7266,84     |+7�266,84   |   +7�266,84|+00007266,84|    7�266,84|"));
      assertTrue(csv.contains("-7266.855|-7266,855000|-7266,86    |-7�266,86   |   -7�266,86|-00007266,86|  (7�266,86)|"));
      assertTrue(csv.contains("2016-02-04T09:13:09.588+01:00|04/02/2016|09:13:09|+0100|13/11/1979|"));
      assertTrue(csv.contains("HEADER1|HEADER2|HEADER3|HEADER4|"));
      assertTrue(csv.contains("FOOTER1|FOOTER2|FOOTER3|FOOTER4|"));
    }
    catch (Exception e)
    {
      fail();
    }
  }

  /**
   * Permet de tester la r�gle de la RFC 4180 (format CSV) qui impose d'�chapper les double quotes internes par un second double quote si les champs sont
   * encapsul�s par des doubles quotes avec le s�parateur ","
   */
  public void testProviderWriterWithMapperPrefix()
  {
    try
    {
      MapperManagerServiceImpl serv = new MapperManagerServiceImpl();
      MapperType mapper = new MapperType();
      mapper.setName("mapper");
      mapper.setTemplate(new CylandeTemplateType());
      mapper.getTemplate().setDir(new File("").getAbsolutePath() + "/src/test/resources");
      mapper.getTemplate().setFile("dataPrefixTemplate.txt");
      serv.createMapper(mapper, null, ContextTransformer.fromLocale());
      FILEPROVIDER provider = new FILEPROVIDER();
      provider.setName("providerName");
      provider.setMapper(new ProviderMapperType());
      provider.getMapper().setRef("mapper");
      ProviderFileType file = new ProviderFileType();
      file.setDir(new File("").getAbsolutePath() + "/src/test/resources");
      file.setFileName("dataPrefixListType.csv");
      file.setSortType(FileSortEnum.NAME);
      file.setOverWrite(true);
      provider.setFile(file);
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderWriter writer = new ProviderWriterImpl(fileProvider);
      Reader reader = new FileReader(new File("").getAbsolutePath() + "/src/test/resources/dataFormatListTypeToCSV.xml");
      String xml = IOUtils.toString(reader).replaceAll("(?m)(?<=^ *)\\s+", "").replaceAll("[\\n\\r]", ""); // suppression de l'indentation et des sauts de ligne
      writer.write(xml);
      writer.releaseProvider();
      // on force la lecture du fichier de r�sultat en UTF-8 sinon le caract�re de s�parateur des milliers n'est pas correctement encod�
      reader = new InputStreamReader(new FileInputStream(new File("").getAbsolutePath() + "/src/test/resources/"  + file.getFileName()), "UTF-8");
      String csv = IOUtils.toString(reader);
      assertTrue(csv.contains("\"461012\",\"127 rue \"\"Paul Bert\"\"\""));
    }
    catch (Exception e)
    {
      fail();
    }
  }

  /**
   * Permet de v�rifier que la pr�sence d'un pr�fixe et d'un suffixe dans les donn�es CSV � int�grer ne se retrouve pas dans les donn�es converties en XML.
   * Teste �galement les filtres d'exclusion de ligne.
   */
  public void testProviderReaderWithMapper()
  {
    try
    {
      MapperManagerServiceImpl serv = new MapperManagerServiceImpl();
      MapperType mapper = new MapperType();
      mapper.setName("mapper");
      mapper.setTemplate(new CylandeTemplateType());
      mapper.getTemplate().setDir(new File("").getAbsolutePath() + "/src/test/resources");
      mapper.getTemplate().setFile("codifTemplate.txt");
      mapper.getTemplate().setLineFilter(new MapperLineFilterType());
      List<MapperLineCriteriaType> excludeList = new ArrayList();
      MapperLineCriteriaType crit = new MapperLineCriteriaType();
      crit.setEquals("codeEqualsExclude;descEqualsExclude");
      excludeList.add(crit);
      crit.setStartsWith("codeStartsWithExclude");
      excludeList.add(crit);
      crit.setEndsWith("descEndsWithExclude");
      excludeList.add(crit);
      crit.setContains("ContainsExclude");
      excludeList.add(crit);
      crit = new MapperLineCriteriaType();
      crit.setCaseInSensitive(true);
      crit.setEquals("codeEqualscaseinsExclude;descEqualsExclude");
      excludeList.add(crit);
      crit.setStartsWith("codeStartsWithcaseinsExclude");
      excludeList.add(crit);
      crit.setEndsWith("descEndsWithcaseinsExclude");
      excludeList.add(crit);
      crit.setContains("ContainscaseinsExclude");
      excludeList.add(crit);
      mapper.getTemplate().getLineFilter().setExcludes(excludeList);
      serv.createMapper(mapper, null, ContextTransformer.fromLocale());
      FILEPROVIDER provider = new FILEPROVIDER();
      provider.setName("providerName");
      provider.setMapper(new ProviderMapperType());
      provider.getMapper().setRef("mapper");
      ProviderFileType file = new ProviderFileType();
      file.setDir(new File("").getAbsolutePath() + "/src/test/resources");
      file.setFileName("codif.csv");
      file.setSortType(FileSortEnum.NAME);
      provider.setFile(file);
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderReader reader = new ProviderReaderImpl(fileProvider, 0, null, null);
      DataPackage data = reader.read(null, null);
      assertTrue(data.getValue().contains("<detailCode>co&amp;de1</detailCode>"));
      assertTrue(data.getValue().contains("<shortDescription>desc1</shortDescription>"));
      assertTrue(data.getValue().contains("<detailCode>code2</detailCode>"));
      assertTrue(data.getValue().contains("<shortDescription>desc2</shortDescription>"));
    }
    catch (Exception e)
    {
      fail();
    }
  }

  /**
   * Test de non r�gression quand le mapper est d�fini sans pr�fixe et avec ';' comme s�parateur : les doubles quotes doivent
   * d�s lors �tre supprim�s des donn�es sauf ceux �chapp�s par '\'. Dans ce cas, seul le caract�re '\' est supprim�.
   */
  public void testProviderReaderWithMapperWithoutPrefix()
  {
    try
    {
      MapperManagerServiceImpl serv = new MapperManagerServiceImpl();
      MapperType mapper = new MapperType();
      mapper.setName("mapper");
      mapper.setTemplate(new CylandeTemplateType());
      mapper.getTemplate().setDir(new File("").getAbsolutePath() + "/src/test/resources");
      mapper.getTemplate().setFile("codifTemplateWithoutPrefix.txt");
      serv.createMapper(mapper, null, ContextTransformer.fromLocale());
      FILEPROVIDER provider = new FILEPROVIDER();
      provider.setName("providerName");
      provider.setMapper(new ProviderMapperType());
      provider.getMapper().setRef("mapper");
      ProviderFileType file = new ProviderFileType();
      file.setDir(new File("").getAbsolutePath() + "/src/test/resources");
      file.setFileName("codifWithDoubleQuote.csv");
      file.setSortType(FileSortEnum.NAME);
      provider.setFile(file);
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderReader reader = new ProviderReaderImpl(fileProvider, 0, null, null);
      DataPackage data = reader.read(null, null);
      assertTrue(data.getValue().contains("<detailCode>co&amp;de\"1</detailCode>"));
      assertTrue(data.getValue().contains("<shortDescription>desc1</shortDescription>"));
      assertTrue(data.getValue().contains("<detailCode>code2</detailCode>"));
      assertTrue(data.getValue().contains("<shortDescription>desc2</shortDescription>"));
    }
    catch (Exception e)
    {
      fail();
    }
  }

  /**
   * Permet de tester sur un fichier CSV la r�cup�ration des champs de donn�es d�finies sur plusieurs lignes.
   * Pour cela, le champ contenant plusieurs lignes doit �tre encapsul�s par des doubles quotes (" => r�gle de la RFC 4180) et
   * l'attribut csvFormat doit �tre � true.
   */
  public void testProviderReaderWithMapperCSVFormat()
  {
    try
    {
      MapperManagerServiceImpl serv = new MapperManagerServiceImpl();
      MapperType mapper = new MapperType();
      mapper.setName("mapper");
      mapper.setTemplate(new CylandeTemplateType());
      mapper.getTemplate().setDir(new File("").getAbsolutePath() + "/src/test/resources");
      mapper.getTemplate().setFile("countryTemplate.txt");
      mapper.getTemplate().setLineFilter(new MapperLineFilterType());
      List<MapperLineCriteriaType> excludeList = new ArrayList();
      MapperLineCriteriaType crit = new MapperLineCriteriaType();
      crit.setStartsWith("code;"); // les doubles quotes d'encapsulation sont supprim�s lors de la lecture des donn�es donc il ne faut pas les pr�ciser sur les filtres
      crit.setFirst(true);
      excludeList.add(crit);
      mapper.getTemplate().getLineFilter().setCsvFormat(true);
      mapper.getTemplate().getLineFilter().setExcludes(excludeList);
      serv.createMapper(mapper, null, ContextTransformer.fromLocale());
      FILEPROVIDER provider = new FILEPROVIDER();
      provider.setName("providerName");
      provider.setMapper(new ProviderMapperType());
      provider.getMapper().setRef("mapper");
      ProviderFileType file = new ProviderFileType();
      file.setDir(new File("").getAbsolutePath() + "/src/test/resources");
      file.setFileName("countryListType.csv");
      file.setSortType(FileSortEnum.NAME);
      provider.setFile(file);
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderReader reader = new ProviderReaderImpl(fileProvider, 0, null, null);
      DataPackage data = reader.read(null, null);
      assertFalse(data.getValue().contains("<code>code</code>"));
      assertTrue(data.getValue().contains("<description>ligne1;\nligne;2</description>")); // les doubles quotes sont supprim�s lors de la lecture des donn�es
      assertTrue(data.getValue().contains("<translations><language>FR</language><description>Francais</description></translations><vatCodes><code>FR</code><translations><language>FR</language></translations></vatCodes><translations><language>IT</language></translations>"));
    }
    catch (Exception e)
    {
      fail();
    }
  }

  public void testProviderWriterJsonList()
  {
    try
    {
      FILEPROVIDER provider = new FILEPROVIDER();
      provider.setName("providerName");
      ProviderFileType file = new ProviderFileType();
      file.setDir(new File("").getAbsolutePath() + "/src/test/resources");
      file.setFileName("countryListType.json");
      file.setSortType(FileSortEnum.NAME);
      file.setOverWrite(true);
      provider.setFile(file);
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderWriter writer = new ProviderWriterImpl(fileProvider);
      // utilisation d'un InputStream pour pouvoir fixer l'encodage
      InputStream is = new FileInputStream(new File("").getAbsolutePath() + "/src/test/resources/countryListTypeToJson.xml");
      String xml = IOUtils.toString(is, "UTF-8").replaceAll("(?m)(?<=^ *)\\s+", "").replaceAll("[\\n\\r]", ""); // suppression de l'indentation et des sauts de ligne
      writer.write(xml);
      writer.releaseProvider();
      String json = IOUtils.toString(new FileInputStream(new File("").getAbsolutePath() + "/src/test/resources/" + file.getFileName()), "UTF-8");
      String jsonCompare = IOUtils.toString(new FileInputStream(new File("").getAbsolutePath() + "/src/test/resources/tem-" + file.getFileName()), "UTF-8");
      assertEquals(json.length(), jsonCompare.length());
    }
    catch (Exception e)
    {
      fail();
    }
  }

  public void testProviderWriterJson()
  {
    try
    {
      String dir = new File("").getAbsolutePath() + "/src/test/resources/";
      FILEPROVIDER provider = new FILEPROVIDER();
      provider.setName("providerName");
      ProviderFileType file = new ProviderFileType();
      file.setDir(dir);
      file.setFileName("countryType.json");
      file.setSortType(FileSortEnum.NAME);
      file.setOverWrite(true);
      provider.setFile(file);
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderWriter writer = new ProviderWriterImpl(fileProvider);
      // utilisation d'un InputStream pour pouvoir fixer l'encodage
      InputStream is = new FileInputStream(new File("").getAbsolutePath() + "/src/test/resources/countryTypeToJson.xml");
      String xml = IOUtils.toString(is, "UTF-8").replaceAll("(?m)(?<=^ *)\\s+", "").replaceAll("[\\n\\r]", ""); // suppression de l'indentation et des sauts de ligne
      writer.write(xml);
      writer.releaseProvider();
      String json = IOUtils.toString(new FileInputStream(dir + file.getFileName()), "UTF-8");
      String jsonCompare = IOUtils.toString(new FileInputStream(dir + "tem-" + file.getFileName()), "UTF-8");
      assertEquals(json.length(), jsonCompare.length());
    }
    catch (Exception e)
    {
      fail();
    }
  }

  /**
   * Test de lecture d'un fichier JSON contentant une liste de 2 pays (countryListType) : on effectue une lecture par paquet de 1 afin de v�rifier que
   * le d�coupage s'effectue correctement.
   */
  public void testProviderReaderJsonList()
  {
    try
    {
      FILEPROVIDER provider = new FILEPROVIDER();
      provider.setName("providerName");
      ProviderFileType file = new ProviderFileType();
      file.setDir(new File("").getAbsolutePath() + "/src/test/resources");
      file.setFileName("tem-countryListType.json");
      file.setSortType(FileSortEnum.NAME);
      provider.setFile(file);
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderReader reader = new ProviderReaderImpl(fileProvider, 1, null, null);
      DataPackage data = reader.read(null, null);
      assertEquals("tem-countryListType.json", reader.getCurrentFileName());
      assertFalse("cela ne doit pas etre le dernier paquet", data.isLastPackage());
      assertEquals("l'index du paquet n'est pas correct", 1, data.getPackageNumber());
      assertTrue(data.getValue().contains("<description>Chine</description>"));
      data = reader.read(null, null);
      assertEquals("l'index du paquet n'est pas correct", 2, data.getPackageNumber());
      assertTrue(data.getValue().contains("<description>Japon</description>"));
      // teste qu'il n'y a plus rien � lire apr�s la lecture du dernier paquet attendu
      data = reader.read(null, null);
      assertNull("must be null", data);
      reader.releaseProvider();
    }
    catch (Exception e)
    {
      fail();
    }
  }

  /**
   * Test de lecture d'un fichier JSON array
   */
  public void testProviderReaderJsonArray()
  {
    try
    {
      String dir = new File("").getAbsolutePath() + "/src/test/resources/";
      FileUtils.copyFile(new File(dir + "tem-array.json"), new File(dir + "array.json"));
      FILEPROVIDER provider = new FILEPROVIDER();
      provider.setName("providerName");
      ProviderFileType file = new ProviderFileType();
      file.setDir(dir);
      file.setFileName("array.json");
      file.setSortType(FileSortEnum.NAME);
      file.setJson(new JSONFileType());
      file.getJson().setRootElement("countryListType");
      file.getJson().setNodeName("values");
      provider.setFile(file);
      FileProvider fileProvider = new FileProvider(provider, provider.getName(), null, new PropertiesManager(), null, null);
      ProviderReader reader = new ProviderReaderImpl(fileProvider, 0, null, null);
      DataPackage data = reader.read(null, null);
      assertTrue(data.getValue().contains("<countryListType><values>"));
      String json = IOUtils.toString(new FileInputStream(dir + file.getFileName()), "UTF-8");
      String jsonCompare = IOUtils.toString(new FileInputStream(dir + "tem-" + file.getFileName()), "UTF-8");
      assertEquals(json, "{\"countryListType\":{\"values\":" + jsonCompare + "}}");
      reader.releaseProvider();
    }
    catch (Exception e)
    {
      fail();
    }
  }
}
