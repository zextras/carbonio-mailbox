// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/** Test JAXB class with an XmlElement list of enums */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="wrapper-absent-if-empty")
public class WrapperAbsentIfEmpty {
    @XmlTransient
    private final List<Integer> numbers = Lists.newArrayList();

    public WrapperAbsentIfEmpty() { }

    @XmlElementWrapper(name = "numbers", required=false)
    @XmlElement(name = "number", required=false)
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
            Iterables.addAll(this.numbers,entries);
        }
    }

    public void addNumber(Integer number) {
        this.numbers.add(number);
    }
}
