// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.CommentInfo;
import com.zimbra.soap.mail.type.IdEmailName;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_COMMENTS_RESPONSE)
public class GetCommentsResponse {

    /**
     * @zm-api-field-description Users
     */
    @XmlElement(name=MailConstants.A_USER /* user */, required=false)
    private List<IdEmailName> users = Lists.newArrayList();

    /**
     * @zm-api-field-description Comment information
     */
    @XmlElement(name=MailConstants.E_COMMENT /* comment */, required=false)
    private List<CommentInfo> comments = Lists.newArrayList();

    public GetCommentsResponse() {
    }

    public void setComments(Iterable <CommentInfo> comments) {
        this.comments.clear();
        if (comments != null) {
            Iterables.addAll(this.comments,comments);
        }
    }

    public void addComment(CommentInfo comment) {
        this.comments.add(comment);
    }

    public void setUsers(Iterable <IdEmailName> users) {
        this.users.clear();
        if (users != null) {
            Iterables.addAll(this.users,users);
        }
    }

    public void addUser(IdEmailName user) {
        this.users.add(user);
    }

    public List<CommentInfo> getComments() {
        return comments;
    }
    public List<IdEmailName> getUsers() {
        return users;
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("comments", comments)
            .add("users", users);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
