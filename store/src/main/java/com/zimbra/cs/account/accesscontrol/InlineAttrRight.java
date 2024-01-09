// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeManager;

public class InlineAttrRight extends AttrRight {

    private static final String PARTS_SEPARATOR = ".";
    private static final String PARTS_SEPARATOR_ESC = "\\.";
    private static final String OP_GET = "get";
    private static final String OP_SET = "set";
    
    static InlineAttrRight newInlineAttrRight(String right) throws ServiceException {
        String[] parts = right.split(PARTS_SEPARATOR_ESC);
        if (parts.length != 3)
            throw ServiceException.PARSE_ERROR("inline attr right might have 3 parts", null);
        
        RightType rightType;
        if (OP_GET.equals(parts[0]))
            rightType = RightType.getAttrs;
        else if (OP_SET.equals(parts[0]))
            rightType = RightType.setAttrs;
        else
            throw ServiceException.PARSE_ERROR("invalid op for inline attr right: " + parts[0], null);
        
        TargetType targetType = TargetType.fromCode(parts[1]);
        if (targetType == TargetType.global)
            throw ServiceException.PARSE_ERROR("target type for inline attr right cannot be: " + parts[1], null);
        
        String attrName = parts[2];
        
        // use the {op}.{target-type}.{attr-name} string as the right name and description
        InlineAttrRight iar = new InlineAttrRight(right, rightType, AttributeManager.getInstance());
        iar.setDesc(right);
        iar.setTargetType(targetType);
        iar.validateAttr(attrName);
        iar.addAttr(attrName);
        iar.completeRight();
        
        return iar;
    }
    
    public static String composeGetRight(TargetType targetType, String attrName) {
        return composeRight(OP_GET, targetType, attrName);
    }
    
    public static String composeSetRight(TargetType targetType, String attrName) {
        return composeRight(OP_SET, targetType, attrName);
    }
    
    private static String composeRight(String op, TargetType targetType, String attrName) {
        return op + PARTS_SEPARATOR + targetType.getCode() + PARTS_SEPARATOR + attrName;
    }

    static boolean looksLikeOne(String right) {
        if (right.contains("."))
            return true;
        else 
            return false;
    }
    
    private InlineAttrRight(String name, RightType rightType, AttributeManager attributeManager) throws ServiceException {
        super(name, rightType, attributeManager);
    }
    
    @Override
    boolean isTheSameRight(Right other) {
        return getName().equals(other.getName());
    }
}
