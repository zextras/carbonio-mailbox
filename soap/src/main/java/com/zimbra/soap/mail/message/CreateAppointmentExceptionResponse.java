// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.soap.mail.type.CreateCalendarItemResponse;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "CreateAppointmentExceptionResponse")
@GraphQLType(
    name = GqlConstants.CLASS_CREATE_APPOINTMENT_EXCEPTION_RESPONSE,
    description = "Contains response information for create appointment exception")
public class CreateAppointmentExceptionResponse extends CreateCalendarItemResponse {}
