package com.zimbra.cs.account;

import com.zimbra.common.mime.shim.JavaMailInternetAddress;
import com.zimbra.common.service.ServiceException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class EmailValidator {

	public static void validEmailAddress(String addr, boolean personal) throws ServiceException {
		if (addr.indexOf('@') == -1) {
			throw AccountServiceException.INVALID_ATTR_VALUE("address '" + addr + "' does not include domain",
					null);
		}

		try {
			InternetAddress ia = new JavaMailInternetAddress(addr, true);
			// is this even needed?
			ia.validate();
			if (!personal && ia.getPersonal() != null && !ia.getPersonal().equals("")) {
				throw AccountServiceException.INVALID_ATTR_VALUE("invalid email address: " + addr, null);
			}
		} catch (AddressException e) {
			throw AccountServiceException.INVALID_ATTR_VALUE("invalid email address: " + addr, e);
		}
	}
}
