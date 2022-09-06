// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.soap.mail.type.CreateCalendarItemResponse;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CreateAppointmentResponse")
@GraphQLType(
    name = GqlConstants.CLASS_CREATE_APPOINTMENT_RESPONSE,
    description = "Contains response information for create appointment")
public class CreateAppointmentResponse extends CreateCalendarItemResponse {}
