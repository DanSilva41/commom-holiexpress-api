package com.holiexpress.ecommerce.common.exceptions;

/**
 * @author Danilo Silva
 */
public class HoliExpressApiException extends RuntimeException {

  public HoliExpressApiException(String errorMessage) {
    super(errorMessage);
  }

  public HoliExpressApiException(String errorMessage, Throwable cause) {
    super(errorMessage, cause);
  }

  public HoliExpressApiException(Throwable cause) {
    super(cause);
  }

  public HoliExpressApiException(String errorMessage, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(errorMessage, cause, enableSuppression, writableStackTrace);
  }
}
