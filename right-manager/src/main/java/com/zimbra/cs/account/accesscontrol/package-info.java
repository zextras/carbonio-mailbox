// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/**
 * This package-info.java file is required to provide JAXB XML schema annotations for the
 * com.zimbra.cs.account.accesscontrol package, which was moved to the right-manager module
 * in commit d121b47b1e (refactor: move RightManager and attrs.xml files).
 * <p>
 * The accesscontrol package contains the TargetType enum with a nested SoapTargetType enum
 * (@XmlEnum) that is referenced by SOAP admin message types (e.g., EffectiveRightsTargetInfo).
 * When JAXB generates XSD schemas for SOAP messages, it needs to know the XML namespace for
 * all referenced types. Without this annotation, JAXB could not properly serialize/deserialize
 * the TargetType enum values in SOAP responses, breaking WSDL schema generation.
 * <p>
 * This file declares the package's XML namespace as "urn:zimbra" - the same namespace used
 * by other base type and helper packages (soap.type, soap.base, soap.header). This allows
 * JAXB to properly include the SoapTargetType enum definition in the generated XSD schemas
 * and prevents the creation of broken schema references (like the missing schema5.xsd) that
 * would prevent SOAP clients from consuming the Admin API.
 * <p>
 * TECHNICAL NOTES:
 * - The namespace choice of "urn:zimbra" is correct because SoapTargetType is a helper enum
 *   for SOAP operations, not a top-level SOAP message class
 * - Using the shared "urn:zimbra" namespace merges this with other base types in the same
 *   XSD file, avoiding unnecessary additional schema files and preventing numbering issues
 *   in the WSDL generation process
 * - Without elementFormDefault = QUALIFIED, JAXB would not properly qualify the enum values
 * - XmlAccessType.NONE requires explicit annotations on all XML-visible members
 */

@XmlSchema(
    xmlns = {
        @XmlNs(prefix="zimbra", namespaceURI = "urn:zimbra")
    },
    namespace = "urn:zimbra",
    elementFormDefault = XmlNsForm.QUALIFIED
)
@XmlAccessorType(XmlAccessType.NONE)

package com.zimbra.cs.account.accesscontrol;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
