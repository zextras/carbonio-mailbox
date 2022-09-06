// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.mailbox.ItemIdentifier;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.index.ContactHit;
import com.zimbra.cs.index.SearchParams;
import com.zimbra.cs.index.SortBy;
import com.zimbra.cs.index.ZimbraHit;
import com.zimbra.cs.index.ZimbraQueryResults;
import com.zimbra.cs.mailbox.ContactGroup.Member;
import com.zimbra.cs.mailbox.ContactGroup.Member.Type;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactMemberOfMap {

  public static Map<String, Set<String>> getMemberOfMap(Mailbox mbox, OperationContext octxt) {
    long start = System.currentTimeMillis();
    SearchParams params = new SearchParams();
    params.setQueryString("#type:group");
    Set<MailItem.Type> types = Sets.newHashSetWithExpectedSize(1);
    types.add(MailItem.Type.CONTACT);
    params.setTypes(types);
    params.setSortBy(SortBy.NONE);
    OperationContext opCtxt;
    try {
      opCtxt = octxt != null ? octxt : new OperationContext(mbox.getAccount());
    } catch (ServiceException se) {
      ZimbraLog.contact.debug("Problem creating OperationContext for getMemberOfMap", se);
      return Collections.emptyMap();
    }
    try (ZimbraQueryResults results = mbox.index.search(SoapProtocol.Soap12, opCtxt, params)) {
      Map<String, Set<String>> memberOfMap = Maps.newHashMap();
      while (results.hasNext()) {
        ZimbraHit hit = results.getNext();
        if (hit instanceof ContactHit) {
          Contact contact = ((ContactHit) hit).getContact();
          ContactGroup contactGroup = null;
          try {
            contactGroup =
                ContactGroup.init(contact.get(ContactConstants.A_groupMember), mbox.getAccountId());
            List<Member> members = contactGroup.getMembers();
            if (members == null) {
              continue;
            }
            for (Member member : members) {
              if (!Type.CONTACT_REF.equals(member.getType())) {
                continue;
              }
              String memberId = member.getValue();
              if (!memberOfMap.containsKey(memberId)) {
                memberOfMap.put(memberId, Sets.newHashSet());
              }
              memberOfMap.get(memberId).add(hit.getParsedItemID().toString());
            }
          } catch (ServiceException e) {
            ZimbraLog.contact.debug("can't get group members for Contact %d", contact.getId(), e);
          }
        }
      }
      ZimbraLog.contact.debug(
          "getMemberOfMap for %s - %s members %s",
          mbox.getAccount().getName(),
          memberOfMap.size(),
          ZimbraLog.elapsedTime(start, System.currentTimeMillis()));
      return memberOfMap;
    } catch (ServiceException | IOException e) {
      ZimbraLog.contact.debug("Problem creating MemberOf map", e);
    }
    return Collections.emptyMap();
  }

  public static Set<String> setOfMemberOf(
      String acctId, int id, Map<String, Set<String>> memberOfMap) {
    if (memberOfMap == null) {
      return null;
    }
    Set<String> memberOf = memberOfMap.get(Integer.toString(id));
    if (memberOf != null) {
      return memberOf;
    }
    ItemIdentifier ident = ItemIdentifier.fromAccountIdAndItemId(acctId, id);
    return memberOfMap.get(ident.toString());
  }
}
