// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class FreeBusySlot {

    /**
     * @zm-api-field-tag start-millis-gmt
     * @zm-api-field-description GMT Start time for slot in milliseconds
     */
    @XmlAttribute(name=MailConstants.A_CAL_START_TIME /* s */, required=true)
    private final long startTime;

    /**
     * @zm-api-field-tag end-millis-gmt
     * @zm-api-field-description GMT End time for slot in milliseconds
     */
    @XmlAttribute(name=MailConstants.A_CAL_END_TIME /* e */, required=true)
    private final long endTime;
    
    /**
     * @zm-api-field-tag id
     * @zm-api-field-description calendar event id
     */
    @XmlAttribute(name=MailConstants.E_CAL_EVENT_ID, required=false)
    private String id;
    
    /**
     * @zm-api-field-tag subject
     * @zm-api-field-description Appointment subject
     */
    @XmlAttribute(name=MailConstants.E_CAL_EVENT_SUBJECT, required=false)
    private String subject;
    
    /**
     * @zm-api-field-tag location
     * @zm-api-field-description location of meeting
     */
    @XmlAttribute(name=MailConstants.E_CAL_EVENT_LOCATION, required=false)
    private String location;
    
    /**
     * @zm-api-field-tag isMeeting
     * @zm-api-field-description returns a boolean value whether this calendar event is a meeting or not.
     */
    @XmlAttribute(name=MailConstants.E_CAL_EVENT_ISMEETING, required=false)
    private boolean isMeeting;
    
    /**
     * @zm-api-field-tag isRecurring
     * @zm-api-field-description returns a boolean indicating whether it is continuous or not.
     */
    @XmlAttribute(name=MailConstants.E_CAL_EVENT_ISRECURRING, required=false)
    private boolean isRecurring;
    
    /**
     * @zm-api-field-tag isException
     * @zm-api-field-description returns a boolean indicating whether there is any exception or not.
     */
    @XmlAttribute(name=MailConstants.E_CAL_EVENT_ISEXCEPTION, required=false)
    private boolean isException;
    
    /**
     * @zm-api-field-tag isReminderSet
     * @zm-api-field-description returns a boolean indicating whether any reminder has been set or not.
     */
    @XmlAttribute(name=MailConstants.E_CAL_EVENT_ISREMINDERSET, required=false)
    private boolean isReminderSet;
    
    /**
     * @zm-api-field-tag isPrivate
     * @zm-api-field-description returns a boolean indicating whether this meeting is private or not.
     */
    @XmlAttribute(name=MailConstants.E_CAL_EVENT_ISPRIVATE, required=false)
    private boolean isPrivate;
    
    /**
     * @zm-api-field-tag hasPermission
     * @zm-api-field-description returns a boolean indicating hasPermission to view FreeBusy information
     */
    @XmlAttribute(name=MailConstants.E_CAL_EVENT_HASPERMISSION, required=false)
    private boolean hasPermission;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    protected FreeBusySlot() {
        this(-1L, -1L);
    }
    public FreeBusySlot(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean isMeeting() {
		return isMeeting;
	}

	public void setMeeting(boolean isMeeting) {
		this.isMeeting = isMeeting;
	}

	public boolean isRecurring() {
		return isRecurring;
	}

	public void setRecurring(boolean isRecurring) {
		this.isRecurring = isRecurring;
	}

	public boolean isException() {
		return isException;
	}

	public void setException(boolean isException) {
		this.isException = isException;
	}

	public boolean isReminderSet() {
		return isReminderSet;
	}

	public void setReminderSet(boolean isReminderSet) {
		this.isReminderSet = isReminderSet;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public boolean isHasPermission() {
		return hasPermission;
	}

	public void setHasPermission(boolean hasPermission) {
		this.hasPermission = hasPermission;
	}

	public String getStatus() {
	    switch (this.getClass().getSimpleName()) {
	    case "FreeBusyFREEslot":
	        return MailConstants.E_FREEBUSY_FREE;
	    case "FreeBusyBUSYslot":
	        return MailConstants.E_FREEBUSY_BUSY;
	    case "FreeBusyBUSYTENTATIVEslot":
	        return MailConstants.E_FREEBUSY_BUSY_TENTATIVE;
	    case "FreeBusyBUSYUNAVAILABLEslot":
	        return MailConstants.E_FREEBUSY_BUSY_UNAVAILABLE;
        default:
            return MailConstants.E_FREEBUSY_NODATA;
	    }
	}

	public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper.add("startTime", startTime).add("endTime", endTime).add("id", id).add("subject", subject).add("location", location).add("isMeeting", isMeeting)
        		.add("isRecurring", isRecurring).add("isException", isException).add("isPrivate", isPrivate).add("isReminderSet", isReminderSet).add("hasPermission", hasPermission);
    }
    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
