package com.cylande.unitedretail.batch.provider.pool;

import java.io.IOException;
import java.io.Serializable;

/**
 * Mot clef du protocole d'échange entre thread client et providers distribués
 */
public class RemoteProviderProtocoleTag implements Serializable
{
  /** la valeur du mot clef (chaine) */
  private String _value;

  /**
   * Constructeur
   * @param pValue : le mot clef
   */
  public RemoteProviderProtocoleTag(String pValue)
  {
    _value = pValue;
  }

  /** {@inheritDoc} */
  public boolean equals(Object pOther)
  {
    return (pOther != null) && ((pOther == this) || ((pOther instanceof RemoteProviderProtocoleTag) && (_value.equals(((RemoteProviderProtocoleTag)pOther)._value))));
  }

  /** {@inheritDoc} */
  public int hashCode()
  {
    int hashCode = 17;
    hashCode = 31 * hashCode + (_value == null ? 0 : _value.hashCode());
    return hashCode;
  }

  /** {@inheritDoc} */
  public String toString()
  {
    return _value;
  }

  /**
   * Surcharge de la sérialisation Java
   * @param pOut : le stream pour l'écriture de l'objet
   * @throws IOException exception
   */
  private void writeObject(java.io.ObjectOutputStream pOut) throws IOException
  {
    pOut.writeObject(_value);
  }

  /**
   * Surcharge de la sérialisation Java
   * @param pIn : le stream pour la lecture de l'objet
   * @throws IOException exception
   * @throws ClassNotFoundException exception
   */
  private void readObject(java.io.ObjectInputStream pIn) throws IOException, ClassNotFoundException
  {
    _value = (String)pIn.readObject();
  }
}
