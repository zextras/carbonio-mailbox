// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class DomUtil {
	public static byte[] getBytes(Document doc) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writeDocumentToStream(doc, baos);
		byte[] msg = baos.toByteArray();
		return msg;
	}
	public static void writeDocumentToStream(Document doc, OutputStream out) throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setTrimText(false);
		format.setOmitEncoding(false);
		XMLWriter writer = new XMLWriter(out, format);
		writer.write(doc);
	}
}
