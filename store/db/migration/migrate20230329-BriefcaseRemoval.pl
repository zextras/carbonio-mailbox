#!/usr/bin/perl

# 
# SPDX-FileCopyrightText: 2021 Zextras, Srl.
#
# SPDX-License-Identifier: GPL-2.0-only
# 

#
# Remove Briefcase system folder (id = 16) for each mailbox.
#

use strict;
use Migrate;

my $CONCURRENCY = 10;
my $ID = 16;
my $METADATA = 'd1:ai1e4:mseqi1e4:unxti17e1:vi10e2:vti8ee';
my $NOW = time();
my $FOLDERNAME = 'Briefcase';

my @sqlRemove;
my %mailboxes = Migrate::getMailboxes();
foreach my $mboxId (sort(keys %mailboxes)) {
    my $gid = $mailboxes{$mboxId};
    my $sql = removeFolder($mboxId, $gid);
    push(@sqlRemove, $sql);
}

Migrate::runSqlParallel($CONCURRENCY, @sqlRemove);

exit(0);


#####################

sub removeFolder($) {
    my ($mboxId, $gid) = @_;

    my $sql = <<_SQL_;
DELETE FROM mboxgroup$gid.mail_item
WHERE mailbox_id = $mboxId AND folder_id = 1 AND id = $ID AND metadata = '$METADATA';
_SQL_
    return $sql;
}
