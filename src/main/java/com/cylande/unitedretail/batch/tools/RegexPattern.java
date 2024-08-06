package com.cylande.unitedretail.batch.tools;

/**
 * Classe utilitaire pour créer des expressions régulières à partir de jokers
 */
public final class RegexPattern
{
  /** joker "au plus un" */
  private static final String AT_MOST_JOKER = "?";

  /** joker "zéro ou un" */
  private static final String ZERO_OR_MORE_JOKER = "*";

  /** expression régulière "au plus un" */
  private static final String AT_MOST_REGEX = ".?";

  /** expression régulière "zéro ou un" */
  private static final String ZERO_OR_MORE_REGEX = ".*";

  /**
   * Constructor
   */
  private RegexPattern()
  {
  }

  /**
   * Génère l'expression en remplaçant les caractères jokers de la chaîne en paramètre
   * @param pBefore
   * @return résultat
   */
  public static String getRegexPattern(String pBefore)
  {
    String result = null;
    if (pBefore == null)
    {
      return null;
    }
    result = pBefore.replace(ZERO_OR_MORE_JOKER, ZERO_OR_MORE_REGEX);
    result = result.replace(AT_MOST_JOKER, AT_MOST_REGEX);
    return result;
  }
}
