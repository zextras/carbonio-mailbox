// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.zimlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.ZimletConstants;

/**
 * 
 * @author jylee
 *
 */
public class ZimletDescription extends ZimletMeta {
	
	public static final String ZIMLET_REGEX_EXTENSION_CLASS = "com.zimbra.cs.zimlet.handler.RegexHandler";
	
	private List<String> mScripts;
	private List<String> mStyleSheets;
	private String mContentObject;
	private String mPanelItem;
	private String mExtensionClass;
	private String mKeyword;
	private String mRegexString;
	private List<String> mTargets;
	private String mDisableUIUndeploy;
	
	public ZimletDescription(File desc) throws ZimletException {
		super(desc);
	}

	public ZimletDescription(String desc) throws ZimletException {
		super(desc);
	}
	
	protected void initialize() {
		mScripts = new ArrayList<>();
		mStyleSheets = new ArrayList<>();
		mTargets = new ArrayList<>();
	}

	protected void validateElement(Element elem) throws ZimletException {
		if (elem.getName().equals(ZimletConstants.ZIMLET_TAG_CONTENT_OBJECT)) {
			mContentObject = elem.toString();
		} else if (elem.getName().equals(ZimletConstants.ZIMLET_TAG_PANEL_ITEM)) {
			mPanelItem = elem.toString();
		} else if (elem.getName().equals(ZimletConstants.ZIMLET_TAG_SERVER_EXTENSION)) {
			parseServerExtension(elem);
		} else if (elem.getName().equals(ZimletConstants.ZIMLET_TAG_SCRIPT)) {
			parseResource(elem);
		} else if (elem.getName().equals(ZimletConstants.ZIMLET_TAG_CSS)) {
			parseCss(elem);
		} else if (elem.getName().equals(ZimletConstants.ZIMLET_TAG_TARGET)) {
			mTargets.add(elem.getText());
		} else if (elem.getName().equals(ZimletConstants.ZIMLET_DISABLE_UI_UNDEPLOY)) {
			mDisableUIUndeploy = elem.getText();
		}
	}

	private void parseResource(Element resource) throws ZimletException {
		mScripts.add(resource.getText());
	}

	private void parseCss(Element css) throws ZimletException {
		mStyleSheets.add(css.getText());
	}
	
	private void parseServerExtension(Element serverExt) throws ZimletException {
		assert(serverExt.getName().equals(ZimletConstants.ZIMLET_TAG_SERVER_EXTENSION));
		String val = serverExt.getAttribute(ZimletConstants.ZIMLET_ATTR_HAS_KEYWORD, "");
		if (val.length() > 0) {
			mKeyword = val;
		}
		val = serverExt.getAttribute(ZimletConstants.ZIMLET_ATTR_EXTENSION_CLASS, "");
		if (val.length() > 0) {
			mExtensionClass = val;
		}
		val = serverExt.getAttribute(ZimletConstants.ZIMLET_ATTR_REGEX, "");
		if (val.length() > 0) {
			mExtensionClass = ZIMLET_REGEX_EXTENSION_CLASS;
			mRegexString = val;
		}
	}
	
	public String getContentObjectAsXML() {
		assert(mTopElement != null);
		return mContentObject;
	}
	
	public String getPanelItemAsXML() {
		assert(mTopElement != null);
		return mPanelItem;
	}
	
	public String getContentObjectAsJSON() {
		assert(mTopElement != null);
		return null;
	}
	
	public String getPanelItemAsJSON() {
		assert(mTopElement != null);
		return null;
	}
	
	public String[] getScripts() {
		if (mScripts.isEmpty()) {
			return new String[0];
		}
		return mScripts.toArray(new String[0]);
	}
	
	public String[] getStyleSheets() {
		if (mStyleSheets.isEmpty()) {
			return new String[0];
		}
		return mStyleSheets.toArray(new String[0]);
	}
	
	public String getServerExtensionClass() {
		return mExtensionClass;
	}
	
	public String getServerExtensionKeyword() {
		return mKeyword;
	}
	
	public boolean getServerExtensionHasKeyword() {
		return (mKeyword != null);
	}
	
	public String getRegexString() {
		return mRegexString;
	}
	
	public boolean checkTarget(String target) {
		return (mTargets.contains(target));
	}
	
	public List<String> getTargets() {
		return mTargets;
	}
	
	public String getDisableUIUndeploy() {
		return mDisableUIUndeploy;
	}
}
