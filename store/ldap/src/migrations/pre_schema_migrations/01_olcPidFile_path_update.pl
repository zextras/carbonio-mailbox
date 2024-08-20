#!/usr/bin/perl

# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only

use strict;
use warnings;
use lib '/opt/zextras/common/lib/perl5';
use Zextras::LdapMigrationUtils;

Zextras::LdapMigrationUtils::ensure_zextras_user();

my $ldap = Zextras::LdapMigrationUtils::connect_ldap();
my $dn = "cn=config";
my $mesg = $ldap->modify(
    $dn,
    replace => { olcPidFile => '/run/carbonio/slapd.pid' },
);
$mesg->code && die "Modify: " . $mesg->error . "\n";
$ldap->unbind();
