#!/usr/bin/perl

# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only

use strict;
use lib '/opt/zextras/common/lib/perl5';
use Net::LDAP;
use XML::Simple;

if ( ! -d "/opt/zextras/common/etc/openldap/schema" ) {
  print STDERR "ERROR: openldap does not appear to be installed - exiting\n";
  exit(1);
}

my $id = getpwuid($<);
chomp $id;
if ($id ne "zextras") {
    print STDERR "Error: must be run as zextras user\n";
    exit (1);
}

my $ldap_status = qx(/opt/zextras/bin/ldap status);
if ($ldap_status =~ /slapd running pid/) {
    print "LDAP is already running.\n";
} else {
    my $rc = qx(/opt/zextras/bin/ldap start);
    if ($? != 0) {
        die "Failed to start LDAP, Exit status:" . ($? >> 8) . "\n";
    }
    print "LDAP started successfully.\n";
}

print "* Start applying 'olcPidFile' path migration..\n";
my $localxml = XMLin("/opt/zextras/conf/localconfig.xml");
my $ldap_root_password = $localxml->{key}->{ldap_root_password}->{value};
chomp($ldap_root_password);

my @known_ldap_socket_paths = (
    'ldapi://%2frun%2fcarbonio%2frun%2fldapi/',
    'ldapi://%2fopt%2fzextras%2fdata%2fldap%2fstate%2frun%2fldapi/'
);

my $ldap;

foreach my $ldap_socket (@known_ldap_socket_paths) {
    $ldap = Net::LDAP->new($ldap_socket);
    last if $ldap;
}

unless ($ldap) {
    die "Failed to connect to LDAP server using any of the provided LDPAI socket paths.";
}

my $mesg = $ldap->bind("cn=config", password=>"$ldap_root_password");

$mesg->code && die "Bind: ". $mesg->error . "\n";

my $dn = "cn=config";
$mesg = $ldap->modify(
    $dn,
    replace => { olcPidFile => '/run/carbonio/slapd.pid' },
);

$mesg->code && die "Modify: ". $mesg->error . "\n";

$ldap->unbind;

my $rc = qx(/opt/zextras/bin/ldap restart);
if ($? != 0) {
    die "Failed to restart LDAP, Exit status:" . ($? >> 8) . "\n";
}
print "* Migration applied successfully.\n";
