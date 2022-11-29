// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import java.util.ArrayList;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.mime.MimeConstants;

/**
 * Walks a JavaMail MIME tree and calls the abstract methods for each node. 
 *   
 * @author bburtin
 */
public abstract class MimeVisitor {

    /** The list of registered MimeVistor classes that convert stored
     *  messages on the fly. */
    private static List<Class<? extends MimeVisitor>> sMimeConverters = new ArrayList<Class<? extends MimeVisitor>>();
    /** The list of registered MimeVistor classes that convert new messages
     *  before storing them to disk. */
    private static List<Class<? extends MimeVisitor>> sMimeMutators   = new ArrayList<Class<? extends MimeVisitor>>();

        static {
            try {
                if (LC.zimbra_converter_enabled_uuencode.booleanValue())
                    registerConverter(UUEncodeConverter.class);
            } catch (Exception e) {
                ZimbraLog.misc.error("error loading UUENCODE converter", e);
            }

            try {
                if (LC.zimbra_converter_enabled_tnef.booleanValue())
                    registerConverter(TnefConverter.class);
            } catch (Exception e) {
                ZimbraLog.misc.error("error loading TNEF converter", e);
            }
        }

    /** Adds a MimeVisitor class to the list of converters invoked on the fly
     *  when a message is fetched from the store or prepared for indexing.
     *  Note that changes made by these MimeVisitors are not persisted to disk
     *  but instead are executed every time the message is accessed. */
    public static void registerConverter(Class<? extends MimeVisitor> vclass) {
        if (vclass != null) {
            ZimbraLog.lmtp.debug("Registering MIME converter: %s", vclass.getName());
            sMimeConverters.add(vclass);
        }
    }
    
    /** Removes a <tt>MimeVisitor</tt> from the list of converters. */
    public static void unregisterConverter(Class<? extends MimeVisitor> vclass) {
        if (vclass != null) {
            ZimbraLog.lmtp.debug("Unregistering MIME converter: %s", vclass.getName());
            sMimeConverters.remove(vclass);
        }
    }

    /** Retrieves the list of all registered MimeVisitor converter classes.
     * @see #registerConverter(Class) */
    public static List<Class<? extends MimeVisitor>> getConverters() {
        return new ArrayList<Class<? extends MimeVisitor>>(sMimeConverters);
    }

    /** Returns whether there are any registered MimeVisitor converter classes.
     * @see #registerConverter(Class) */
    public static boolean anyConvertersRegistered() {
        return !sMimeConverters.isEmpty();
    }

    /** Adds a MimeVisitor class to the list of mutators invoked before a
     *  message is saved to disk or sent via SMTP. */
    public static void registerMutator(Class<? extends MimeVisitor> vclass) {
        if (vclass != null) {
            ZimbraLog.lmtp.debug("Registering MIME mutator: %s", vclass.getName());
            sMimeMutators.add(vclass);
        }
    }
    
    /** Removes a <tt>MimeVisitor</tt> from the list of mutators. */
    public static void unregisterMutator(Class<? extends MimeVisitor> vclass) {
        if (vclass != null) {
            ZimbraLog.lmtp.debug("Unregistering MIME mutator: %s", vclass.getName());
            sMimeMutators.remove(vclass);
        }
    }

    /** Retrieves the list of all registered MimeVisitor mutator classes.
     * @see #registerMutator(Class) */
    public static List<Class<? extends MimeVisitor>> getMutators() {
        return new ArrayList<Class<? extends MimeVisitor>>(sMimeMutators);
    }

    /** Returns whether there are any registered MimeVisitor mutator classes.
     * @see #registerMutator(Class) */
    public static boolean anyMutatorsRegistered() {
        return !sMimeMutators.isEmpty();
    }


    /** This inner interface permits the {@link Mime#accept} caller to be
     *  notified immediately before any changes to the MimeMessage are
     *  performed by a <code>MimeVistor</code>.  Note that when a call to
     *  {@link Mime#accept} results in multiple modifications, the callback
     *  will be invoked multiple times. */
    public static interface ModificationCallback {
        /** A callback function invoked immediately prior to any modification
         *  of the message.  If the callback returns <code>false</code>, the
         *  modification is not performed. */
        public boolean onModification();
    }

    protected ModificationCallback mCallback;

    /** Sets the MimeVisitor's pre-modification callback.  The callback can
     *  be unset by passing <code>null</code> as the argument.
     * @return the <code>MimeVisitor</code> itself */
    public MimeVisitor setCallback(ModificationCallback callback) {
        mCallback = callback;
        return this;
    }
    /** Returns the pre-modification callback currently associated with the
     *  MimeVisitor, or <code>null</code> if there is no such callback. */
    public ModificationCallback getCallback()  { return mCallback; }


    /** The flags passed to the <code>visitXXX</code> methods before and
     *  after a node's children are visited, respectively. */
    protected enum VisitPhase { VISIT_BEGIN, VISIT_END };

    /** Visitor callback for traversing a MimeMessage, either standalone
     *  or as an attachment to another MimeMessage.
     * @return whether any changes were performed during the visit
     * @see VisitPhase */
    protected abstract boolean visitMessage(MimeMessage mm, VisitPhase visitKind) throws MessagingException;

    /** Visitor callback for traversing a Multipart.
     * @return whether any changes were performed during the visit
     * @see VisitPhase */
    protected abstract boolean visitMultipart(MimeMultipart mp, VisitPhase visitKind) throws MessagingException;

    /** Visitor callback for traversing a BodyPart.
     * @return whether any changes were performed during the visit
     * @see VisitPhase */
    protected abstract boolean visitBodyPart(MimeBodyPart bp) throws MessagingException;


    /** Walks the mail object tree depth-first, starting at the specified
     *  <code>MimePart</code>.  Invokes the various <code>MimeVisitor</code>
     *  methods in for each visited node.
     * 
     * @param mp the root MIME part at which to start the traversal */
    public synchronized final boolean accept(MimeMessage mm) throws MessagingException {
        return accept(mm, 0);
    }

    private static final int MAX_VISITOR_DEPTH = LC.zimbra_converter_depth_max.intValue();

    private synchronized final boolean accept(MimePart mp, int depth) throws MessagingException {
        // do not recurse beyond a fixed depth
        if (depth >= MAX_VISITOR_DEPTH)
            return false;

        boolean modified = false;

        if (mp instanceof MimeMessage)
            modified |= visitMessage((MimeMessage) mp, VisitPhase.VISIT_BEGIN);

        String ctype = Mime.getContentType(mp);
        boolean isMultipart = ctype.startsWith(MimeConstants.CT_MULTIPART_PREFIX);
        boolean isMessage = !isMultipart && ctype.equals(MimeConstants.CT_MESSAGE_RFC822);

        if (isMultipart) {
            Object content = null;
            try {
                content = Mime.getMultipartContent(mp, ctype);
            } catch (Exception e) {
                ZimbraLog.extensions.warn("could not fetch multipart content; skipping", e);
            }
            if (content instanceof MimeMultipart) {
                MimeMultipart multi = (MimeMultipart) content;
                boolean multiModified = false;

                if (visitMultipart(multi, VisitPhase.VISIT_BEGIN))
                    modified = multiModified = true;

                try {
                    for (int i = 0; i < multi.getCount(); i++) {
                        BodyPart bp = multi.getBodyPart(i);
                        if (bp instanceof MimeBodyPart) {
                            if (accept((MimeBodyPart) bp, depth + 1))
                                modified = multiModified = true;
                        } else {
                            ZimbraLog.extensions.info("unexpected BodyPart subclass: " + bp.getClass().getName());
                        }
                    }
                } catch (MessagingException e) {
                    ZimbraLog.extensions.warn("could not fetch body subpart; skipping remainder", e);
                }

                if (visitMultipart(multi, VisitPhase.VISIT_END))
                    modified = multiModified = true;
                if (multiModified)
                    mp.setContent(multi);
            }
        } else if (isMessage) {
            MimeMessage content = null;
            try {
                content = Mime.getMessageContent(mp);
            } catch (Exception e) {
                ZimbraLog.extensions.warn("could not fetch attached message content; skipping", e);
            }
            if (content != null)
                modified |= accept(content, depth + 1);
        } else if (mp instanceof MimeBodyPart) {
            modified |= visitBodyPart((MimeBodyPart) mp);
        } else if (!(mp instanceof MimeMessage)) {
            ZimbraLog.extensions.info("unexpected MimePart subclass: " + mp.getClass().getName() + " (ctype='" + ctype + "')");
        }

        if (mp instanceof MimeMessage) {
            MimeMessage mm = (MimeMessage) mp;
            modified |= visitMessage(mm, VisitPhase.VISIT_END);

            // commit changes to the message
            if (modified)
                mm.saveChanges();
        }

        return modified;
    }
}
