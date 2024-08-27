# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only

package Zextras::LdapMigrationUtils;

use strict;
use warnings;
use Net::LDAP;
use XML::Simple;
use File::Find;
use File::Spec;
use POSIX qw(getpwuid);
use File::Path qw(make_path);


# Function to check if LDAP is running
sub ldap_is_running {
    my $status = qx(/opt/zextras/bin/ldap status);
    return $status =~ /slapd running pid/;
}

# Function to LDAP backup using zmslapcat
sub ldap_backup {
    my $rc = qx(/opt/zextras/libexec/zmslapcat -c );
    die "Failed to backup LDAP, Exit status:" . ($? >> 8) . "\n" if $? != 0;
}

# Function to start LDAP
sub start_ldap {
    my $rc = qx(/opt/zextras/bin/ldap start);
    die "Failed to start LDAP, Exit status:" . ($? >> 8) . "\n" if $? != 0;
}

# Function to restart LDAP
sub restart_ldap {
    my $rc = qx(/opt/zextras/bin/ldap restart);
    die "Failed to restart LDAP, Exit status:" . ($? >> 8) . "\n" if $? != 0;
}

# Function to get LDAP root password
sub get_ldap_root_password {
    my $localxml = XMLin("/opt/zextras/conf/localconfig.xml");
    my $ldap_root_password = $localxml->{key}->{ldap_root_password}->{value};
    chomp($ldap_root_password);
    return $ldap_root_password;
}

# Function to connect to LDAP
sub connect_ldap {
    my $ldap_root_password = get_ldap_root_password();
    my @known_ldap_socket_paths = (
        'ldapi://%2frun%2fcarbonio%2frun%2fldapi/',
        'ldapi://%2fopt%2fzextras%2fdata%2fldap%2fstate%2frun%2fldapi/'
    );

    my $ldap;
    foreach my $ldap_socket (@known_ldap_socket_paths) {
        $ldap = Net::LDAP->new($ldap_socket);
        last if $ldap;
    }

    die "Failed to connect to LDAP server using any of the provided LDPAI socket paths." unless $ldap;

    my $mesg = $ldap->bind("cn=config", password => $ldap_root_password);
    $mesg->code && die "Bind: " . $mesg->error . "\n";

    return $ldap;
}

# Function to ensure the script is run as the 'zextras' user
sub ensure_zextras_user {
    my $id = getpwuid($<);
    chomp $id;
    if ($id ne "zextras") {
        die "Error: must be run as zextras user\n";
    }
}

# Function to ensure LDAP is running
sub ensure_ldap_running {
    unless (ldap_is_running()) {
        start_ldap();
        print "LDAP started successfully.\n";
    } else {
        print "LDAP is already running.\n";
    }
}

1;
