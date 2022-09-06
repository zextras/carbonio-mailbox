// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"completedTests", "failedTests"})
public class TestResultInfo {

  /**
   * @zm-api-field-description Information for completed tests
   */
  @XmlElement(name = "completed", required = false)
  private List<CompletedTestInfo> completedTests = Lists.newArrayList();

  /**
   * @zm-api-field-description Information for failed tests
   */
  @XmlElement(name = "failure", required = false)
  private List<FailedTestInfo> failedTests = Lists.newArrayList();

  public TestResultInfo() {}

  public void setCompletedTests(Iterable<CompletedTestInfo> completedTests) {
    this.completedTests.clear();
    if (completedTests != null) {
      Iterables.addAll(this.completedTests, completedTests);
    }
  }

  public TestResultInfo addCompletedTest(CompletedTestInfo completedTest) {
    this.completedTests.add(completedTest);
    return this;
  }

  public void setFailedTests(Iterable<FailedTestInfo> failedTests) {
    this.failedTests.clear();
    if (failedTests != null) {
      Iterables.addAll(this.failedTests, failedTests);
    }
  }

  public TestResultInfo addFailedTest(FailedTestInfo failedTest) {
    this.failedTests.add(failedTest);
    return this;
  }

  public List<CompletedTestInfo> getCompletedTests() {
    return Collections.unmodifiableList(completedTests);
  }

  public List<FailedTestInfo> getFailedTests() {
    return Collections.unmodifiableList(failedTests);
  }
}
