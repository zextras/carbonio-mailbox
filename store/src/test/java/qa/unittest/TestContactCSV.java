// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import com.zimbra.cs.service.formatter.ContactCSV;

/**
 * Unit test for {@link ContactCSV}.
 * Note that ContactCSV import requires /opt/zextras/conf/contact-fields.xml 
 *
 */
public final class TestContactCSV
extends TestCase {

    /**
     * Ensure that we autodetect the use of ";" as a separator when the fields are quoted.
     * Also ensure that a comma inside the quotes for the first field doesn't confuse things
     */
    public void testSemicolonSepWithQuotes() throws Exception {
        String hdr = "\"Junk with, comma\";\"Title\";\"First Name\";\"Last Name\";\"E-mail Address\";\"User 1\"";
        String line1 = ";;\"Di\";\"Burns\";\"di@example.test\";\"Misc comment\"";
        String line2 = ";;\"Su\";\"James\";\"su@example.test\";\"Street musician\"";
        StringReader reader = new StringReader(String.format("%s\n%s\n%s\n", hdr, line1, line2));
        List<Map<String, String>> contacts = ContactCSV.getContacts(new BufferedReader(reader), null);
        assertNotNull("getContacts return null", contacts);
        assertEquals("getContacts return list length", 2, contacts.size());
        Map<String, String> firstContact = contacts.get(0);
        if (4 != firstContact.size()) {
            for (Entry<String, String> entry : firstContact.entrySet()) {
                System.err.println(String.format("Key=%s Value=%s", entry.getKey(), entry.getValue()));
            }
        }
        assertEquals("getContacts returned first contact map size", 4, firstContact.size());
    }

    /**
     * Ensure that we autodetect the use of ";" as a separator when the header fields are not quoted.
     */
    public void testSemicolonSepNoQuotes() throws Exception {
        String hdr = "Voornaam;Achternaam;Naam;E-mailadres";
        String line1 = "Fred;Bloggs;Frederick Bloggs;fred@example.test";
        String line2 = "Blaise;Pascal;B. Pascal;blaise.pascal@example.test";
        StringReader reader = new StringReader(String.format("%s\n%s\n%s\n", hdr, line1, line2));
        List<Map<String, String>> contacts = ContactCSV.getContacts(new BufferedReader(reader), null);
        assertNotNull("getContacts return null", contacts);
        assertEquals("getContacts return list length", 2, contacts.size());
        Map<String, String> firstContact = contacts.get(0);
        if (3 != firstContact.size()) {
            for (Entry<String, String> entry : firstContact.entrySet()) {
                System.err.println(String.format("Key=%s Value=%s", entry.getKey(), entry.getValue()));
            }
        }
        assertEquals("getContacts returned first contact map size", 3, firstContact.size());
    }

    public void tearDown()
    throws Exception {
        cleanUp();
    }

    private void cleanUp()
    throws Exception {
    }

    public static void main(String[] args)
    throws Exception {
        // TestUtil.cliSetup();
        TestUtil.runTest(TestContactCSV.class);
    }
}
