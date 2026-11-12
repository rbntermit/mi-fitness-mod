#!/usr/bin/env perl
# Applies a patch folder's rules.txt: exact text edits of resources and React Native bundles.
#
#   perl scripts/text_rules.pl <decompiled app dir> <unpacked bundles dir> patches/NN-name/rules.txt
#
# rules.txt (one block per rule, blocks separated by a blank line; # starts a comment line):
#   RULE id                         short name; a rule is switched off by commenting out its whole block
#   FILE res/layout/x.xml           path in the decompiled app, or bundle:<module>/<path inside the bundle>
#   FIND <perl regex>               matched against the whole file
#   REPLACE <perl replacement>      $1.. refer to the groups of FIND
#   COUNT n | any                   exact number of matches required (anything else stops the build); "any" only
#                                   for translation fixes that apply when the app ships that language
# Prints "bundle:<module>" for every bundle it changed (build.sh repacks those).
use strict;
use warnings;

my ($src, $bundles, $spec) = @ARGV;
die "usage: $0 <app dir> <bundles dir> <rules.txt>\n" unless $src && $bundles && $spec && -f $spec;

my (@rules, $r);
open my $sf, '<:raw', $spec or die $!;
while (my $l = <$sf>) {
    $l =~ s/\r?\n$//;
    if ($l =~ /^\s*$/) { $r = undef; next; }
    next if $l =~ /^\s*#/;
    if ($l =~ /^RULE\s+(\S+)/) { $r = { id => $1 }; push @rules, $r; next; }
    die "rules: '$l' outside a RULE block\n" unless $r;
    if    ($l =~ /^FILE\s+(\S+)$/)        { $r->{file} = $1; }
    elsif ($l =~ /^FIND\s(.+)$/)          { $r->{find} = $1; }
    elsif ($l =~ /^REPLACE\s?(.*)$/)      { $r->{replace} = $1; }
    elsif ($l =~ /^COUNT\s+(\d+|any)$/)   { $r->{count} = $1; }
    else { die "rules: cannot parse '$l'\n"; }
}
close $sf;

my %touched;
for my $x (@rules) {
    for my $k (qw(file find replace count)) {
        die "rule $x->{id}: $k missing\n" unless defined $x->{$k};
    }
    my ($path, $bundle);
    if ($x->{file} =~ m{^bundle:([a-z]+)/(.+)$}) {
        $bundle = "bundle:$1";
        $path = "$bundles/$1/$2";
    } else {
        $path = "$src/$x->{file}";
    }
    if (!-f $path) {
        if ($x->{count} eq 'any') {
            print STDERR "   rule $x->{id}: no $x->{file} in this app version, skipped\n";
            next;
        }
        die "rule $x->{id}: $x->{file} not found\n";
    }
    open my $fh, '<:raw', $path or die $!;
    my $text = do { local $/; <$fh> };
    close $fh;
    my $re = qr/$x->{find}/;
    # count matches, not captured groups (a list assignment of //g would return the groups)
    my $n = 0;
    $n++ while $text =~ /$re/g;
    die "rule $x->{id}: expected $x->{count} match(es) in $x->{file}, found $n\n"
        unless $x->{count} eq 'any' || $n == $x->{count};
    next if $n == 0;
    my $rep = $x->{replace};
    $text =~ s/$re/expand($rep, $1, $2, $3, $4, $5, $6, $7, $8, $9)/ge;
    open my $out, '>:raw', $path or die $!;
    print $out $text;
    close $out;
    $touched{$bundle} = 1 if $bundle;
    print STDERR "   rule $x->{id}: $n change(s) in $x->{file}\n";
}
print "$_\n" for sort keys %touched;

sub expand {
    my ($rep, @g) = @_;
    $rep =~ s/\$(\d)/defined $g[$1 - 1] ? $g[$1 - 1] : ''/ge;
    return $rep;
}
