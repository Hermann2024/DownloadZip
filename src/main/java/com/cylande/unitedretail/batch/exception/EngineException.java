package com.cylande.unitedretail.batch.exception;

/**
 * @deprecated
 */
public class EngineException extends Exception
{
  /**
   * Default constructor
   */
  public EngineException()
  {
    super();
  }

  /**
   * Construct an EngineException with given message
   * @param pMessage
   */
  public EngineException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   * @param pMessage
   * @param pCause
   */
  public EngineException(String pMessage, Throwable pCause)
  {
    super(pMessage, pCause);
  }
}
