package com.zextras.mailbox.midlewarepojo;

import java.util.List;

/**
 * DTO class to transfer data from WEB layer to service layer.
 *
 * @author Yuliya Aheeva, Dima Dymkovets
 * @since 23.10.0
 */
public class UpdateInput {
  private String internalGrantExpiryString;
  private String guestGrantExpiryString;
  private List<GrantInput> grantInputList;
  private String newName;
  private String flags;
  private byte color;
  private String view;

  public UpdateInput(
      final String internalGrantExpiryString,
      final String guestGrantExpiryString,
      final List<GrantInput> grantInputList,
      final String newName,
      final String flags,
      final byte color,
      final String view) {
    this.internalGrantExpiryString = internalGrantExpiryString;
    this.guestGrantExpiryString = guestGrantExpiryString;
    this.grantInputList = grantInputList;
    this.newName = newName;
    this.flags = flags;
    this.color = color;
    this.view = view;
  }

  public String getInternalGrantExpiryString() {
    return internalGrantExpiryString;
  }

  public void setInternalGrantExpiryString(final String internalGrantExpiryString) {
    this.internalGrantExpiryString = internalGrantExpiryString;
  }

  public String getGuestGrantExpiryString() {
    return guestGrantExpiryString;
  }

  public void setGuestGrantExpiryString(final String guestGrantExpiryString) {
    this.guestGrantExpiryString = guestGrantExpiryString;
  }

  public List<GrantInput> getGrantInputList() {
    return grantInputList;
  }

  public void setGrantInputList(final List<GrantInput> grantInputList) {
    this.grantInputList = grantInputList;
  }

  public String getNewName() {
    return newName;
  }

  public void setNewName(final String newName) {
    this.newName = newName;
  }

  public String getFlags() {
    return flags;
  }

  public void setFlags(final String flags) {
    this.flags = flags;
  }

  public byte getColor() {
    return color;
  }

  public void setColor(final byte color) {
    this.color = color;
  }

  public String getView() {
    return view;
  }

  public void setView(final String view) {
    this.view = view;
  }

  /**
   * Builder class to build {@link GrantInput}.
   *
   * @author Yuliya Aheeva, Dima Dymkovets
   * @since 23.10.0
   */
  public static final class Builder {
    private String internalGrantExpiryString;
    private String guestGrantExpiryString;
    private List<GrantInput> grantInputList;
    private String newName;
    private String flags;
    private byte color;
    private String view;

    public Builder setInternalGrantExpiryString(final String internalGrantExpiryString) {
      this.internalGrantExpiryString = internalGrantExpiryString;
      return this;
    }

    public Builder setGuestGrantExpiryString(final String guestGrantExpiryString) {
      this.guestGrantExpiryString = guestGrantExpiryString;
      return this;
    }

    public Builder setGrantInputList(final List<GrantInput> grantInputList) {
      this.grantInputList = grantInputList;
      return this;
    }

    public Builder setNewName(final String newName) {
      this.newName = newName;
      return this;
    }

    public Builder setFlags(final String flags) {
      this.flags = flags;
      return this;
    }

    public Builder setColor(final byte color) {
      this.color = color;
      return this;
    }

    public Builder setView(final String view) {
      this.view = view;
      return this;
    }

    public UpdateInput build() {
      return new UpdateInput(
          internalGrantExpiryString,
          guestGrantExpiryString,
          grantInputList,
          newName,
          flags,
          color,
          view);
    }
  }
}
