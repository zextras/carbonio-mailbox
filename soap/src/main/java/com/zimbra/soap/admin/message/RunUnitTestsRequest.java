// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Runs the server-side unit test suite. <br>
 *     If <b>&lt;test></b>'s are specified, then run the requested tests (instead of the standard
 *     test suite). Otherwise the standard test suite is run.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_RUN_UNIT_TESTS_REQUEST)
public class RunUnitTestsRequest {

  /**
   * @zm-api-field-tag test
   * @zm-api-field-description Test names - each entry of form: className[#testName[+testName]*]
   *     <p>Example test names:
   *     <pre>
   * com.zimbra.qa.unittest.TestCalDav
   * com.zimbra.qa.unittest.TestUtilCode#testGzip
   * com.zimbra.qa.unittest.TestUtilCode#testGzip+testLruMap
   * </pre>
   */
  @XmlElement(name = AdminConstants.E_TEST, required = false)
  private final List<String> tests = Lists.newArrayList();

  public RunUnitTestsRequest() {}

  public void setTests(Iterable<String> tests) {
    this.tests.clear();
    if (tests != null) {
      Iterables.addAll(this.tests, tests);
    }
  }

  public RunUnitTestsRequest addTest(String test) {
    this.tests.add(test);
    return this;
  }

  public List<String> getTests() {
    return Collections.unmodifiableList(tests);
  }
}
