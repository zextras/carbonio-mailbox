package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class CalendarGroupInfo {
  /**
   * @zm-api-field-tag id
   * @zm-api-field-description Calendar Group ID
   */
  @XmlAttribute(name = "id" /* id */, required = true)
  private String id;

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description Calendar Group Name
   */
  @XmlAttribute(name = "name" /* name */, required = true)
  private String name;

  /**
   * @zm-api-field-tag calendarIds
   * @zm-api-field-description Calendar IDs
   */
  @XmlElement(name = "calendarId" /* calendarId */, required = true)
  private List<String> calendarIds;

  /**
   * no-argument constructor wanted by JAXB
   */
  @SuppressWarnings("unused")
  public CalendarGroupInfo() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getCalendarIds() {
    return calendarIds;
  }

  public void setCalendarIds(List<String> calendarIds) {
    this.calendarIds = calendarIds;
  }
}
