/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.account;

import static com.zimbra.cs.account.AttributeInfo.DURATION_PATTERN_DOC;
import static com.zimbra.cs.account.EmailValidator.validEmailAddress;

import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.DateUtil;
import com.zimbra.common.util.ZimbraLog;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class LdapAttributeInfo {
	private static final Map<String, AttributeCallback> callbacks = new ConcurrentHashMap<>();
	private final AttributeInfo attributeInfo;

	//  8        4  4     4      12
	//8cf3db5d-cfd7-11d9-884f-e7b38f15492d
	private static final Pattern ID_PATTERN =
			Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	//yyyyMMddHHmmssZ or yyyyMMddHHmmss.SSSZ
	private static final Pattern GENTIME_PATTERN = Pattern.compile("^\\d{14}(\\.\\d{1,3})?[zZ]$");

	private static final Pattern DURATION_PATTERN = Pattern.compile("^\\d+([hmsd]|ms)?$");
	public static LdapAttributeInfo get(AttributeInfo attributeInfo) {
		return new LdapAttributeInfo(attributeInfo);
	}

	private LdapAttributeInfo(AttributeInfo attributeInfo) {
		this.attributeInfo = attributeInfo;
	}

	public synchronized AttributeCallback getCallback() {
		return callbacks.computeIfAbsent(attributeInfo.getName(), key -> {
			try {
				return (AttributeCallback) Class.forName(attributeInfo.getCallbackClassName())
						.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				// Consider logging here
				System.err.println("Failed to instantiate callback for " + key + ": " + e.getMessage());
				return null;
			}
		});
	}



	public void checkValue(Object value, boolean checkImmutable, Map attrsToModify) throws ServiceException {
		if ((value == null) || (value instanceof String)) {
			checkValue((String) value, checkImmutable, attrsToModify);
		} else if (value instanceof String[]) {
			String[] values = (String[]) value;
			for (String s : values)
				checkValue(s, checkImmutable, attrsToModify);
		}

		if (attributeInfo.isDeprecated() && !DebugConfig.allowModifyingDeprecatedAttributes) {
			throw ServiceException.FAILURE("modifying deprecated attribute is not allowed: " + attributeInfo.getName(), null);
		}
	}

	private void checkValue(String value, boolean checkImmutable, Map attrsToModify) throws ServiceException {
		if (checkImmutable && attributeInfo.ismImmutable())
			throw ServiceException.INVALID_REQUEST(attributeInfo.getName() +" is immutable", null);
		checkValue(value, attrsToModify);
	}

	private void checkValue(String value, Map attrsToModify) throws ServiceException {

		// means to delete/unset the attribute
		if (value == null || value.equals(""))
			return;

		switch (attributeInfo.mType) {
			case TYPE_BOOLEAN:
				if ("TRUE".equals(value) || "FALSE".equals(value)) {
					return;
				} else {
					throw AccountServiceException.INVALID_ATTR_VALUE(attributeInfo.getName() + " must be TRUE or FALSE", null);
				}
			case TYPE_BINARY:
			case TYPE_CERTIFICATE:
				byte[] binary = ByteUtil.decodeLDAPBase64(value);
				if (binary.length > attributeInfo.getmMax()) {
					throw AccountServiceException.INVALID_ATTR_VALUE(
							attributeInfo.getName() + " value length(" + binary.length + ") larger than max allowed: " + attributeInfo.getmMax(), null);
				}
				return;
			case TYPE_DURATION:
				if (!DURATION_PATTERN.matcher(value).matches()) {
					throw AccountServiceException.INVALID_ATTR_VALUE(attributeInfo.getName() + " " + DURATION_PATTERN_DOC, null);
				}
				long l = DateUtil.getTimeInterval(value, 0);
				if (l < attributeInfo.getmMin()) {
					throw AccountServiceException.INVALID_ATTR_VALUE(
							attributeInfo.getName() + " is shorter than minimum allowed: " + attributeInfo.getmMinDuration(), null);
				}
				if (l > attributeInfo.getmMax()) {
					throw AccountServiceException.INVALID_ATTR_VALUE(
							attributeInfo.getName() + " is longer than max allowed: " + attributeInfo.getmMaxDuration(), null);
				}
				return;
			case TYPE_EMAIL:
				if (value.length() > attributeInfo.getmMax()) {
					throw AccountServiceException.INVALID_ATTR_VALUE(
							attributeInfo.getName() + " value length(" + value.length() + ") larger than max allowed: " + attributeInfo.getmMax(), null);
				}
				validEmailAddress(value, false);
				return;
			case TYPE_EMAILP:
				if (value.length() > attributeInfo.getmMax()) {
					throw AccountServiceException.INVALID_ATTR_VALUE(
							attributeInfo.getName() + " value length(" + value.length() + ") larger than max allowed: " + attributeInfo.getmMax(), null);
				}
				validEmailAddress(value, true);
				return;
			case TYPE_CS_EMAILP:
				if (value.length() > attributeInfo.getmMax()) {
					throw AccountServiceException.INVALID_ATTR_VALUE(
							attributeInfo.getName() + " value length(" + value.length() + ") larger than max allowed: " + attributeInfo.getmMax(), null);
				}
				String[] emails = value.split(",");
				for (String email : emails) {
					validEmailAddress(email, true);
				}
				return;
			case TYPE_ENUM:
				if (attributeInfo.getmEnumSet().contains(value)) {
					return;
				} else {
					throw AccountServiceException.INVALID_ATTR_VALUE(attributeInfo.getName() + " must be one of: " + attributeInfo.getValue(), null);
				}
			case TYPE_GENTIME:
				if (GENTIME_PATTERN.matcher(value).matches()) {
					return;
				} else {
					throw AccountServiceException.INVALID_ATTR_VALUE(
							attributeInfo.getName() + " must be a valid generalized time: yyyyMMddHHmmssZ or yyyyMMddHHmmss.SSSZ", null);
				}
			case TYPE_ID:
				// For bug 21776 we check format for id only if the Provisioning class mandates
				// that all attributes of type id must be an UUID.
				//
				// Id is in UUID format, no need to check
//                if (!Provisioning.getInstance().idIsUUID()) {
//                    return;
//                }

				if (ID_PATTERN.matcher(value).matches()) {
					return;
				} else {
					throw AccountServiceException.INVALID_ATTR_VALUE(attributeInfo.getName() + " must be a valid id", null);
				}
			case TYPE_INTEGER:
				try {
					int v = Integer.parseInt(value);
					if (v < attributeInfo.getmMin()) {
						throw AccountServiceException.INVALID_ATTR_VALUE(
								attributeInfo.getName() + " value(" + v + ") smaller than minimum allowed: " + attributeInfo.getmMin(), null);
					}
					if (v > attributeInfo.getmMax()) {
						throw AccountServiceException.INVALID_ATTR_VALUE(
								attributeInfo.getName() + " value(" + v + ") larger than max allowed: " + attributeInfo.getmMax(), null);
					}
					return;
				} catch (NumberFormatException e) {
					throw AccountServiceException.INVALID_ATTR_VALUE(attributeInfo.getName() + " must be a valid integer: " + value, e);
				}
			case TYPE_LONG:
				try {
					long v = Long.parseLong(value);
					if (v < attributeInfo.getmMin()) {
						throw AccountServiceException.INVALID_ATTR_VALUE(
								attributeInfo.getName() + " value(" + v + ") smaller than minimum allowed: " + attributeInfo.getmMin(), null);
					}
					if (v > attributeInfo.getmMax()) {
						throw AccountServiceException.INVALID_ATTR_VALUE(
								attributeInfo.getName() + " value(" + v + ") larger than max allowed: " + attributeInfo.getmMax(), null);
					}
					return;
				} catch (NumberFormatException e) {
					throw AccountServiceException.INVALID_ATTR_VALUE(attributeInfo.getName() + " must be a valid long: " + value, e);
				}
			case TYPE_PORT:
				try {
					int v = Integer.parseInt(value);
					if (v >= 0 && v <= 65535) {
						return;
					}
					throw AccountServiceException.INVALID_ATTR_VALUE(attributeInfo.getName() + " must be a valid port: " + value, null);
				} catch (NumberFormatException e) {
					throw AccountServiceException.INVALID_ATTR_VALUE(attributeInfo.getName() + " must be a valid port: " + value, null);
				}
			case TYPE_STRING:
			case TYPE_ASTRING:
			case TYPE_OSTRING:
			case TYPE_CSTRING:
			case TYPE_PHONE:
				if (value.length() > attributeInfo.getmMax()) {
					throw AccountServiceException.INVALID_ATTR_VALUE(
							attributeInfo.getName() + " value length(" + value.length() + ") larger than max allowed: " + attributeInfo.getmMax(), null);
				}
				// TODO
				return;
			case TYPE_REGEX:
				if (attributeInfo.getmRegex().matcher(value).matches()) {
					return;
				} else {
					throw AccountServiceException.INVALID_ATTR_VALUE(attributeInfo.getName() + " must match the regex: " + attributeInfo.getValue(), null);
				}
			default:
				ZimbraLog.misc.warn("unknown type(" + attributeInfo.mType + ") for attribute: " + value);
				break;
		}
	}
}
