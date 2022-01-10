// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail;

import java.util.EnumSet;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.zimbra.soap.mail.type.Acl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.soap.mail.message.GetFolderResponse;
import com.zimbra.soap.mail.type.Folder;
import com.zimbra.soap.mail.type.Grant;
import com.zimbra.soap.type.GrantGranteeType;
import com.zimbra.soap.mail.type.ItemType;
import com.zimbra.soap.mail.type.SearchFolder;

/**
 * Unit test for {@link GetFolderRequest}.
 */
public final class GetFolderTest {

    private static Unmarshaller unmarshaller;

    @BeforeClass
    public static void init() throws Exception {
        JAXBContext jaxb = JAXBContext.newInstance(GetFolderResponse.class);
        unmarshaller = jaxb.createUnmarshaller();
    }

    /**
     * Motivated by Bug 55153 failure in ZGrant.java line 134:
     *      mGranteeType = GranteeType.fromString(grant.getGranteeType().toString());
     */
    @Test
    public void unmarshallGetFolderResponseContainingGrant() throws Exception {
        GetFolderResponse result = (GetFolderResponse) unmarshaller.unmarshal(
                getClass().getResourceAsStream("GetFolderResponseWithGrant.xml"));
        Folder top = result.getFolder();
        boolean foundGrant = false;
        for (Folder child : top.getSubfolders()) {
            Acl acl = child.getAcl();
            if (acl != null) {
                List<Grant> myGrants = acl.getGrants();
                if (myGrants.size() > 0) {
                    foundGrant = true;
                    Grant first = myGrants.get(0);
                    GrantGranteeType mGranteeType = GrantGranteeType.fromString(
                            first.getGranteeType().toString());
                    Assert.assertEquals(GrantGranteeType.usr, mGranteeType);
                }
            }
        }
        Assert.assertTrue("Should have processed a valid <grant>", foundGrant);
        result = (GetFolderResponse) unmarshaller.unmarshal(
                getClass().getResourceAsStream("GetFolderResponseWithBadGrant.xml"));
        top = result.getFolder();
        foundGrant = false;
        for (Folder child : top.getSubfolders()) {
            Acl acl = child.getAcl();
            if (acl != null) {
                List <Grant> myGrants = acl.getGrants();
                if (myGrants.size() > 0) {
                    foundGrant = true;
                    Grant first = myGrants.get(0);
                    GrantGranteeType mGranteeType = first.getGranteeType();
                    Assert.assertNull("There was no 'gt' attribute", mGranteeType);
                }
            }
        }
        Assert.assertTrue("Should have processed a bad <grant>", foundGrant);
    }

    @Test
    public void unmarshallSearchFolderEmptyTypes() throws Exception {
        GetFolderResponse resp = (GetFolderResponse) unmarshaller.unmarshal(
                getClass().getResourceAsStream("GetFolderResponse-SearchFolderEmptyTypes.xml"));
        for (Folder folder : resp.getFolder().getSubfolders()) {
            if ("searchfolder-with-types".equals(folder.getName())) {
                Assert.assertEquals(EnumSet.of(ItemType.CONVERSATION, ItemType.DOCUMENT),
                        ((SearchFolder) folder).getTypes());
            } else if ("searchfolder-with-empty-types".equals(folder.getName())) {
                Assert.assertEquals(EnumSet.noneOf(ItemType.class), ((SearchFolder) folder).getTypes());
            }
        }

    }
}
