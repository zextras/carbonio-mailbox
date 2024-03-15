// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.XmlParseException;
import com.zimbra.common.util.DateUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.type.Policy;
import com.zimbra.soap.mail.type.RetentionPolicy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RetentionPolicyManager {

  private static String FN_PURGE = "purge";
  private static String FN_ID = "id";
  private static String FN_NAME = "name";
  private static String FN_LIFETIME = "lifetime";

  private static class SystemPolicy {
    Map<String, Policy> purge = Maps.newHashMap();
  }

  private static RetentionPolicyManager instance = new RetentionPolicyManager();
  private static String SYSTEM_POLICY_KEY =
      RetentionPolicyManager.class.getSimpleName() + ".SYSTEM_POLICY";

  public static RetentionPolicyManager getInstance() {
    return instance;
  }

  private SystemPolicy getCachedSystemPolicy(Entry entry) throws ServiceException {
    SystemPolicy systemPolicy;
    synchronized (entry) {
      systemPolicy = (SystemPolicy) entry.getCachedData(SYSTEM_POLICY_KEY);

      if (systemPolicy == null) {
        String xml;
        if (entry instanceof Config) {
          xml = ((Config) entry).getMailPurgeSystemPolicy();
        } else if (entry instanceof Cos) {
          xml = ((Cos) entry).getMailPurgeSystemPolicy();
        } else {
          throw ServiceException.UNSUPPORTED();
        }

        systemPolicy = new SystemPolicy();
        if (!Strings.isNullOrEmpty(xml)) {
          ZimbraLog.purge.debug("Parsing system retention policy:\n%s", xml);
          try {
            Element el = Element.parseXML(xml);
            RetentionPolicy retentionPolicy = JaxbUtil.elementToJaxb(el, RetentionPolicy.class);
            for (Policy policy : retentionPolicy.getPurgePolicy()) {
              assert (policy.getId() != null);
              systemPolicy.purge.put(policy.getId(), policy);
            }
          } catch (XmlParseException e) {
            throw ServiceException.FAILURE("Unable to parse system retention policy.", e);
          }
        }
        entry.setCachedData(SYSTEM_POLICY_KEY, systemPolicy);
      }
    }
    return systemPolicy;
  }

  public Policy createSystemPurgePolicy(Entry entry, String name, String lifetime)
      throws ServiceException {
    validateLifetime(lifetime);
    Policy policy = Policy.newSystemPolicy(generateId(), name, lifetime);
    synchronized (entry) {
      SystemPolicy systemPolicy = getCachedSystemPolicy(entry);
      systemPolicy.purge.put(policy.getId(), policy);
      saveSystemPolicy(entry, new RetentionPolicy(systemPolicy.purge.values()));
    }
    return policy;
  }

  private static void validateLifetime(String lifetime) throws ServiceException {
    if (Strings.isNullOrEmpty(lifetime)) {
      throw ServiceException.INVALID_REQUEST("lifetime not specified", null);
    }
    long lifetimeInMillis = DateUtil.getTimeInterval(lifetime, -1);
    if (lifetimeInMillis == -1) {
      throw ServiceException.INVALID_REQUEST("Invalid lifetime value: " + lifetime, null);
    }
  }

  /**
   * Updates the properties of the system policy with the given id.
   *
   * @return {@code null} if a {@code Policy} with the given id could not be found
   */
  public Policy modifySystemPolicy(Entry entry, String id, String name, String lifetime)
      throws ServiceException {
    validateLifetime(lifetime);
    synchronized (entry) {
      SystemPolicy systemPolicy = getCachedSystemPolicy(entry);

      if (systemPolicy.purge.containsKey(id)) {
        Policy policy = Policy.newSystemPolicy(id, name, lifetime);
        systemPolicy.purge.put(id, policy);
        saveSystemPolicy(entry, new RetentionPolicy(systemPolicy.purge.values()));
        return policy;
      }
    }
    return null;
  }

  /**
   * Deletes the system policy with the given id.
   *
   * @return {@code true} if the policy was successfully deleted, {@code false} if no policy exists
   *     with the given id
   */
  public boolean deleteSystemPolicy(Entry entry, String id) throws ServiceException {
    synchronized (entry) {
      SystemPolicy systemPolicy = getCachedSystemPolicy(entry);
      Policy policy = systemPolicy.purge.remove(id);
      if (policy != null) {
        saveSystemPolicy(entry, new RetentionPolicy(systemPolicy.purge.values()));
        return true;
      }
    }
    return false;
  }

  private void saveSystemPolicy(Entry entry, RetentionPolicy rp) throws ServiceException {
    String xml = JaxbUtil.jaxbToElement(rp, XMLElement.mFactory).prettyPrint();
    if (entry instanceof Config) {
      ((Config) entry).setMailPurgeSystemPolicy(xml);
    } else if (entry instanceof Cos) {
      ((Cos) entry).setMailPurgeSystemPolicy(xml);
    }
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }

  public RetentionPolicy getSystemRetentionPolicy(Entry entry) throws ServiceException {
    SystemPolicy systemPolicy = getCachedSystemPolicy(entry);
    return new RetentionPolicy(systemPolicy.purge.values());
  }

  public RetentionPolicy getSystemRetentionPolicy(Account acct) throws ServiceException {
    RetentionPolicy retentionPolicy = null;
    Cos cos = acct.getCOS();
    if (cos != null) {
      retentionPolicy = RetentionPolicyManager.getInstance().getSystemRetentionPolicy(cos);
    }
    if (retentionPolicy == null || !retentionPolicy.isSet()) {
      Config config = Provisioning.getInstance().getConfig();
      retentionPolicy = RetentionPolicyManager.getInstance().getSystemRetentionPolicy(config);
    }
    return retentionPolicy;
  }

  /**
   * Returns a new {@code RetentionPolicy} that has the latest system policy data for any elements
   * in {@code rp} of type {@link Policy.Type#SYSTEM}.
   */
  private RetentionPolicy getCompleteRetentionPolicy(RetentionPolicy master, RetentionPolicy rp) {
    return new RetentionPolicy(getLatestList(master, rp.getPurgePolicy()));
  }

  public RetentionPolicy getCompleteRetentionPolicy(Account acct, RetentionPolicy rp)
      throws ServiceException {
    // Check CoS first, if not found get from Config
    RetentionPolicy retentionPolicy =
        RetentionPolicyManager.getInstance().getSystemRetentionPolicy(acct);
    retentionPolicy =
        RetentionPolicyManager.getInstance().getCompleteRetentionPolicy(retentionPolicy, rp);
    return retentionPolicy;
  }

  private List<Policy> getLatestList(RetentionPolicy master, Iterable<Policy> list) {
    List<Policy> latestList = Lists.newArrayList();
    for (Policy policy : list) {
      if (policy.getType() == Policy.Type.USER) {
        latestList.add(policy);
      } else {
        Policy latest = master.getPolicyById(policy.getId());
        if (latest != null) {
          latestList.add(latest);
        }
      }
    }
    return latestList;
  }

  public Policy getPolicyById(Entry entry, String id) throws ServiceException {
    SystemPolicy systemPolicy = getCachedSystemPolicy(entry);
    return systemPolicy.purge.get(id);
  }

  /**
   * Persists retention policy to {@code Metadata}.
   *
   * @param rp retention policy data
   * @param forMailbox {@code true} if this is mailbox retention policy, {@code false} if this is
   *     system retention policy. For mailbox policy, only the id is persisted.
   * @return
   */
  public static Metadata toMetadata(RetentionPolicy rp, boolean forMailbox) {
    MetadataList purge = new MetadataList();

    for (Policy policy : rp.getPurgePolicy()) {
      purge.add(toMetadata(policy, forMailbox));
    }

    Metadata metadata = new Metadata();
    metadata.put(FN_PURGE, purge);
    return metadata;
  }

  /**
   * Persists retention policy to {@code Metadata}.
   *
   * @param policy retention policy data
   * @param forMailbox {@code true} if this is mailbox retention policy, {@code false} if this is
   *     system retention policy. For mailbox policy, only the id is persisted.
   * @return
   */
  public static Metadata toMetadata(Policy policy, boolean forMailbox) {
    Metadata metadata = new Metadata();
    if (policy.getType() == Policy.Type.USER) {
      metadata.put(FN_LIFETIME, policy.getLifetime());
    } else {
      metadata.put(FN_ID, policy.getId());
      if (!forMailbox) {
        metadata.put(FN_NAME, policy.getName());
        metadata.put(FN_LIFETIME, policy.getLifetime());
      }
    }
    return metadata;
  }

  public static RetentionPolicy retentionPolicyFromMetadata(Metadata metadata, boolean forMailbox)
      throws ServiceException {
    if (metadata == null) {
      return new RetentionPolicy();
    }

    List<Policy> purge = Collections.emptyList();

    MetadataList purgeMeta = metadata.getList(FN_PURGE, true);
    if (purgeMeta != null) {
      purge = policyListFromMetadata(purgeMeta, forMailbox);
    }

    return new RetentionPolicy(purge);
  }

  private static List<Policy> policyListFromMetadata(MetadataList metadataList, boolean forMailbox)
      throws ServiceException {
    List<Policy> policyList = Lists.newArrayList();
    if (metadataList != null) {
      for (int i = 0; i < metadataList.size(); i++) {
        Policy policy = policyFromMetadata(metadataList.getMap(i), forMailbox);
        if (policy != null) {
          policyList.add(policy);
        }
      }
    }
    return policyList;
  }

  private static Policy policyFromMetadata(Metadata metadata, boolean forMailbox)
      throws ServiceException {
    String id = metadata.get(FN_ID, null);
    if (id != null) {
      if (forMailbox) {
        return Policy.newSystemPolicy(id);
      } else {
        return Policy.newSystemPolicy(id, metadata.get(FN_NAME), metadata.get(FN_LIFETIME));
      }
    } else {
      return Policy.newUserPolicy(metadata.get(FN_LIFETIME));
    }
  }
}
