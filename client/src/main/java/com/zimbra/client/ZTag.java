// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;

import com.zimbra.client.event.ZModifyTagEvent;
import com.zimbra.common.mailbox.ZimbraTag;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.RetentionPolicy;

public class ZTag implements Comparable<ZTag>, ZItem, ZimbraTag, ToZJSONObject {

    private Color mColor;
    private String mId;
    private String mName;
    private int mUnreadCount;
    private RetentionPolicy mRetentionPolicy = new RetentionPolicy();

    public enum Color {

        defaultColor(0),
        blue(1),
        cyan(2),
        green(3),
        purple(4),
        red(5),
        yellow(6),
        pink(7),
        gray(8),
        orange(9),
        rgbColor;

        private long mValue;
        private static Map<String,Color> colorMap = new HashMap<>();

        static {
          for (Color c : Color.values())
                colorMap.put(c.toString(), c);
        }

        public long getValue() { return mValue; }

        public static Color fromString(String s) throws ServiceException {
            if (s != null) {
                s = s.toLowerCase();
                if (colorMap.containsKey(s)) {
                    return colorMap.get(s);
                }
            }
            return Color.values()[com.zimbra.common.mailbox.Color.getMappedColor(s)];
        }

        public Color setRgbColor(String s) {
            mValue = new com.zimbra.common.mailbox.Color(s).getValue();
            return this;
        }

        public String getRgbColor() {
            return new com.zimbra.common.mailbox.Color(mValue).toString();
        }

        Color(long value) { mValue = value; }
        Color() {}
    }

    public ZTag(Element e) throws ServiceException {
        String rgb = e.getAttribute(MailConstants.A_RGB, null);
        // Server reports color or rgb attribute on mail items but not both.
        // If rgb, map the color to the rgb value. If the attr is color, return the value as is.
        if (rgb != null) {
            mColor =  Color.rgbColor.setRgbColor(rgb);
        } else {
            String s = e.getAttribute(MailConstants.A_COLOR, "0");
            mColor = Color.values()[(byte)Long.parseLong(s)];
        }
        mId = e.getAttribute(MailConstants.A_ID);
        mName = e.getAttribute(MailConstants.A_NAME);
        mUnreadCount = (int) e.getAttributeLong(MailConstants.A_UNREAD, 0);

        Element rpEl = e.getOptionalElement(MailConstants.E_RETENTION_POLICY);
        if (rpEl != null) {
            mRetentionPolicy = new RetentionPolicy(rpEl);
        }
    }

    public void modifyNotification(ZModifyTagEvent tevent) throws ServiceException {
	    mColor = tevent.getColor(mColor);
	    mName = tevent.getName(mName);
	    mUnreadCount = tevent.getUnreadCount(mUnreadCount);
	    mRetentionPolicy = tevent.getRetentionPolicy(mRetentionPolicy);
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getUuid() {
        return null;
    }

	/** Returns the folder's name.  Note that this is the folder's
     *  name (e.g. <code>"foo"</code>), not its absolute pathname
     *  (e.g. <code>"/baz/bar/foo"</code>).
     *
     *
     */
    public String getName() {
        return mName;
    }

    /**
     * @return number of unread items in folder
     */
    public int getUnreadCount() {
        return mUnreadCount;
    }

    public Color getColor() {
        return mColor;
    }

  @Override
    public ObjectNode toObjectNode() {
        ObjectNode zjo = JsonNodeFactory.instance.objectNode();
        if (mId != null) zjo.put("id", mId);
        if (mName != null) zjo.put("name", mName);
        zjo.put("color", mColor.name());
        zjo.put("unreadCount", mUnreadCount);
        return zjo;
    }

    @Override
    public String toString() {
        return String.format("[ZTag %s]", mName);
    }

    public String dump() {
        return ToZJSONObject.toString(this);
    }

    @Override
    public int compareTo(ZTag other) {
        return getName().compareToIgnoreCase(other.getName());
    }

    @Override
    public int getTagId() {
        return Integer.parseInt(getId());
    }

    @Override
    public String getTagName() {
        return getName();
    }
}
