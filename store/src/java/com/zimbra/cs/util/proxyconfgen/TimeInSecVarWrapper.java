package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.service.ServiceException;

/**
 * a wrapper class that convert a ProxyConfVar which contains the time in milliseconds to seconds.
 * This is useful when the default timeout unit used by Provisioning API is "ms" but nginx uses "s".
 *
 * @author jiankuan
 */
class TimeInSecVarWrapper extends ProxyConfVar {

  protected ProxyConfVar mVar;

  public TimeInSecVarWrapper(ProxyConfVar proxyConfVar) {
    super(null, null, null, null, null, null);

    if (proxyConfVar.mValueType != ProxyConfValueType.TIME) {
      throw new RuntimeException(
          "Only Proxy Conf Var with TIME" + " type can be used in this wrapper");
    }

    mVar = proxyConfVar;
  }

  @Override
  public void update() throws ServiceException, ProxyConfException {
    mVar.update();
    mVar.mValue = (Long) mVar.mValue / 1000;
  }

  @Override
  public String format(Object o) throws ProxyConfException {
    return mVar.mValue.toString();
  }
}
