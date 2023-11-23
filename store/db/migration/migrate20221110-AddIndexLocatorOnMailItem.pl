#!/usr/bin/perl
#
# SPDX-FileCopyrightText: 2021 Synacor, Inc.
#
# SPDX-License-Identifier: GPL-2.0-only

# Add Index to locator COLUMN on mail_item TABLE
# The database name is dynamic and is mboxgroup<id>

use strict;
use Migrate;


########################################################################################################################
# For each mailboxgroup<id> DATABASE
foreach my $mailboxDb (Migrate::getMailboxGroups()) {
    addIndexLocator($mailboxDb);
}

exit(0);

# Add index on locator COLUMN in mail_item TABLE
sub addIndexLocator($)
{
    my ($mailboxDb) = @_;
    my $sql = "CREATE INDEX IF NOT EXISTS i_locator ON $mailboxDb.mail_item (mailbox_id, locator);";

    Migrate::logSql("Adding index on locator column for $mailboxDb.mail_item.");
    Migrate::runSql($sql);
}