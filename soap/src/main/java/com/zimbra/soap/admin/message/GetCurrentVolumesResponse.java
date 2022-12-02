// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_CURRENT_VOLUMES_RESPONSE)
@XmlType(propOrder = {})
public final class GetCurrentVolumesResponse {

    /**
     * @zm-api-field-description Current volume information.  Entry for secondary message type (2) is optional
     */
    @XmlElement(name=AdminConstants.E_VOLUME, required=false)
    private final List<CurrentVolumeInfo> volumes = Lists.newArrayList();

    public void setVolumes(Collection<CurrentVolumeInfo> list) {
        volumes.clear();
        if (list != null) {
            volumes.addAll(list);
        }
    }

    public void addVolume(CurrentVolumeInfo volume) {
        volumes.add(volume);
    }

    public List<CurrentVolumeInfo> getVolumes() {
        return Collections.unmodifiableList(volumes);
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static final class CurrentVolumeInfo {
        @XmlAttribute(name=AdminConstants.A_ID, required=true)
        private short id;

        @XmlAttribute(name=AdminConstants.A_VOLUME_TYPE, required=true)
        private short type;

        /**
         * no-argument constructor wanted by JAXB
         */
         @SuppressWarnings("unused")
        private CurrentVolumeInfo() {
            this((short) -1, (short) 0);
        }

        public CurrentVolumeInfo(short id, short type) {
            this.id = id;
            this.type = type;
        }

        public short getId() {
            return id;
        }

        public short getType() {
            return type;
        }

    }

}
