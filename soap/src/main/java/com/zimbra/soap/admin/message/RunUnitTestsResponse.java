// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.TestResultInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_RUN_UNIT_TESTS_RESPONSE)
public class RunUnitTestsResponse {

  /**
   * @zm-api-field-description Information about test results
   */
  @XmlElement(name = "results", required = true)
  private final TestResultInfo results;

  /**
   * @zm-api-field-tag num-executed
   * @zm-api-field-description Number of executed tests
   */
  @XmlAttribute(name = AdminConstants.A_NUM_EXECUTED, required = true)
  private final int numExecuted;

  /**
   * @zm-api-field-tag num-failed
   * @zm-api-field-description Number of failed tests
   */
  @XmlAttribute(name = AdminConstants.A_NUM_FAILED, required = true)
  private final int numFailed;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RunUnitTestsResponse() {
    this((TestResultInfo) null, -1, -1);
  }

  public RunUnitTestsResponse(TestResultInfo results, int numExecuted, int numFailed) {
    this.results = results;
    this.numExecuted = numExecuted;
    this.numFailed = numFailed;
  }

  public TestResultInfo getResults() {
    return results;
  }

  public int getNumExecuted() {
    return numExecuted;
  }

  public int getNumFailed() {
    return numFailed;
  }
}
