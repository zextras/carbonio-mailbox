// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SmimeConstants;
import com.zimbra.soap.mail.type.MsgToSend;
import com.zimbra.soap.type.ZmBoolean;

import java.util.Arrays;

// TODO: indicate whether to save in SentMail (or some other folder)
/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Send message
 * <ul>
 * <li> Supports (f)rom, (t)o, (c)c, (b)cc, (r)eply-to, (s)ender, read-receipt (n)otification "type" on
 *      <b>&lt;e></b> elements.
 * <li> Only allowed one top-level <b>&lt;mp></b> but can nest <b>&lt;mp></b>s within if multipart/*
 * <li> A leaf <b>&lt;mp></b> can have inlined content
 *      (<b>&lt;mp ct="{content-type}">&lt;content>...&lt;/content>&lt;/mp></b>)
 * <li> A leaf <b>&lt;mp></b> can have referenced content (<b>&lt;mp>&lt;attach ...>&lt;/mp></b>)
 * <li> Any <b>&lt;mp></b> can have a Content-ID header attached to it.
 * <li> On reply/forward, set origid on <b>&lt;m></b> element and set rt to "r" or "w", respectively
 * <li> Can optionally set identity-id to specify the identity being used to compose the message
 * <li> If noSave is set, a copy will <b>not</b> be saved to sent regardless of account/identity settings
 * <li> Can set priority high (!) or low (?) on sent message by specifying "f" attr on <b>&lt;m></b>
 * <li> The message to be sent can be fully specified under the <b>&lt;m></b> element or, to compose the message
 *      remotely remotely, upload it via FileUploadServlet, and submit it through our server using something like:
 *      <pre>
 *         &lt;SendMsgRequest [suid="{send-uid}"] [needCalendarSentByFixup="0|1"]>
 *             &lt;m aid="{uploaded-MIME-body-ID}" [origid="..." rt="r|w"]/>
 *         &lt;/SendMsgRequest>
 *      </pre>
 * <li> If the message is saved to the sent folder then the ID of the message is returned.  Otherwise, no ID is
 *      returned -- just a <b>&lt;m></b> is returned.
 * </ul>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_SEND_MSG_REQUEST)
public class SendMsgRequest {

    /**
     * @zm-api-field-tag add-sent-by
     * @zm-api-field-description If set then Add SENT-BY parameter to ORGANIZER and/or ATTENDEE properties in
     * iCalendar part when sending message on behalf of another user.  Default is unset.
     */
    @XmlAttribute(name=MailConstants.A_NEED_CALENDAR_SENTBY_FIXUP /* needCalendarSentByFixup */, required=false)
    private ZmBoolean needCalendarSentbyFixup;
    
    /**
     * @zm-api-field-tag calendar-forward
     * @zm-api-field-description Indicates whether this a forward of calendar invitation in which
     * case the server sends Forward Invitation Notification, default is unset.
     */
    @XmlAttribute(name=MailConstants.A_IS_CALENDAR_FORWARD /* isCalendarForward */, required=false)
    private ZmBoolean isCalendarForward;

    /**
     * @zm-api-field-tag no-save
     * @zm-api-field-description If set, a copy will <b>not</b> be saved to sent regardless of account/identity
     * settings
     */
    @XmlAttribute(name=MailConstants.A_NO_SAVE_TO_SENT /* noSave */, required=false)
    private ZmBoolean noSaveToSent;

    /**
     * @zm-api-field-tag fetch-saved-msg
     * @zm-api-field-description If set, return the copy of the sent message, if it was saved, in the response.
     */
    @XmlAttribute(name=MailConstants.A_FETCH_SAVED_MSG /* fetchSavedMsg */, required=false)
    private ZmBoolean fetchSavedMsg;

    /**
     * @zm-api-field-tag send-uid
     * @zm-api-field-description Send UID
     */
    @XmlAttribute(name=MailConstants.A_SEND_UID /* suid */, required=false)
    private String sendUid;

    // E_INVITE child is not allowed
    /**
     * @zm-api-field-description Message
     */
    @XmlElement(name=MailConstants.E_MSG /* m */, required=false)
    private MsgToSend msg;

    public SendMsgRequest() {
    }

    /**
     * @zm-api-field-tag sign
     * @zm-api-field-description Sign mime
     */
    @XmlAttribute(name= SmimeConstants.A_SIGN, required=false)
    protected ZmBoolean sign;

    /**
     * @zm-api-field-tag encrypt
     * @zm-api-field-description Encrypt mime
     */
    @XmlAttribute(name=SmimeConstants.A_ENCRYPT, required=false)
    protected ZmBoolean encrypt;

    /**
     * @zm-api-field-tag encryptionType
     * @zm-api-field-description smime / pgp.
     */
    @XmlAttribute(name=SmimeConstants.A_ENCRYPTION_TYPE /* encryptionType */, required=false)
    protected EncryptionType encryptionType;

    /**
     * @zm-api-field-tag encryptionPassword
     * @zm-api-field-description Secure email password can be used for smime or pgp certificate password.
     */
    @XmlAttribute(name=SmimeConstants.A_ENCRYPTION_PASSWORD /* encryptionPassword */, required=false)
    protected String encryptionPassword;

    public void setNeedCalendarSentbyFixup(Boolean needCalendarSentbyFixup) {
        this.needCalendarSentbyFixup = ZmBoolean.fromBool(needCalendarSentbyFixup);
    }
    
    public void setIsCalendarForward(Boolean isCalendarForward) {
        this.isCalendarForward = ZmBoolean.fromBool(isCalendarForward);
    }

    public void setNoSaveToSent(Boolean noSaveToSent) {
        this.noSaveToSent = ZmBoolean.fromBool(noSaveToSent);
    }

    public void setFetchSavedMsg(Boolean fetchSavedMsg) {
        this.fetchSavedMsg = ZmBoolean.fromBool(fetchSavedMsg);
    }

    public void setSendUid(String sendUid) { this.sendUid = sendUid; }
    public void setMsg(MsgToSend msg) { this.msg = msg; }

    public Boolean getNeedCalendarSentbyFixup() { return ZmBoolean.toBool(needCalendarSentbyFixup); }
    public Boolean getIsCalendarForward() { return ZmBoolean.toBool(isCalendarForward); }
    public Boolean getNoSaveToSent() { return ZmBoolean.toBool(noSaveToSent); }
    public Boolean getFetchSavedMsg() { return ZmBoolean.toBool(fetchSavedMsg); }
    public String getSendUid() { return sendUid; }
    public MsgToSend getMsg() { return msg; }

    public Boolean getSign() {
        return ZmBoolean.toBool(sign, false);
    }

    public void setSign(Boolean sign) {
        this.sign = ZmBoolean.fromBool(sign, false);
    }


    public Boolean getEncrypt() {
        return ZmBoolean.toBool(encrypt, false);
    }

    public void setEncrypt(Boolean encrypt) {
        this.encrypt = ZmBoolean.fromBool(encrypt, false);;
    }

    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(EncryptionType encryptionType) {
        this.encryptionType = encryptionType;
    }

    public String getEncryptionPassword() {
        return encryptionPassword;
    }

    public void setEncryptionPassword(String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("needCalendarSentbyFixup", needCalendarSentbyFixup)
            .add("isCalendarForward", isCalendarForward)
            .add("noSaveToSent", noSaveToSent)
            .add("fetchSavedMsg", fetchSavedMsg)
            .add("sendUid", sendUid)
            .add("msg", msg)
            .add(SmimeConstants.A_SIGN, sign)
            .add(SmimeConstants.A_ENCRYPT, encrypt)
            .add(SmimeConstants.A_ENCRYPTION_PASSWORD, encryptionPassword);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }

    @XmlEnum
    public enum EncryptionType {
        smime,
        pgp;

        public static EncryptionType fromString(String value) throws ServiceException {
            if (value == null) {
                return null;
            }
            try {
                return EncryptionType.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw ServiceException.INVALID_REQUEST(
                        "Invalid value: " + value + ", valid values: " + Arrays.asList(EncryptionType.values()), null);
            }
        }
    }
}
