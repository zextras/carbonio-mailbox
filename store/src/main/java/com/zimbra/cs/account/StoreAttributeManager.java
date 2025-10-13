package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;

/**
 * Just a wrapper to get {@link AttributeManager} and map with a {@link ServiceException}
 */
public class StoreAttributeManager {

	public static AttributeManager getInstance() throws ServiceException {
		try {
			return AttributeManager.getInstance();
		} catch (AttributeManagerException e) {
			throw ServiceException.FAILURE(e.getMessage());
		}
	}

}
