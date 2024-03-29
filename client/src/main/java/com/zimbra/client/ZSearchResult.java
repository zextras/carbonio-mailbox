// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.google.common.base.MoreObjects;
import com.zimbra.client.event.ZModifyConversationEvent;
import com.zimbra.client.event.ZModifyEvent;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;

public class ZSearchResult implements ToZJSONObject {

  private List<ZSearchHit> hits;
  private List<ZImapSearchHit> imapHits;
  private ZConversationSummary convSummary;
  private boolean hasMore;
  private String sortBy;
  private int offset;

  public ZSearchResult(Element e, boolean convNest, TimeZone tz) throws ServiceException {
    if (!convNest) {
      init(e, e, tz);
    } else {
      Element c = e.getElement(MailConstants.E_CONV);
      convSummary = new ZConversationSummary(c);
      init(e, c, tz);
    }
  }

  public ZSearchResult(
      List<ZSearchHit> hits,
      ZConversationSummary convSummary,
      boolean hasMore,
      String sortBy,
      int offset) {
    this.hits = hits;
    this.convSummary = convSummary;
    this.hasMore = hasMore;
    this.sortBy = sortBy;
    this.offset = offset;
  }

  private void init(Element resp, Element el, TimeZone tz) throws ServiceException {
    sortBy = resp.getAttribute(MailConstants.A_SORTBY);
    hasMore = resp.getAttributeBool(MailConstants.A_QUERY_MORE);
    offset = (int) resp.getAttributeLong(MailConstants.A_QUERY_OFFSET, -1);
    hits = new ArrayList<>();
    imapHits = new ArrayList<>();
    for (Element h : el.listElements()) {
      if (h.getName().equals(MailConstants.E_CONV)) {
        hits.add(new ZConversationHit(h));
      } else if (h.getName().equals(MailConstants.E_MSG)) {
        ZMessageHit hit = new ZMessageHit(h);
        hits.add(hit);
        imapHits.add(hit);
      } else if (h.getName().equals(MailConstants.E_CONTACT)) {
        ZContactHit hit = new ZContactHit(h);
        hits.add(hit);
        imapHits.add(hit);
      } else if (h.getName().equals(MailConstants.E_APPOINTMENT)) {
        ZAppointmentHit.addInstances(h, hits, tz, false);
      } else if (h.getName().equals(MailConstants.E_HIT)) {
        ZIdHit hit = new ZIdHit(h);
        hits.add(hit);
        imapHits.add(hit);
      }
    }
  }

  /**
   * @return ZSearchHit objects from search
   */
  public List<ZSearchHit> getHits() {
    return hits;
  }

  public List<ZImapSearchHit> getImapHits() {
    return imapHits;
  }

  public ZConversationSummary getConversationSummary() {
    return convSummary;
  }

  /**
   * @return true if there are more search results on the server
   */
  public boolean hasMore() {
    return hasMore;
  }

  /**
   * @return the sort by value
   */
  public String getSortBy() {
    return sortBy;
  }

  /**
   * @return offset of the search
   */
  public int getOffset() {
    return offset;
  }

  @Override
  public ZJSONObject toZJSONObject() throws JSONException {
    ZJSONObject zjo = new ZJSONObject();
    zjo.put("more", hasMore);
    zjo.put("sortBy", sortBy);
    zjo.put("offset", offset);
    zjo.put("hits", hits);
    return zjo;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("size", hits.size())
        .add("more", hasMore)
        .toString();
  }

  public String dump() {
    return ZJSONObject.toString(this);
  }

  /*
   * TODO: this class is really not a ZSearchHit, but for now that works best do to ZSearchPagerCache. modifyNotication handling
   */
  public static class ZConversationSummary implements ZSearchHit {

    private String mId;
    private String mFlags;
    private String mTags;
    private int mMessageCount;

    public ZConversationSummary(Element e) throws ServiceException {
      mId = e.getAttribute(MailConstants.A_ID);
      mFlags = e.getAttribute(MailConstants.A_FLAGS, null);
      mTags = e.getAttribute(MailConstants.A_TAGS, null);
      mMessageCount = (int) e.getAttributeLong(MailConstants.A_NUM);
    }

    @Override
    public void modifyNotification(ZModifyEvent event) throws ServiceException {
      if (event instanceof ZModifyConversationEvent) {
        ZModifyConversationEvent cevent = (ZModifyConversationEvent) event;
        mFlags = cevent.getFlags(mFlags);
        mTags = cevent.getTagIds(mTags);
        mMessageCount = cevent.getMessageCount(mMessageCount);
      }
    }

    @Override
    public String getId() {
      return mId;
    }

    @Override
    public String getSortField() {
      return null;
    }

    @Override
    public ZJSONObject toZJSONObject() throws JSONException {
      ZJSONObject zjo = new ZJSONObject();
      zjo.put("id", mId);
      zjo.put("flags", mFlags);
      zjo.put("tags", mTags);
      zjo.put("messageCount", mMessageCount);
      return zjo;
    }

    @Override
    public String toString() {
      return String.format("[ZConversationSummary %s]", mId);
    }

    public String dump() {
      return ZJSONObject.toString(this);
    }

    public String getFlags() {
      return mFlags;
    }

    public String getTagIds() {
      return mTags;
    }

    public int getMessageCount() {
      return mMessageCount;
    }

    public boolean hasFlags() {
      return mFlags != null && mFlags.length() > 0;
    }

    public boolean hasTags() {
      return mTags != null && mTags.length() > 0;
    }

    public boolean hasAttachment() {
      return hasFlags() && mFlags.indexOf(ZConversation.Flag.attachment.getFlagChar()) != -1;
    }

    public boolean isFlagged() {
      return hasFlags() && mFlags.indexOf(ZConversation.Flag.flagged.getFlagChar()) != -1;
    }

    public boolean isSentByMe() {
      return hasFlags() && mFlags.indexOf(ZConversation.Flag.sentByMe.getFlagChar()) != -1;
    }

    public boolean isUnread() {
      return hasFlags() && mFlags.indexOf(ZConversation.Flag.unread.getFlagChar()) != -1;
    }

    public boolean isDraft() {
      return hasFlags() && mFlags.indexOf(ZConversation.Flag.draft.getFlagChar()) != -1;
    }
  }
}
