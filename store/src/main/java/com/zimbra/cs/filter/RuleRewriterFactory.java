// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import java.util.List;

import org.apache.jsieve.parser.generated.Node;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.ElementFactory;
import com.zimbra.cs.mailbox.Mailbox;

class RuleRewriterFactory {
	
	private static RuleRewriterFactory instance = null;	
	
    static synchronized RuleRewriterFactory getInstance() {
		if (instance == null) {
			instance = new RuleRewriterFactory();
		}
        return instance;
    }
    
    RuleRewriter createRuleRewriter() {
    	return new RuleRewriter();
    }
    
    RuleRewriter createRuleRewriter(ElementFactory factory, Node node, List<String> ruleNames) {
    	RuleRewriter rrw = createRuleRewriter();
    	rrw.initialize(factory, node, ruleNames);
    	return rrw;
    }
    
    RuleRewriter createRuleRewriter(Element eltRules, Mailbox mbox) {
     	RuleRewriter rrw = createRuleRewriter();
    	rrw.initialize(eltRules, mbox);
    	return rrw;
    }
}
