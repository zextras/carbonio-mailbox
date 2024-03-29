// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zimbra.doc.soap.apidesc.SoapApiCommand;
import com.zimbra.doc.soap.apidesc.SoapApiDescription;
import com.zimbra.doc.soap.changelog.AttributeChanges;
import com.zimbra.doc.soap.changelog.CommandChanges;
import com.zimbra.doc.soap.changelog.CommandChanges.NamedAttr;
import com.zimbra.doc.soap.changelog.CommandChanges.NamedElem;
import com.zimbra.doc.soap.changelog.ElementChanges;
import com.zimbra.doc.soap.changelog.SoapApiChangeLog;
import com.zimbra.soap.type.ZmBoolean;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ChangelogTest {

  private static final Logger LOG = LogManager.getLogger(ChangelogTest.class);

  static {
    com.zimbra.common.util.LogManager.setThisLogAndRootToLevel(LOG, Level.INFO);
  }

    @BeforeAll
    public static void init() throws Exception {
    }

  @XmlRootElement(name = "aRequest")
  public class aRequest {
    @XmlAttribute(name = "attribute1", required = false)
    private String attribute1;

    @XmlAttribute(name = "attribute2", required = false)
    private String attribute2;

    @XmlElement(name = "element", required = false)
    private aElem element;

    public aRequest() {}

    public String getAttribute1() {
      return attribute1;
    }

    public void setAttribute1(String attribute1) {
      this.attribute1 = attribute1;
    }

    public String getAttribute2() {
      return attribute2;
    }

    public void setAttribute2(String attribute2) {
      this.attribute2 = attribute2;
    }

    public aElem getElement() {
      return element;
    }

    public void setElement(aElem element) {
      this.element = element;
    }
  }

  @XmlRootElement(name = "aResponse")
  public class aResponse {
    @XmlElement(name = "an-elem", required = false)
    private ZmBoolean anElem;

    public ZmBoolean getAnElem() {
      return anElem;
    }

    public void setAnElem(ZmBoolean anElem) {
      this.anElem = anElem;
    }
  }

  @XmlRootElement(name = "bRequest")
  public class bRequest {}

  @XmlRootElement(name = "bResponse")
  public class bResponse {}

  @XmlRootElement(name = "cRequest")
  public class cRequest {}

  @XmlRootElement(name = "cResponse")
  public class cResponse {}

  public class aElem {
    @XmlAttribute(name = "long-attribute", required = false)
    private Long longAttribute;

    @XmlAttribute(name = "int-attribute", required = true)
    private int intAttribute;

    @XmlValue int value;

    public Long getLongAttribute() {
      return longAttribute;
    }

    public void setLongAttribute(Long longAttribute) {
      this.longAttribute = longAttribute;
    }

    public int getIntAttribute() {
      return intAttribute;
    }

    public void setIntAttribute(int intAttribute) {
      this.intAttribute = intAttribute;
    }
  }

  @Test
  void makeChangelogTest()
      throws Exception {
    Map<String, ApiClassDocumentation> javadocInfo = Maps.newTreeMap();
    List<Class<?>> classes = Lists.newArrayList();
    classes.add(aRequest.class);
    classes.add(aResponse.class);
    classes.add(bRequest.class);
    classes.add(bResponse.class);
    classes.add(cRequest.class);
    classes.add(cResponse.class);
    Root soapApiDataModelRoot = WsdlDocGenerator.processJaxbClasses(javadocInfo, classes);
    SoapApiDescription jsonDescCurrent = new SoapApiDescription("7.99.99", "20000131-2359");
    jsonDescCurrent.build(soapApiDataModelRoot);
    // File json = new File("/tmp/test1.json");
    // jsonDescCurrent.serializeToJson(json);
    InputStream is = getClass().getResourceAsStream("baseline1.json");
    SoapApiDescription jsonDescBaseline = SoapApiDescription.deserializeFromJson(is);
    SoapApiChangeLog clog = new SoapApiChangeLog();
    clog.setBaselineDesc(jsonDescBaseline);
    clog.setCurrentDesc(jsonDescCurrent);
    clog.makeChangeLogDataModel();
    List<SoapApiCommand> newCmds = clog.getNewCommands();
    LOG.info("    New Command:" + newCmds.get(0).getName());
    List<SoapApiCommand> delCmds = clog.getDeletedCommands();
    LOG.info("    Deleted Command:" + delCmds.get(0).getName());
    List<CommandChanges> modCmds = clog.getModifiedCommands();
    CommandChanges modCmd = modCmds.get(0);
    LOG.info("    Modified Command:" + modCmd.getName());
    List<NamedAttr> delAttrs = modCmd.getDeletedAttrs();
    for (NamedAttr attr : delAttrs) {
      LOG.info("    Deleted Attribute:" + attr.getXpath());
    }
    List<NamedAttr> addAttrs = modCmd.getNewAttrs();
    for (NamedAttr attr : addAttrs) {
      LOG.info("    Added Attribute:" + attr.getXpath());
    }
    List<AttributeChanges> modAttrs = modCmd.getModifiedAttrs();
    for (AttributeChanges modAttr : modAttrs) {
      LOG.info(
          "    Modified Attribute "
              + modAttr.getXpath()
              + ":\nbase="
              + modAttr.getBaselineRepresentation()
              + "\ncurr="
              + modAttr.getCurrentRepresentation());
    }
    List<NamedElem> delEs = modCmd.getDeletedElems();
    for (NamedElem el : delEs) {
      LOG.info("    Deleted Element :" + el.getXpath());
    }
    List<NamedElem> newEs = modCmd.getNewElems();
    for (NamedElem el : newEs) {
      LOG.info("    New Element :" + el.getXpath());
    }
    List<ElementChanges> modEs = modCmd.getModifiedElements();
    for (ElementChanges el : modEs) {
      LOG.info(
          "    Modified Element "
              + el.getXpath()
              + ":\nbase="
              + el.getBaselineRepresentation()
              + "\ncurr="
              + el.getCurrentRepresentation());
    }
    assertEquals(1, newCmds.size(), "Number of new commands");
    assertEquals(1, delCmds.size(), "Number of deleted commands");
    assertEquals(1, modCmds.size(), "Number of modified commands");
    assertEquals(1, delAttrs.size(), "Number of deleted attributes");
    assertEquals(1, addAttrs.size(), "Number of new attributes");
    assertEquals(1, modAttrs.size(), "Number of modified attributes");
    assertEquals(1, delEs.size(), "Number of deleted elements");
    assertEquals(1, newEs.size(), "Number of new elements");
    assertEquals(1, modEs.size(), "Number of modified elements");
  }
}
