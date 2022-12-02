// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;
import com.zimbra.soap.admin.type.CheckedRight;
import com.zimbra.soap.admin.type.EffectiveRightsTargetSelector;
import com.zimbra.soap.admin.type.GranteeSelector;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Check if a principal has the specified right on target.
 * <br />
 * A successful return means the principal specified by the <b>&lt;grantee></b> is allowed for the specified right on
 * the target object.
 * <br />
 * If PERM_DENIED is thrown, it means the authed user does not have privilege to run this SOAP command (has to be an
 * admin because this command is in admin namespace).
 * <br />
 * Result of CheckRightRequest is in the allow="1|0" attribute in CheckRightResponse.
 * If a specific grant decisively lead to the result, details of it are specified in <b>&lt;via></b> in the
 * <b>&lt;CheckRightResponse></b>.
 * <br />
 * <br />
 * e.g. if a combo right C containing renameAccount is granted to group G on domain D, and admin A is in group G, then:
 * <pre>
 *            &lt;CheckRightRequest>
 *               &lt;target type="account"> by="name">user1@D&lt;/target>
 *               &lt;grantee by="name">admin@D&lt;/grantee>
 *               &lt;right>renameAccount&lt;/right>
 *            &lt;/CheckRightRequest>
 * </pre>
 * will return:
 * <pre>
 *            &lt;CheckRightResponse allow="1">
 *               &lt;via>
 *                 &lt;target type=domain>D&lt;/target>
 *                 &lt;grantee type=grp>G&lt;/grantee>
 *                 &lt;right>C&lt;/right>
 *               &lt;/via>
 *            &lt;/CheckRightResponse>
 * </pre>
 * <br />
 * Note, <b>&lt;via></b> is optional.  If the right of interest is not granted at all, there will be no
 *       <b>&lt;via></b> in the response.     Also, <b>&lt;via></b> will probably be hairy for rights that modify/get
 *       selective attrs, it may not be returned for those rights.  TDB...
 * <br />
 * e.g.
 * <pre>
 *       &lt;CheckRightRequest>
 *           &lt;target type="account"> by="name">user1@D&lt;/target>
 *           &lt;grantee by="name">admin@D&lt;/grantee>
 *           &lt;right>configureQuota&lt;/right>
 *           &lt;attrs>
 *               &lt;a n="zimbraMailQuota">100000&lt;/a>
 *               &lt;a n="zimbraQuotaWarnPercent">80&lt;/a>
 *           &lt;attrs>
 *       &lt;/CheckRightRequest>
 *
 *       &lt;CheckRightResponse allow="0">
 * </pre>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CHECK_RIGHT_REQUEST)
public class CheckRightRequest extends AdminAttrsImpl {

    /**
     * @zm-api-field-description Target
     */
    @XmlElement(name=AdminConstants.E_TARGET /* target */, required=true)
    private final EffectiveRightsTargetSelector target;

    /**
     * @zm-api-field-description Grantee - valid values for type are "usr" and "email"
     */
    @XmlElement(name=AdminConstants.E_GRANTEE /* grantee */, required=true)
    private final GranteeSelector grantee;

    /**
     * @zm-api-field-description Checked Right
     */
    @XmlElement(name=AdminConstants.E_RIGHT /* right */, required=true)
    private final CheckedRight right;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CheckRightRequest() {
        this((EffectiveRightsTargetSelector) null,
                (GranteeSelector) null, (CheckedRight) null);
    }

    public CheckRightRequest(EffectiveRightsTargetSelector target,
            GranteeSelector grantee, CheckedRight right) {
        this.target = target;
        this.grantee = grantee;
        this.right = right;
    }

    public EffectiveRightsTargetSelector getTarget() { return target; }
    public GranteeSelector getGrantee() { return grantee; }
    public CheckedRight getRight() { return right; }
}
