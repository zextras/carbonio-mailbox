package com.zimbra.cs.account;

import com.google.common.base.Strings;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Originally Version of commons. Moved here to avoid coupling and allow splitting modules.
 */
public class AttributeVersion implements Comparable<AttributeVersion> {

	public static final String FUTURE = "future";
	private static Pattern mPattern = Pattern.compile("([a-zA-Z]+)(\\d*)");

	private enum Release {
		BETA, M, RC, GA;

		public static Release fromString(String rel) throws AttributeManagerException {
			try {
				return Release.valueOf(rel);
			} catch (IllegalArgumentException e) {
				throw new AttributeManagerException("unknown release: " + rel, e);
			}
		}
	}

	private boolean mFuture;
	private int mMajor;
	private int mMinor;
	private int mPatch;
	private Release mRel;
	private int mRelNum;
	private String mVersion;

	public AttributeVersion(String version) throws AttributeManagerException {
		this(version, true);
	}

	public AttributeVersion(String version, boolean strict) throws AttributeManagerException {
		mVersion = version;
		if (FUTURE.equalsIgnoreCase(version)) {
			mFuture = true;
			return;
		}

		String ver = version;
		int underscoreAt = version.indexOf('_');
		int lastUnderscoreAt = version.lastIndexOf('_');
		if(lastUnderscoreAt == -1 || lastUnderscoreAt == underscoreAt)
			lastUnderscoreAt = version.length()-1;

		if (underscoreAt != -1) {
			ver = version.substring(0, underscoreAt);
			Matcher matcher = mPattern.matcher(version);
			if (matcher.find()) {
				mRel = Release.fromString(matcher.group(1));
				String relNum = matcher.group(2);
				if (!Strings.isNullOrEmpty(relNum))
					mRelNum = Integer.parseInt(relNum);
			}
		}

		String[] parts = ver.split("\\.");

		try {
			if (parts.length == 1)
				mMajor = Integer.parseInt(parts[0]);
			else if (parts.length == 2) {
				mMajor = Integer.parseInt(parts[0]);
				mMinor = Integer.parseInt(parts[1]);
			} else if (parts.length == 3) {
				mMajor = Integer.parseInt(parts[0]);
				mMinor = Integer.parseInt(parts[1]);
				mPatch = Integer.parseInt(parts[2]);
			} else if (parts.length == 4 && !strict) {
				// so we can parse version number in ZCO/ZCB UA,
				// where version number is 4 segments, like 7.0.0.0
				// NOTE: the last segment is ignored.
				mMajor = Integer.parseInt(parts[0]);
				mMinor = Integer.parseInt(parts[1]);
				mPatch = Integer.parseInt(parts[2]);
			} else {
				throw new AttributeManagerException("invalid version format:" + version);
			}
		} catch (NumberFormatException e) {
			throw new AttributeManagerException("invalid version format:" + version, e);
		}

	}

	/**
	 * Compares the two versions.
	 *
	 * e.g.
	 * <ul>
	 *  <li>{@code compare("5.0.10", "5.0.9") > 0}
	 *  <li>{@code compare("5.0.10", "5.0.10") == 0}
	 *  <li>{@code compare("5.0", "5.0.9") < 0}
	 *  <li>{@code compare("5.0.10_RC1", "5.0.10_BETA3") > 0}
	 *  <li>{@code compare("5.0.10_GA", "5.0.10_RC2") > 0}
	 *  <li>{@code compare("5.0.10", "5.0.10_RC2") > 0}
	 * </ul>
	 *
	 * @return a negative integer, zero, or a positive integer as
	 * versionX is older than, equal to, or newer than the versionY.
	 */
	public static int compare(String versionX, String versionY) throws AttributeManagerException {
		AttributeVersion x = new AttributeVersion(versionX);
		AttributeVersion y = new AttributeVersion(versionY);
		return x.compareTo(y);
	}

	/**
	 * Compares this object with the specified version.
	 *
	 * @param version
	 * @return a negative integer, zero, or a positive integer as this object is
	 * older than, equal to, or newer than the specified version.
	 */
	public int compare(String version) throws AttributeManagerException {
		AttributeVersion other = new AttributeVersion(version);
		return compareTo(other);
	}

	/**
	 * Compares this object with the specified version.
	 *
	 * @param version
	 * @return a negative integer, zero, or a positive integer as this object is
	 * older than, equal to, or newer than the specified version.
	 */
	@Override
	public int compareTo(AttributeVersion version) {
		if (mFuture) {
			if (version.mFuture)
				return 0;
			else
				return 1;
		} else if (version.mFuture)
			return -1;

		int r = mMajor - version.mMajor;
		if (r != 0)
			return r;

		r = mMinor - version.mMinor;
		if (r != 0)
			return r;

		r = mPatch - version.mPatch;
		if (r != 0)
			return r;

		if (mRel != null) {
			if (version.mRel != null) {
				r = mRel.ordinal() - version.mRel.ordinal();
				if (r != 0) {
					return r;
				}
				return mRelNum - version.mRelNum;
			} else { // no Release means GA
				return mRel.ordinal() - Release.GA.ordinal();
			}
		} else { // no Release means GA
			if (version.mRel != null) {
				return Release.GA.ordinal() - version.mRel.ordinal();
			} else {
				return 0;
			}
		}
	}

	public boolean isSameMinorRelease(AttributeVersion version) {
		return (this.mMajor == version.mMajor && this.mMinor == version.mMinor);
	}

	public boolean isLaterMajorMinorRelease(AttributeVersion version) {
		return (this.mMajor > version.mMajor || (this.mMajor == version.mMajor && this.mMinor > version.mMinor));
	}

	public boolean isFuture() {
		return mFuture;
	}

	@Override
	public String toString() {
		return mVersion;
	}

}
