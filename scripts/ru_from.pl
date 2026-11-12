#!/usr/bin/env perl
# Russian strings for an app version whose bundle has no Russian split, taken from an older version (RU_FROM).
#
#   perl scripts/ru_from.pl <old res dir> <new res dir> <report file>
#
# <old res>/values-ru/{strings,plurals,arrays}.xml give the translations; <old res>/values gives the old English
# (to notice plurals whose structure changed); <new res>/values is the English of the version being built.
# A translation is taken only when the name exists in the new English, the entry is translatable, and:
#   string        format specifiers (%s, %1$d, %%...) and HTML tags (<b>, &lt;a href...&gt;...) are the same as in
#                 the new English (same multiset, same positions);
#   plurals       the new English has the same number of items as the old English, and every Russian item has
#                 the specifiers and tags of the new English "other" item;
#   string-array  same number of items as the new English, item by item the same specifiers and tags.
# Anything else stays English. Writes <new res>/values-ru/{strings,plurals,arrays}.xml and a report.
use strict;
use warnings;

my ($old, $new, $report) = @ARGV;
die "usage: $0 <old res> <new res> <report>\n" unless $old && $new && $report;
for my $f ("$old/values-ru/strings.xml", "$new/values/strings.xml") {
    die "missing $f\n" unless -f $f;
}
die "$new/values-ru already exists: the bundle has its own Russian, RU_FROM is not needed\n"
    if -e "$new/values-ru/strings.xml" || -e "$new/values-ru/plurals.xml" || -e "$new/values-ru/arrays.xml";

sub slurp { my $f = shift; return '' unless -f $f; open my $h, '<:raw', $f or die "$f: $!"; local $/; my $t = <$h>; close $h; $t }

# name => { attrs, body } for <string>; also collects translatable="false"
sub strings {
    my $t = shift; my %s;
    while ($t =~ m{<string name="([^"]+)"([^>]*?)(?:/>|>(.*?)</string>)}sg) {
        $s{$1} = { attrs => $2, body => defined $3 ? $3 : '', raw => $& };
    }
    return \%s;
}
sub plurals {
    my $t = shift; my %p;
    while ($t =~ m{<plurals name="([^"]+)"([^>]*)>(.*?)</plurals>}sg) {
        my ($n, $a, $b, $raw) = ($1, $2, $3, $&); my %q;
        $q{$1} = $2 while $b =~ m{<item quantity="([a-z]+)">(.*?)</item>}sg;
        $p{$n} = { attrs => $a, items => \%q, raw => $raw };
    }
    return \%p;
}
sub arrays {
    my $t = shift; my %a;
    while ($t =~ m{<((?:string-|integer-)?array) name="([^"]+)"([^>]*)>(.*?)</\1>}sg) {
        my ($k, $n, $at, $b, $raw) = ($1, $2, $3, $4, $&); my @i;
        push @i, $1 while $b =~ m{<item>(.*?)</item>}sg;
        $a{$n} = { kind => $k, attrs => $at, items => \@i, raw => $raw };
    }
    return \%a;
}

# signature of a text: sorted format specifiers and HTML tag names (literal or escaped)
sub sig {
    my $s = shift;
    my @f = ($s =~ /(%(?:\d+\$)?[-#+ 0,(<]*\d*(?:\.\d+)?[a-zA-Z%])/g);
    my @t = map { lc } ($s =~ m{(?:<|&lt;)(/?[a-zA-Z][\w:.-]*)}g);
    return join(' ', sort @f) . ' | ' . join(' ', sort @t);
}
sub untranslatable { my $a = shift // ''; return $a =~ /translatable="false"/ }

my $oldRuS = strings(slurp("$old/values-ru/strings.xml"));
my $oldRuP = plurals(slurp("$old/values-ru/plurals.xml"));
my $oldRuA = arrays(slurp("$old/values-ru/arrays.xml"));
my $oldEnP = plurals(slurp("$old/values/plurals.xml"));
my $newEnS = strings(slurp("$new/values/strings.xml"));
my $newEnP = plurals(slurp("$new/values/plurals.xml"));
my $newEnA = arrays(slurp("$new/values/arrays.xml"));

my (%stat, @why, @outS, @outP, @outA);
sub drop { my ($kind, $name, $reason) = @_; $stat{"$kind dropped: $reason"}++; push @why, "$kind\t$name\t$reason"; }

for my $n (sort keys %$oldRuS) {
    my $en = $newEnS->{$n};
    if (!$en) { drop('string', $n, 'нет в 3.60'); next; }
    if (untranslatable($en->{attrs})) { drop('string', $n, 'в 3.60 не переводится'); next; }
    my $ru = $oldRuS->{$n};
    if (sig($ru->{body}) ne sig($en->{body})) { drop('string', $n, 'формат или теги не совпадают'); next; }
    push @outS, $ru->{raw}; $stat{'string taken'}++;
}
for my $n (sort keys %$oldRuP) {
    my $en = $newEnP->{$n};
    if (!$en) { drop('plurals', $n, 'нет в 3.60'); next; }
    my $oe = $oldEnP->{$n};
    if ($oe && scalar(keys %{ $oe->{items} }) != scalar(keys %{ $en->{items} })) {
        drop('plurals', $n, 'в 3.60 другое число элементов'); next;
    }
    my $ref = $en->{items}{other} // (values %{ $en->{items} })[0] // '';
    my $bad = grep { sig($_) ne sig($ref) } values %{ $oldRuP->{$n}{items} };
    if ($bad) { drop('plurals', $n, 'формат или теги не совпадают'); next; }
    push @outP, $oldRuP->{$n}{raw}; $stat{'plurals taken'}++;
}
for my $n (sort keys %$oldRuA) {
    my $en = $newEnA->{$n};
    if (!$en) { drop('array', $n, 'нет в 3.60'); next; }
    if (untranslatable($en->{attrs})) { drop('array', $n, 'в 3.60 не переводится'); next; }
    my ($r, $e) = ($oldRuA->{$n}{items}, $en->{items});
    if (@$r != @$e) { drop('array', $n, 'другое число элементов'); next; }
    my $bad = grep { sig($r->[$_]) ne sig($e->[$_]) } 0 .. $#$r;
    if ($bad) { drop('array', $n, 'формат или теги не совпадают'); next; }
    push @outA, $oldRuA->{$n}{raw}; $stat{'array taken'}++;
}

sub write_res {
    my ($file, @items) = @_;
    return unless @items;
    mkdir "$new/values-ru";
    open my $o, '>:raw', $file or die "$file: $!";
    print $o qq{<?xml version="1.0" encoding="utf-8"?>\n<resources>\n};
    print $o "    $_\n" for @items;
    print $o "</resources>\n";
    close $o;
}
write_res("$new/values-ru/strings.xml", @outS);
write_res("$new/values-ru/plurals.xml", @outP);
write_res("$new/values-ru/arrays.xml", @outA);

# what stays English: translatable entries of the new version without a Russian one
my %have = map { $_ => 1 } map { m{name="([^"]+)"} ? $1 : () } (@outS, @outP, @outA);
my $engS = grep { !$have{$_} && !untranslatable($newEnS->{$_}{attrs}) } keys %$newEnS;
my $engP = grep { !$have{$_} } keys %$newEnP;
my $engA = grep { !$have{$_} && !untranslatable($newEnA->{$_}{attrs}) } keys %$newEnA;

open my $r, '>:raw', $report or die $!;
printf $r "Перенесено: строк %d, plurals %d, массивов %d\n", $stat{'string taken'} // 0, $stat{'plurals taken'} // 0,
    $stat{'array taken'} // 0;
print $r "Отброшено:\n";
printf $r "  %-60s %d\n", $_, $stat{$_} for grep { /dropped/ } sort keys %stat;
printf $r "Осталось на английском (есть в 3.60, перевода нет): строк %d, plurals %d, массивов %d\n", $engS, $engP, $engA;
print $r "\nОтброшенные по одной (вид, имя, причина):\n";
print $r "$_\n" for @why;
close $r;
printf "   ru from old version: strings %d, plurals %d, arrays %d taken; %d dropped; English left: %d strings\n",
    $stat{'string taken'} // 0, $stat{'plurals taken'} // 0, $stat{'array taken'} // 0, scalar(@why), $engS;
