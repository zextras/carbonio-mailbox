// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.VoiceConstants;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

@XmlAccessorType(XmlAccessType.NONE)
public class PhoneVoiceFeaturesInfo {

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description Name
   */
  @XmlAttribute(name = AccountConstants.A_NAME /* name */, required = true)
  private String name;

  /**
   * @zm-api-field-description
   */
  @XmlElements({
    @XmlElement(
        name = VoiceConstants.E_VOICE_MAIL_PREFS /* voicemailprefs */,
        type = VoiceMailPrefsFeature.class),
    @XmlElement(
        name = VoiceConstants.E_ANON_CALL_REJECTION /* anoncallrejection */,
        type = AnonCallRejectionFeature.class),
    @XmlElement(
        name = VoiceConstants.E_CALLER_ID_BLOCKING /* calleridblocking */,
        type = CallerIdBlockingFeature.class),
    @XmlElement(
        name = VoiceConstants.E_CALL_FORWARD /* callforward */,
        type = CallForwardFeature.class),
    @XmlElement(
        name = VoiceConstants.E_CALL_FORWARD_BUSY_LINE /* callforwardbusyline */,
        type = CallForwardBusyLineFeature.class),
    @XmlElement(
        name = VoiceConstants.E_CALL_FORWARD_NO_ANSWER /* callforwardnoanswer */,
        type = CallForwardNoAnswerFeature.class),
    @XmlElement(
        name = VoiceConstants.E_CALL_WAITING /* callwaiting */,
        type = CallWaitingFeature.class),
    @XmlElement(
        name = VoiceConstants.E_SELECTIVE_CALL_FORWARD /* selectivecallforward */,
        type = SelectiveCallForwardFeature.class),
    @XmlElement(
        name = VoiceConstants.E_SELECTIVE_CALL_ACCEPTANCE /* selectivecallacceptance */,
        type = SelectiveCallAcceptanceFeature.class),
    @XmlElement(
        name = VoiceConstants.E_SELECTIVE_CALL_REJECTION /* selectivecallrejection */,
        type = SelectiveCallRejectionFeature.class)
  })
  private List<CallFeatureInfo> callFeatures = Lists.newArrayList();

  public PhoneVoiceFeaturesInfo() {}

  public void setName(String name) {
    this.name = name;
  }

  public void setCallFeatures(Iterable<CallFeatureInfo> callFeatures) {
    this.callFeatures.clear();
    if (callFeatures != null) {
      Iterables.addAll(this.callFeatures, callFeatures);
    }
  }

  public void addCallFeature(CallFeatureInfo callFeature) {
    this.callFeatures.add(callFeature);
  }

  public String getName() {
    return name;
  }

  public List<CallFeatureInfo> getCallFeatures() {
    return callFeatures;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("name", name).add("callFeatures", callFeatures);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
