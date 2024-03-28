// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.zimbra.soap.type.KeyValuePair;

@XmlAccessorType(XmlAccessType.NONE)
public interface MessageInfoInterface<P>
extends MessageCommonInterface {
    MessageInfoInterface createFromId(String id);
    void setId(String id);
    void setCalendarIntendedFor(String calendarIntendedFor);
    void setOrigId(String origId);
    void setDraftReplyType(String draftReplyType);
    void setIdentityId(String identityId);
    void setDraftAccountId(String draftAccountId);
    void setDraftAutoSendTime(Long draftAutoSendTime);
    void setSentDate(Long sentDate);
    void setResentDate(Long resentDate);
    void setPart(String part);
    void setFragment(String fragment);
    void setSubject(String subject);
    void setMessageIdHeader(String messageIdHeader);
    void setInReplyTo(String inReplyTo);
    void setHeaders(Iterable<KeyValuePair> headers);
    void addHeader(KeyValuePair header);
    void setContentElems(Iterable<P> contentElems);
    void addContentElem(P contentElem);
    String getId();
    String getCalendarIntendedFor();
    String getOrigId();
    String getDraftReplyType();
    String getIdentityId();
    String getDraftAccountId();
    Long getDraftAutoSendTime();
    Long getSentDate();
    Long getResentDate();
    String getPart();
    String getFragment();
    String getSubject();
    String getMessageIdHeader();
    String getInReplyTo();
    List<KeyValuePair> getHeaders();
    List<P> getContentElems();
    void setEmailInterfaces(Iterable<EmailInfoInterface> emails);
    void addEmailInterface(EmailInfoInterface email);
    void setInviteInterface(InviteInfoInterface invite);
    List<EmailInfoInterface> getEmailInterfaces();
    InviteInfoInterface getInvitInterfacee();
}
