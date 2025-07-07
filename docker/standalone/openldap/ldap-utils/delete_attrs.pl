#!/usr/bin/perl
#
# SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only
#

use strict;
use lib "/opt/zextras/common/lib/perl5";
use Zextras::Util::Common;
use Zextras::Util::Systemd;
use File::Path;
use Net::LDAP;
use Net::LDAP::LDIF;
use Net::LDAP::Entry;
use JSON::PP;
use File::Basename;
use experimental 'smartmatch';

my $source_config_dir = "/opt/zextras/common/etc/openldap";
my $ldap_attribute_delete_dir = "$source_config_dir/zimbra/updates/attrs_to_delete/";
my $ldap_root_password = getLocalConfig("ldap_root_password");
my $ldap_master_url = getLocalConfig("ldap_master_url");
my $ldap_is_master = getLocalConfig("ldap_is_master");
my $ldap_starttls_supported = getLocalConfig("ldap_starttls_supported");
my $zimbra_tmp_directory = getLocalConfig("zimbra_tmp_directory");

my $id = getpwuid($<);
chomp $id;
if ( $id ne "root" ) {
    print STDERR "Error: must be run as root user\n";
    exit(1);
}

if (lc($ldap_is_master) ne "true") {
    exit(0);
}

if (!-d $zimbra_tmp_directory) {
    File::Path::mkpath("$zimbra_tmp_directory");
    system("chown -R zextras:zextras $zimbra_tmp_directory");
}

if ( isSystemd() ) {
    system("systemctl start carbonio-openldap.service");
    sleep 5;
}
else {
    system("/opt/zextras/bin/ldap start");
}

my @masters = split(/ /, $ldap_master_url);
my $master_ref = \@masters;
my $ldap = Net::LDAP->new($master_ref) or die "$@";

# startTLS Operation if available
my $mesg;
if ($ldap_master_url !~ /^ldaps/i) {
    if ($ldap_starttls_supported) {
        $mesg = $ldap->start_tls(
            verify => 'none',
            capath => "/opt/zextras/conf/ca",
        ) or die "start_tls: $@";
        $mesg->code && die "TLS: " . $mesg->error . "\n";
    }
}

# bind ldap or exit with error on failure fail
$mesg = $ldap->bind("cn=config", password => "$ldap_root_password");
if ($mesg->code()) {
    print "Unable to bind: $!";
    exit(0);
}

# get zimbraLDAPSchemaVersion from LDAP server
my $zimbra_ldap_schema_version;
my $last_applied_update_version;
my $result = $ldap->search(base => 'cn=zimbra', filter => '(zimbraLDAPSchemaVersion=*)', attrs => [ 'zimbraLDAPSchemaVersion' ]);
if ( $result->count > 0 ) {
    my $entry = $result->entry(0);
    $zimbra_ldap_schema_version = $entry->get_value('zimbraLDAPSchemaVersion');
    $last_applied_update_version = $zimbra_ldap_schema_version;
    &print_separater("-", "40");
    print "Installed LDAP Schema Version: $zimbra_ldap_schema_version \n";
}
else {
    print "Unable to get zimbraLDAPSchemaVersion from LDAP.\n";
    $ldap->unbind;
    exit(0);
}

# read updates folder and prepare each file for update;
if (-d "$ldap_attribute_delete_dir") {
    opendir(DIR, "$ldap_attribute_delete_dir") or die "Cannot opendir $ldap_attribute_delete_dir: $!\n";
    my @delete_attrs_files =  sort { $a <=> $b } readdir(DIR);
    while ( my $file = shift @delete_attrs_files ) {
        next unless (-f "$ldap_attribute_delete_dir/$file");
        next unless ($file =~ m/json/);
        my $infile = "$ldap_attribute_delete_dir/$file";
        &apply_delete($infile);
    }
    closedir DIR;
    &print_separater("-", "80");
}
else {
    print "LDAP Schema/Attributes update directory($ldap_attribute_delete_dir) not found.\nUnable to process LDAP updates.\n";
    $ldap->unbind;
    exit(0);
}

=begin print_separater
    print_separater($char<string>, $length<int>);
Prints $char $length times prepended by a new line;
=cut
sub print_separater(){
    my ($char, $length) = @_;
    print $char x $length;
    print "\n";
}

=begin getLocalConfig
    getLocalConfig($key<string>);
Returns value of key from localconfig, using zmlocalconfig util.
=cut
sub getLocalConfig {
    my $key = shift;

    return $main::loaded{lc}{$key}
        if (exists $main::loaded{lc}{$key});

    my $val = qx(/opt/zextras/bin/zmlocalconfig -x -s -m nokey ${key} 2> /dev/null);
    chomp $val;
    $main::loaded{lc}{$key} = $val;
    return $val;
}

=begin update_zimbra_ldap_schema_version
    update_zimbra_ldap_schema_version($last_applied_update_version<string>);
update the value of zimbraLDAPSchemaVersion in LDAP
=cut
sub update_zimbra_ldap_schema_version(){
    my ($last_updated_timestamp) = @_;
    if( $zimbra_ldap_schema_version ne $last_updated_timestamp ) {
        my $result = $ldap->search(base => 'cn=zimbra', filter => '(zimbraLDAPSchemaVersion=*)', attrs => [ 'zimbraLDAPSchemaVersion' ]);
        if ( $result->count > 0 ) {
            my $entry = $result->entry(0);
            if( $entry ){
                $entry->replace( zimbraLDAPSchemaVersion => $last_updated_timestamp );
            }
            my $msg = $entry->update( $ldap );
            if ( $msg->code() ) {
                print "Error msg: ", $entry->dn(), " ", $msg->error(), "\n";
            }
        }
        print "LDAP schema upgraded to version $last_updated_timestamp \n";
    }
}

=begin apply_delete
    apply_delete($filename<string>);
Prepare each update files for updating.
=cut
sub apply_delete(){
    my ($infile) = @_;
    # start, process only eligible update files ;
    my $infile_base_name = basename($infile);
    (my $timestamp_from_file = $infile_base_name) =~ s/\.[^.]+$//;
    chomp $timestamp_from_file;
    &print_separater("-", "80");
    if ($timestamp_from_file > $zimbra_ldap_schema_version) {
        open(FH, '<', $infile) or die "Cannot open file $infile for reading: $!\n";
        my $raw_json = join '', <FH>;
        my $json = new JSON::PP;
        eval {
            my $json_decoded = $json->decode($raw_json);
            print "Initializing updates from ", $timestamp_from_file, ".json\n";
            my @attributes =  @{$json_decoded->{deleted_attributes}};
            &delete_entries(@attributes);
            $last_applied_update_version = $timestamp_from_file;
            1;
        } or do {
            my $e = $@;
            print "Skipping: $timestamp_from_file.json\n    Reason: $e\n";
        };
    }
    else {
        print "Skipping: $timestamp_from_file.json\n    Reason: not eligible for this update.\n";
    }
    close(FH);
}


sub delete_attribute_from_entries {
    my ($base, $attribute) = @_;

    print "Searching in base '$base' for attribute '$attribute'\n";

    my $search = $ldap->search(
        base   => $base,
        scope  => 'sub',
        filter => "($attribute=*)",
        attrs  => ['dn']
    );

    if ($search->code) {
        warn "Search error: " . $search->error;
        return;
    }

    foreach my $entry ($search->entries) {
        my $dn = $entry->dn;
        print "Found DN: $dn – removing attribute '$attribute'\n";

        my $modify = $ldap->modify($dn, delete => { $attribute => [] });

        if ($modify->code) {
            warn "Failed to modify $dn: " . $modify->error;
        } else {
            print "Successfully removed '$attribute' from $dn\n";
        }
    }
}


=begin delete_entries
    delete_entries(@attributes<array>);
Deletes given attributes from all entries in LDAP
=cut
sub delete_entries {
    my (@attributes) = @_;

    foreach my $attr (@attributes) {
        delete_attribute_from_entries("cn=zimbra", $attr);
        delete_attribute_from_entries("cn=config", $attr);
    }
}

&update_zimbra_ldap_schema_version($last_applied_update_version);
$ldap->unbind;
exit(0);