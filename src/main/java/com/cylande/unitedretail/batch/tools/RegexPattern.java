package com.cylande.unitedretail.batch.tools;

/**
 * Classe utilitaire pour cr�er des expressions r�guli�res � partir de jokers
 */
public final class RegexPattern
{
  /** joker "au plus un" */
  private static final String AT_MOST_JOKER = "?";

  /** joker "z�ro ou un" */
  private static final String ZERO_OR_MORE_JOKER = "*";

  /** expression r�guli�re "au plus un" */
  private static final String AT_MOST_REGEX = ".?";

  /** expression r�guli�re "z�ro ou un" */
  private static final String ZERO_OR_MORE_REGEX = ".*";

  /**
   * Constructor
   */
  private RegexPattern()
  {
  }

  /**
   * G�n�re l'expression en rempla�ant les caract�res jokers de la cha�ne en param�tre
   * @param pBefore
   * @return r�sultat
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
