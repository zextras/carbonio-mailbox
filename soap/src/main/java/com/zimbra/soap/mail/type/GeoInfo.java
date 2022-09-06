// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.calendar.Geo;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.GeoInfoInterface;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name = GqlConstants.CLASS_GEO_INFORMATION, description = "Geo information")
public class GeoInfo implements GeoInfoInterface {

  /**
   * @zm-api-field-tag longitude
   * @zm-api-field-description Longitude (float value)
   */
  @XmlAttribute(name = MailConstants.A_CAL_GEO_LATITUDE /* lat */, required = false)
  @GraphQLQuery(name = GqlConstants.LATITUDE, description = "Latitude (float value)")
  private final String latitude;

  /**
   * @zm-api-field-tag longitude
   * @zm-api-field-description Longitude (float value)
   */
  @XmlAttribute(name = MailConstants.A_CAL_GEO_LONGITUDE /* lon */, required = false)
  @GraphQLQuery(name = GqlConstants.LONGITUDE, description = "Longitude (float value)")
  private final String longitude;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GeoInfo() {
    this((String) null, (String) null);
  }

  public GeoInfo(
      @GraphQLInputField(name = GqlConstants.LATITUDE) String latitude,
      @GraphQLInputField(name = GqlConstants.LONGITUDE) String longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public GeoInfo(Geo geo) {
    this.latitude = geo.getLatitude();
    this.longitude = geo.getLongitude();
  }

  @Override
  public GeoInfoInterface create(String latitude, String longitude) {
    return new GeoInfo(latitude, longitude);
  }

  @Override
  public GeoInfoInterface create(Geo geo) {
    return new GeoInfo(geo);
  }

  @Override
  public String getLatitude() {
    return latitude;
  }

  @Override
  public String getLongitude() {
    return longitude;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("latitude", latitude)
        .add("longitude", longitude)
        .toString();
  }
}
