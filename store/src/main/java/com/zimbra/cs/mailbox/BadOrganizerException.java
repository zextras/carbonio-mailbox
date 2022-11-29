// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;

public class BadOrganizerException extends ServiceException {
    private static final long serialVersionUID = 5950670701876236413L;

    private BadOrganizerException(String message, Throwable cause) {
        super(message, INVALID_REQUEST, SENDERS_FAULT, cause);
    }

    /**
     * CalDAV has specific errors for this situation
     */
    public static class DiffOrganizerInComponentsException extends BadOrganizerException {
        private static final long serialVersionUID = -1061008479818171529L;

        public DiffOrganizerInComponentsException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * @param compDescription - either {@code "an appointment"} or {@code "a task"}
     */
    public static BadOrganizerException ADD_ORGANIZER_NOT_ALLOWED(String newOrganizer,
            String compDescription) {
        return new BadOrganizerException(String.format(
                "Adding ORGANIZER '%s' for %s is not allowed", newOrganizer, compDescription), null);
    }

    /**
     * @param compDescription - either {@code "an appointment"} or {@code "a task"}
     */
    public static BadOrganizerException DEL_ORGANIZER_NOT_ALLOWED(String origOrganizer,
            String compDescription) {
        return new BadOrganizerException(String.format(
                "Removing ORGANIZER '%s' for %s is not allowed", origOrganizer, compDescription), null);
    }

    /**
     * @param compDescription - either {@code "an appointment"} or {@code "a task"}
     */
    public static BadOrganizerException CHANGE_ORGANIZER_NOT_ALLOWED(String oldOrganizer, String newOrganizer,
            String compDescription) {
        return new BadOrganizerException(String.format(
                "Changing ORGANIZER of %s is not allowed: old='%s' new='%s'",
                compDescription, oldOrganizer, newOrganizer), null);
    }

    /**
     * @param compDescription - either {@code "an appointment"} or {@code "a task"}
     */
    public static BadOrganizerException ORGANIZER_INTRODUCED_FOR_EXCEPTION(String newOrganizer, String compDescription) {
        return new DiffOrganizerInComponentsException(String.format(
                "For %s, instance with ORGANIZER '%s' is not allowed.  Main rule has no ORGANIZER",
                compDescription, newOrganizer), null);
    }

    /**
     * @param compDescription - either {@code "an appointment"} or {@code "a task"}
     */
    public static BadOrganizerException DIFF_ORGANIZER_IN_COMPONENTS(String oldOrganizer, String newOrganizer,
            String compDescription) {
        return new DiffOrganizerInComponentsException(String.format(
                "For %s, instance ORGANIZER '%s' should be same as main rule ORGANIZER '%s'",
                compDescription, newOrganizer, oldOrganizer), null);
    }

    /**
     * @param compDescription - either {@code "an appointment"} or {@code "a task"}
     */
    public static BadOrganizerException MISSING_ORGANIZER_IN_SINGLE_INSTANCE(String oldOrganizer,
            String compDescription) {
        return new DiffOrganizerInComponentsException(String.format(
                "For %s, instance is missing an ORGANIZER.  Must be same as main rule ORGANIZER '%s'",
                compDescription, oldOrganizer), null);
    }
}
