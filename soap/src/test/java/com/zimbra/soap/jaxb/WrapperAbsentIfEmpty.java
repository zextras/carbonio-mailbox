// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/** Test JAXB class with an XmlElement list of enums */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "wrapper-absent-if-empty")
public class WrapperAbsentIfEmpty {
  @XmlTransient private final List<Integer> numbers = Lists.newArrayList();

  public WrapperAbsentIfEmpty() {}

  @XmlElementWrapper(name = "numbers", required = false)
  @XmlElement(name = "number", required = false)
  public List<Integer> getNumbers() {
    if (numbers.isEmpty()) {
      return null;
    } else {
      return numbers;
    }
  }

  public void setNumbers(List<Integer> entries) {
    this.numbers.clear();
    if (entries != null) {
      Iterables.addAll(this.numbers, entries);
    }
  }

  public void addNumber(Integer number) {
    this.numbers.add(number);
  }
}
