package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.service.ServiceException;
import java.util.ArrayList;
import java.util.regex.Matcher;

class AddHeadersVar extends ProxyConfVar {

  private final ArrayList<String> rhdr;
  private int i;

  public AddHeadersVar(String key, ArrayList<String> rhdr, String description) {
    super(key, null, null, ProxyConfValueType.CUSTOM, ProxyConfOverride.CUSTOM, description);
    this.rhdr = rhdr;
  }

  @Override
  public void update() throws ServiceException {
    ArrayList<KeyValue> directives = new ArrayList<>();
    KeyValue[] headers = new KeyValue[rhdr.size()];
    i = 0;

    for (String hdr : rhdr) {
      Matcher matcher = RE_HEADER.matcher(hdr);
      if (matcher.matches()) {
        headers[i] = new KeyValue(matcher.group(1), matcher.group(2));
      } else {
        headers[i] = new KeyValue(hdr);
      }
      directives.add(headers[i]);
      i++;
    }
    mValue = directives;
  }

  @Override
  public String format(Object o) {
    @SuppressWarnings("unchecked")
    ArrayList<KeyValue> rsphdr = (ArrayList<KeyValue>) o;
    StringBuilder sb = new StringBuilder();
    for (i = 0; i < rsphdr.size(); i++) {
      KeyValue header = rsphdr.get(i);
      mLog.debug("Adding directive add_header " + header.key + " " + header.value);
      if (i == 0) {
        sb.append(String.format("add_header %s %s;", header.key, header.value));
      } else {
        sb.append(String.format("\n    add_header %s %s;", header.key, header.value));
      }
    }
    return sb.toString();
  }
}
