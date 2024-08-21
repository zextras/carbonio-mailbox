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
my $dn = "cn=module{0},cn=config";

my $entry = $ldap->search(
    base   => $dn,
    scope   => 'base',
    filter  => '(objectClass=*)',
)->entry(0);

my @module_loads = $entry->get_value('olcModuleLoad', asref => 1);
my $exists = grep { $_ eq '{8}pw-argon2.la' } @module_loads;

if($exists){
    my $mesg = $ldap->modify(
        $dn,
        delete => { olcModuleLoad => '{8}pw-argon2.la' },
        add => { olcModuleLoad => '{8}argon2' }
    );
    $mesg->code && die "Modify: " . $mesg->error . "\n";
}else{
    print "The value '{8}pw-argon2.la' does not exist. No modification performed.";
}

$ldap->unbind();
