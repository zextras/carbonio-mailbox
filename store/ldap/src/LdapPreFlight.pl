#!/usr/bin/perl

# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only

use strict;
use warnings;
use lib '/opt/zextras/common/lib/perl5';
use Zextras::LdapMigrationUtils;

Zextras::LdapMigrationUtils::ensure_zextras_user();

my $scenario = 'pre_flight';

my $migration_dir = "/opt/zextras/libexec/scripts/LDAP/migrations/$scenario/";
my $track_file = "/opt/zextras/log/ldap_${scenario}_tracker.txt";

my %applied_migrations = Zextras::LdapMigrationUtils::read_applied_migrations($track_file);

Zextras::LdapMigrationUtils::apply_migration_scripts($migration_dir, \%applied_migrations, $track_file);
