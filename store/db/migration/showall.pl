#!/usr/bin/perl -w
# 
# SPDX-FileCopyrightText: 2021 Synacor, Inc.
#
# SPDX-License-Identifier: GPL-2.0-only
# 

# Prints the output of SHOW CREATE TABLE for all zimbra databases

use strict;
use Migrate;

my @databases = Migrate::runSql("SHOW DATABASES;", 0);
foreach my $database (@databases) {
    $database = lc($database);
    if ($database eq "zimbra" || $database =~ /^mailbox[0-9]+$/) {
	print("Database $database:\n");
	my @tables = Migrate::runSql("SHOW TABLES FROM $database;", 0);
	foreach my $table (@tables) {
	    my $row = (Migrate::runSql("SHOW CREATE TABLE $database.$table;", 0))[0];
	    my $create = (split("\t", $row))[1];
	    $create =~ s/\\n/\n/g;
	    print("\n" . $create . "\n");
	}
    }
}
