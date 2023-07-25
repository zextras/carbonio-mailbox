// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.zimbra.common.localconfig.DebugConfig;

/**
 * A basic xhtml handler that deletes unfriendly tags and attributes which are not whitelisted.
 * 
 * The functionality is based mostly off the DefangFilter that's used for html.
 * 
 * There was also a little influence from here:
 * http://download.oracle.com/javaee/1.4/tutorial/doc/JAXPSAX3.html
 * 
 * 
 * @author jpowers
 *
 */
public class XHtmlDocumentHandler extends DefaultHandler {
    /**
     * The list of tags that should always be allowed
     */
    private static Set<String> allowTags = new HashSet<String>(
        Arrays.asList(DebugConfig.xhtmlWhitelistedTags.split(",")));
    /**
     * The list of attributes that should always be allowed
     */
    private static Set<String> allowAttributes = new HashSet<String>(
        Arrays.asList(DebugConfig.xhtmlWhitelistedAttributes.split(",")));

    /*
     * The writer that we'll write the sanitzed output to
     */
    private Writer out;

    /**
     * A stack that keeps track of the elements that
     * have been removed. It lets us keep track of what tags
     * need their end tag removed.
     */
    private Stack<String> removedElements = new Stack<String>();

    /**
     * This buffer keeps track of the text between tags
     */
    private StringBuffer textBuffer = new StringBuffer();

    /***
     * Creates a new handler that writes
     * @param out
     */
    public XHtmlDocumentHandler(Writer out){
        this.out = out;
    }

    @Override
    public void characters(char[] buf, int start, int len) throws SAXException {
        // if we're removing tags, remove all of the text between them as well.
        if(!removedElements.isEmpty()){
            // TODO check to see if we can't allow some text, but in reality it probably isn't needed
            return;
        }
        textBuffer.append(new String(buf, start, len));
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        try {
            out.append("<?").append(target).append(" ").append(data).append("?>");
        } catch (IOException e) {
           throw new SAXException(e);
        }
    }

    @Override
    public void startDocument() throws SAXException {
         try {
            out.write("<?xml version='1.0' encoding='UTF-8'?>\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            out.flush();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void startElement(String namespaceURI,
            String sName, // simple name
            String qName, // qualified name
            Attributes attrs) throws SAXException {

        String eName = "".equals(sName)? qName: sName; // element name

        // check to see if we're removing this tag
        if(!allowTags.contains(eName.toLowerCase())) {
            removedElements.push(eName.toLowerCase());
            return;
        }
        try {
            // output any text that might need outputing
            if(textBuffer.length() > 0)
            {
                out.append(textBuffer);
                textBuffer = new StringBuffer();
            }
            out.append("<");
            out.append(eName);
            if (attrs != null) {
              for (int i = 0; i < attrs.getLength(); i++) {
                String aName = "".equals(attrs.getLocalName(i))? attrs.getQName(i) : attrs.getLocalName(i); // Attr name
                if(!allowAttributes.contains(aName.toLowerCase())) {
                    // just skip this attribute
                    continue;
                }
                out.append(" ");
                out.append(aName).append("=\"").append(attrs.getValue(i)).append("\"");
              }
            }
            out.append(">");
        }
        catch(IOException e) {
            throw new SAXException(e);
        }
    }
    @Override
    public void endElement(String namespaceURI,
            String sName, // simple name
            String qName)  // qualified name
            throws SAXException {
        // first, check to see if this element is one of the ones that we are removing
        String eName = "".equals(sName)? qName: sName; // element name

        if(!removedElements.isEmpty() && removedElements.peek().equals(eName.toLowerCase())) {
            // this is one of the tags we're getting rid of, pop and return
            removedElements.pop();
            return;
        }

        try {
            // output any text that might need outputting
            if(textBuffer.length() > 0)
            {
                out.append(textBuffer);
                textBuffer = new StringBuffer();
            }
            out.append("</").append(eName).append(">");
        } catch (IOException e) {
            throw new SAXException(e);
        }

    }

}
