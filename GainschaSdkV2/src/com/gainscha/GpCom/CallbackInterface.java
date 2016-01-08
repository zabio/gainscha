package com.gainscha.GpCom;

public abstract interface CallbackInterface
{
  public abstract GpCom.ERROR_CODE CallbackMethod(GpComCallbackInfo paramGpComCallbackInfo);
}
