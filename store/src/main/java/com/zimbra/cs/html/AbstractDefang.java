// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Utility class that implements many of the more mundane methods.
 * @author jpowers
 *
 */
public abstract class AbstractDefang implements BrowserDefang {

    public String defang(String html, boolean neuterImages) throws IOException {
        return defang(new StringReader(html), neuterImages);
    }
    
    public String defang(InputStream html, boolean neuterImages)
    throws IOException {
        StringWriter writer = new StringWriter();
        defang(html, neuterImages, writer);
        return writer.toString(); 
    }
    
    
    public String defang(Reader htmlReader, boolean neuterImages)
    throws IOException {
        StringWriter writer = new StringWriter();
        defang(htmlReader, neuterImages, writer);
        return writer.toString();
    }
    
    
    
}
