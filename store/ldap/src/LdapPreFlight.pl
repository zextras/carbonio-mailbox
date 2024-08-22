#!/usr/bin/perl

# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only

use strict;
use warnings;
use lib '/opt/zextras/common/lib/perl5';
use Zextras::LdapMigrationUtils;

Zextras::LdapMigrationUtils::ensure_zextras_user();

sub apply_scripts {
    my ($base_dir, @scripts) = @_;

    foreach my $script (@scripts) {
        my $script_path = $base_dir . "/" . $script;
        print "** Applying changes from $script_path...\n";

        my $output = `$script_path 2>&1`;
        if ($?) {
            print " * Error applying changes from $script_path: $output\n";
            exit(1);
        }
        else {
            $output =~ s/^\s+|\s+$//g;
            if ($output) {
                print "\t$output\n";
            }
            print " * done.\n\n";
        }
    }
}

my $base_dir = "/opt/zextras/libexec/scripts/LDAP/migrations/pre_flight/";

my @scripts = (
    "01_olcPidFile_path_update.pl",
    "02_ldap_module_conf_update.sh"
);

apply_scripts($base_dir, @scripts);