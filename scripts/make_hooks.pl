#!/usr/bin/env perl
# Generates a patch folder's smali.patch from a hooks.txt spec, so hunks never have to be written by hand.
#
#   perl scripts/make_hooks.pl <decompiled app dir> patches/NN-name/hooks.txt > patches/NN-name/smali.patch
#
# hooks.txt:
#   CLASS com/x/Y                      class path inside smali*/ (no .smali)
#   METHOD name(Lsig;)V                method name + signature, exactly as in the .method line
#   AT start | after <regex> | before <regex>
#                                      start = before the first instruction (default); after/before = after or
#                                      before the first line of the method matching the regex
#   TEMPS n                            v0..v(n-1) are free scratch registers; .locals is raised when needed
#   CODE                               smali lines up to the next blank line
# Lines starting with # are comments. pN in CODE are the method's parameters; at "start" they are untouched.
# The script refuses a hook whose pN register would not fit a 4-bit operand, and a .locals raise that would push
# the method's own parameter registers past v15.
use strict;
use warnings;

my ($src, $spec) = @ARGV;
die "usage: $0 <app dir> <hooks.txt>\n" unless $src && $spec && -d $src && -f $spec;

sub find_class {
    my ($cls) = @_;
    my @f = grep { -f $_ } map { "$_/$cls.smali" } glob("$src/smali*");
    die "class $cls not found\n" unless @f == 1;
    return $f[0];
}

sub param_regs {
    my ($line, $sig) = @_;
    my ($args) = $sig =~ /\((.*)\)/ or die "bad signature $sig\n";
    my $n = $line =~ /\bstatic\b/ ? 0 : 1;
    while ($args =~ /\G(\[*)(?:(L[^;]+;)|([JD])|([ZBSCIF]))/g) {
        $n += ($3 && !$1) ? 2 : 1;
    }
    return $n;
}

# read spec
my (@hooks, $h, $in_code);
open my $sf, '<', $spec or die $!;
my $cls;
while (my $l = <$sf>) {
    $l =~ s/\r?\n$//;
    if ($in_code) {
        if ($l =~ /^\s*$/) { $in_code = 0; next; }
        push @{ $h->{code} }, $l;
        next;
    }
    next if $l =~ /^\s*(#|$)/;
    if    ($l =~ /^CLASS\s+(\S+)/)  { $cls = $1; }
    elsif ($l =~ /^METHOD\s+(\S+)/) { $h = { cls => $cls, method => $1, at => 'start', temps => 0, code => [] }; push @hooks, $h; }
    elsif ($l =~ /^AT\s+start/)     { $h->{at} = 'start'; }
    elsif ($l =~ /^AT\s+after\s+(.+)$/) { $h->{at} = 'after'; $h->{re} = $1; }
    elsif ($l =~ /^AT\s+before\s+(.+)$/) { $h->{at} = 'before'; $h->{re} = $1; }
    elsif ($l =~ /^TEMPS\s+(\d+)/)  { $h->{temps} = $1; }
    elsif ($l =~ /^CODE\s*$/)       { $in_code = 1; }
    else { die "spec: cannot parse '$l'\n"; }
}
close $sf;

# group by class, keep spec order
my (%by, @order);
for my $x (@hooks) { push @order, $x->{cls} unless $by{ $x->{cls} }; push @{ $by{ $x->{cls} } }, $x; }

my $tmp = ($ENV{TMPDIR} // '/tmp') . "/make_hooks.$$";
for my $c (@order) {
    my $file = find_class($c);
    open my $fh, '<', $file or die $!;
    my @lines = map { s/\r?\n$//r } <$fh>;
    close $fh;
    my @orig = @lines;
    for my $x (@{ $by{$c} }) {
        my ($m) = grep { $lines[$_] =~ /^\.method .*\Q $x->{method}\E$/ } 0 .. $#lines;
        die "$c: method $x->{method} not found\n" unless defined $m;
        my $mline = $lines[$m];
        my ($li) = grep { $lines[$_] =~ /^\s*\.locals \d+/ } $m + 1 .. $m + 3;
        die "$c->$x->{method}: no .locals\n" unless defined $li;
        my ($locals) = $lines[$li] =~ /\.locals (\d+)/;
        my $params = param_regs($mline, $x->{method});
        if ($locals < $x->{temps}) {
            die "$c->$x->{method}: raising .locals to $x->{temps} would move parameters past v15\n"
                if $x->{temps} + $params > 16;
            $lines[$li] =~ s/\.locals \d+/.locals $x->{temps}/;
            $locals = $x->{temps};
        }
        for my $cl (@{ $x->{code} }) {
            for my $p ($cl =~ /\bp(\d+)\b/g) {
                die "$c->$x->{method}: p$p is not a parameter\n" if $p >= $params;
                die "$c->$x->{method}: p$p is v" . ($locals + $p) . ", too high for this hook\n"
                    if $locals + $p > 15 && $cl !~ /\/from16|\/range/;
            }
        }
        my $at;
        if ($x->{at} ne 'start') {
            my $re = qr/$x->{re}/;
            for my $i ($li + 1 .. $#lines) {
                die "$c->$x->{method}: anchor not found\n" if $lines[$i] =~ /^\.end method/;
                if ($lines[$i] =~ $re) { $at = $i + ($x->{at} eq 'after' ? 1 : 0); last; }
            }
        } else {
            my $ann = 0;
            for my $i ($li + 1 .. $#lines) {
                my $t = $lines[$i];
                if ($ann) { $ann = 0 if $t =~ /^\s*\.end annotation/; next; }
                if ($t =~ /^\s*\.annotation\b/) { $ann = 1; next; }
                next if $t =~ /^\s*$/ || ($t =~ /^\s*\./ && $t !~ /^\s*\.line\b/);
                $at = $i; last;
            }
        }
        die "$c->$x->{method}: no insertion point\n" unless defined $at;
        splice @lines, $at, 0, '', '    # band-mod hook', @{ $x->{code} }, '';
        print STDERR "  $c->$x->{method}: locals $locals, params $params\n";
    }
    for my $side (['a', \@orig], ['b', \@lines]) {
        my $p = "$tmp/$side->[0]/$c.smali";
        (my $d = $p) =~ s{/[^/]+$}{};
        system('mkdir', '-p', $d) == 0 or die;
        open my $o, '>', $p or die $!;
        print $o join("\n", @{ $side->[1] }), "\n";
        close $o;
    }
}
chdir $tmp or die;
for my $c (@order) {
    my $out = `diff -u a/$c.smali b/$c.smali`;
    $out =~ s/^(---|\+\+\+) (\S+)\t.*$/$1 $2/mg;
    print $out;
}
chdir '/';
system('rm', '-rf', $tmp);
