// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.VolumeInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ALL_VOLUMES_RESPONSE)
public final class GetAllVolumesResponse {

  /**
   * @zm-api-field-description Information about volumes
   */
  @XmlElement(name = AdminConstants.E_VOLUME, required = true)
  private final List<VolumeInfo> volumes = Lists.newArrayList();

  public void setVolumes(Collection<VolumeInfo> list) {
    volumes.clear();
    if (list != null) {
      volumes.addAll(list);
    }
  }

  public void addVolume(VolumeInfo volume) {
    volumes.add(volume);
  }

  public List<VolumeInfo> getVolumes() {
    return Collections.unmodifiableList(volumes);
  }
}
