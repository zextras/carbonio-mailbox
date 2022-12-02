// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.apache.jsieve.CommandManager;
import org.apache.jsieve.ExecutableCommand;
import org.apache.jsieve.exception.LookupException;

import com.zimbra.cs.extension.ExtensionUtil;

/**
 * <p>For Bug 77287</p>
 * <p>
 * Maps command names to common implementations or
 *                    custom implementation with Zimbra Extension
 * </p>
 */
public class ZimbraCommandManagerImpl implements CommandManager {

    private static List<String> IMPLICITLY_DECLARED = Arrays.asList("if", "else", "elsif",
            "require", "stop", "keep", "discard", "redirect");

    private static boolean isImplicitlyDeclared(String name) {
        return IMPLICITLY_DECLARED.contains(name);
    }

    private final ConcurrentMap<String, String> classNameMap;

    /**
     * Constructor for Zimbra's own CommandManager.
     */
    public ZimbraCommandManagerImpl(final ConcurrentMap<String, String> classNameMap) {
        super();
        this.classNameMap = classNameMap;
    }

    /**
     * <p>
     * Method lookup answers the class to which a Command name is mapped.
     * </p>
     * 
     * @param name -
     *            The name of the Command
     * @return Class - The class of the Command
     * @throws LookupException
     */
    private Class lookup(String name) throws LookupException {
        Class cmdClass = null;
        try {
            cmdClass = getClass().getClassLoader()
                    .loadClass(getClassName(name));
        } catch (ClassNotFoundException e) {
        	// try once more from zimbra extension
        	try {
        		cmdClass = ExtensionUtil.loadClass(name, getClassName(name));
        	} catch (ClassNotFoundException ee) {
        		throw new LookupException("Command named '" + name + "' not found.");
        	}
        }
        if (!ExecutableCommand.class.isAssignableFrom(cmdClass))
            throw new LookupException("Class " + cmdClass.getName()
                    + " must implement " + ExecutableCommand.class.getName());
        return cmdClass;
    }

    /**
     * <p>
     * Method newInstance answers an instance of the class to which a Command
     * name is mapped.
     * </p>
     * 
     * @param name -
     *            The name of the Command
     * @return Class - The class of the Command
     * @throws LookupException
     */
    @Override
    public ExecutableCommand getCommand(String name) throws LookupException {
        try {
            return (ExecutableCommand) lookup(name).newInstance();
        } catch (InstantiationException e) {
            throw new LookupException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new LookupException(e.getMessage());
        }
    }

    /**
     * Method isSupported answers a boolean indicating if a Command name is
     * configured.
     * 
     * @param name -
     *            The Command name
     * @return boolean - True if the Command name is configured.
     */
    @Override
    public boolean isCommandSupported(String name) {
        boolean isSupported = false;
        try {
            lookup(name);
            isSupported = true;
        } catch (LookupException e) {
        }
        return isSupported;
    }

    /**
     * <p>
     * Method getClassName answers the name of the class to which a Command name
     * is mapped.
     * </p>
     * 
     * @param name -
     *            The name of the Command
     * @return String - The name of the class
     * @throws LookupException
     */
    protected String getClassName(String name) throws LookupException {
        final String className = classNameMap.get(name.toLowerCase());
        if (null == className)
            throw new LookupException("Command named '" + name
                    + "' not mapped.");
        return className;
    }

    /**
     * @see org.apache.jsieve.CommandManager#getExtensions()
     */
    @Override
    public List<String> getExtensions() {
        List<String> extensions = new ArrayList<String>(classNameMap.size());
        for (String key : classNameMap.keySet())
        {
            if (!isImplicitlyDeclared(key))
            {
                extensions.add(key);
            }
        }
        return extensions;
    }

}
