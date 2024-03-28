// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import static com.zimbra.cs.filter.JsieveConfigMapHandler.CAPABILITY_FILEINTO;
import static com.zimbra.cs.filter.JsieveConfigMapHandler.CAPABILITY_COPY;

import java.util.List;
import org.apache.jsieve.Argument;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.Block;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.exception.SyntaxException;
import org.apache.jsieve.mail.MailAdapter;

import com.zimbra.cs.filter.FilterUtil;
import com.zimbra.cs.filter.ZimbraMailAdapter;


public class FileInto extends org.apache.jsieve.commands.optional.FileInto {
	
    @Override
    protected Object executeBasic(MailAdapter mail, Arguments arguments, Block block,
        SieveContext context) throws SieveException {
        if (!(mail instanceof ZimbraMailAdapter)) {
            return null;
        }
        ZimbraMailAdapter mailAdapter = (ZimbraMailAdapter) mail;
        Require.checkCapability(mailAdapter, CAPABILITY_FILEINTO);
        List<Argument> args = arguments.getArgumentList();
        if (args.size() == 1) {
            String folderPath = ((StringListArgument) arguments.getArgumentList().get(0)).getList()
                .get(0);
            folderPath = FilterUtil.replaceVariables(mailAdapter, folderPath);
            mail.addAction(new ActionFileInto(folderPath));
        } else {
            Require.checkCapability(mailAdapter, CAPABILITY_COPY);
            String folderPath = ((StringListArgument) arguments.getArgumentList().get(1)).getList()
                .get(0);
            folderPath = FilterUtil.replaceVariables(mailAdapter, folderPath);
            mail.addAction(new ActionFileInto(folderPath, true));
        }
        return null;
    }

	@Override
	protected void validateArguments(Arguments arguments, SieveContext context) throws SieveException {
		List<Argument> args = arguments.getArgumentList();
	    if (args.size() < 1 || args.size() > 2) {
	      throw new SyntaxException("Exactly 1 or 2 arguments permitted. Found " + args.size());
	    }
	    Argument argument;
	    String copyArg;
	    if(args.size() == 1) {
	    	// folder list argument
	    	argument = args.get(0);
	    } else {
	    	copyArg = args.get(0).getValue().toString();
	    	// if arguments size is 2; first argument should be :copy
            if (!Copy.COPY.equalsIgnoreCase(copyArg)) {
	  	      throw new SyntaxException("Error in sieve fileinto. Expecting argument :copy");
	  	    } 
	    	// folder list argument
	    	argument = args.get(1);
	    }
	    // folder list argument should be a String list
	    if (!(argument instanceof StringListArgument)) {
            throw new SyntaxException("Expecting a string-list");
	    } 
	    // folder list argument should contain exactly one folder name  
	    if (1 != ((StringListArgument)argument).getList().size()) {
	      throw new SyntaxException("Expecting exactly one argument");
	    }
	}
}
