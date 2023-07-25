#!/usr/bin/perl

# SPDX-FileCopyrightText: 2022 Synacor, Inc.
# SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only

use strict;
use lib '/opt/zextras/common/lib/perl5';
use Net::LDAP;
use XML::Simple;

if ( !-d "/opt/zextras/common/etc/openldap/schema" ) {
    warn "ERROR: openldap does not appear to be installed - exiting\n";
    exit(1);
}

my $id = getpwuid($<);
chomp $id;
if ( $id ne "zextras" ) {
    warn "Error: must be run as zextras user\n";
    exit(1);
}

my $localxml           = XMLin("/opt/zextras/conf/localconfig.xml");
my $ldap_root_password = $localxml->{key}->{ldap_root_password}->{value};
my $ldap_is_master     = $localxml->{key}->{ldap_is_master}->{value};
chomp($ldap_is_master, $ldap_root_password);

if ( lc($ldap_is_master) ne "true" ) {
    exit 0;
}

my $ldap =
  Net::LDAP->new('ldapi://%2fopt%2fzextras%2fdata%2fldap%2fstate%2frun%2fldapi/')
  or die "$@";

my $mesg = $ldap->bind( "cn=config", password => "$ldap_root_password" );

$mesg->code && die "Bind: " . $mesg->error . "\n";

$mesg = $ldap->search(
    base   => "cn=accesslog",
    filter => "(objectClass=*)",
    scope  => "base",
    attrs  => ['1.1'],
);
if ( $mesg->count > 0 ) {
    my $bdn = "olcDatabase={3}mdb,cn=config";

    $mesg = $ldap->search(
        base   => "$bdn",
        filter => "(olcOverlay=syncprov)",
        scope  => "sub",
        attrs  => ['1.1'],
    );

    if ( $mesg->count == 0 ) {
        warn 
"ERROR: This is a master, with an accesslog, and syncprov is missing.\n";
        $ldap->unbind;
        exit 1;
    }
    else {
        my $dn = $mesg->entry(0)->dn;

        $mesg = $ldap->search(
            base   => "$bdn",
            filter => "(olcSpSessionLog=*)",
            scope  => "sub",
            attrs  => ['olcSpSessionLog'],
        );

        if ( $mesg->count == 0 ) {
            $mesg =
              $ldap->modify( "$dn", add => { olcSpSessionlog => '10000000' }, );
            $mesg->code && warn "failed to add entry: ", $mesg->error;
            $ldap->unbind;
            exit 0;
        }
    }
}
else {
    $ldap->unbind;
    exit 0;
}
