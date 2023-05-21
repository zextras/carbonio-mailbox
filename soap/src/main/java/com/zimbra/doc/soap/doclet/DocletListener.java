// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Tag;

public abstract class DocletListener {

    private String className = null;

    public DocletListener(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    /**
     * Called when a registered class is found.
     */
    public abstract void classEvent(ClassDoc classDoc);

    /**
     * Gets the tag text for a given tag.
     *
     * @param    tags an array of tags
     * @param    tag  the tag
     * @return   the tag text or <code>null</code> if tag does not exist
     */
    protected static String getTagText(Tag[] tags, String tag) {
        if (tags.length > 0) {
            for (int k=0; k < tags.length; k++) {
                if (tags[k].name().equalsIgnoreCase(tag)) {
                    return tags[k].text();
                }
            }
        }
        return null;
    }

    /**
     * Dumps the tags to <code>System.out</code>.
     *
     * @param tags an array of tags
     */
    protected static void dumpTags(Tag[] tags) {
        System.out.println("Dumping tags...");
        System.out.printf("tags.length = %d%n", tags.length);
        if (tags.length > 0) {
            for (int k=0; k < tags.length; k++) {
                System.out.printf("tags[%d].name = %s%n", k, tags[k].name());
            }
        }
    }
}
