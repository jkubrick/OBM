#!/usr/bin/perl -w -T


use strict;
use Carp;
use POSIX ":sys_wait_h";
use LWP::UserAgent;
require HTTP::Request;
use Benchmark;

use Getopt::Long;
my %parameters;
my $return = GetOptions( \%parameters, 'loop=i', 'fork=i', 'ssogetticketuri=s', 'login=s', 'realm=s', 'password=s', 'sso', 'ldap' );

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

use constant AUTH_RANDOM => 0;
use constant AUTH_LDAP => 1;
use constant AUTH_SSO => 2;

my $waitedpid;
my $fork = 0;
my $userAgent = LWP::UserAgent->new();


sub REAPER {
    my $child;

    while (($waitedpid = waitpid(-1,WNOHANG)) > 0) {
        print STDERR 'reaped '.$waitedpid.($? ? ' with exit '.$? : '')."\n";
        $fork--;
    }

    $SIG{CHLD} = sub{ REAPER() };
}


sub spawn {
    my $coderef = shift;

    unless (@_ == 0 && $coderef && ref($coderef) eq 'CODE') {
        confess "usage: spawn CODEREF";
    }

    while( $fork >= $parameters{'fork'} ) {
        next;
    }

    $fork++;
    my $pid;
    if (!defined($pid = fork)) {
        print STDERR 'cannot fork: '.$!."\n";
        return;
    } elsif ($pid) {
        print STDERR 'starting new children at PID '.$pid."\n";
        return; # I'm the parent
    }
    # else I'm the child -- go spawn

    exit &$coderef();
}


sub loop {
    return 0;
}


sub ldapAuth {
    `/usr/sbin/testsaslauthd -u $parameters{'login'} -r $parameters{'realm'} -p $parameters{'password'}`;

    return $?;
}


sub ssoAuth {
    my $ssoURI = $parameters{'ssogetticketuri'};
    $ssoURI =~ s/\%l/$parameters{'login'}\@$parameters{'realm'}/g;
    $ssoURI =~ s/\%p/$parameters{'password'}/g;

    my $response = $userAgent->request( HTTP::Request->new( 'GET', $ssoURI ) );

    if( !$response->is_success() ) {
        print STDERR 'HTTP request fail on error '.$response->status_line().', can\'t contact : '.$parameters{'ssogetticketuri'}."\n";
        return 1;
    }

    if( $response->content() !~ /^ticket=(.+)$/ ) {
        print STDERR 'Invalid ticket : '.$response->content()."\n";
        return 1;
    }

    my $ticket = $1;
    `/usr/sbin/testsaslauthd -u $parameters{'login'} -r $parameters{'realm'} -p $ticket`;

    return $?;
}


if( !defined($parameters{'loop'}) ) {
    $parameters{'loop'} = 1000;
}

if( !defined($parameters{'fork'}) ) {
    $parameters{'fork'} = 5;
}

if( !defined($parameters{'ssogetticketuri'}) ) {
    $parameters{'ssogetticketuri'} = 'https://localhost/sso/sso_index.php?action=ticket&mode=interactive&login=%l&password=%p';
}

if( !defined($parameters{'login'}) ) {
    $parameters{'login'} = 'test01';
}

if( !defined($parameters{'realm'}) ) {
    $parameters{'realm'} = 'aliasource.fr';
}

if( !defined($parameters{'password'}) ) {
    $parameters{'password'} = 'ptest01';
}

SWITCH: {
    if( $parameters{'sso'} && !$parameters{'ldap'} ) {
        $parameters{'authType'} = AUTH_SSO;
        last SWITCH;
    }

    if( !$parameters{'sso'} && $parameters{'ldap'} ) {
        $parameters{'authType'} = AUTH_LDAP;
        last SWITCH;
    }

    $parameters{'authType'} = AUTH_RANDOM;
}


print 'Loop '.$parameters{'loop'}.' times, exec '.$parameters{'fork'}.' at same time...'."\n";
$SIG{CHLD} = sub{ REAPER() };

my $i = 0;
my $ldapAuthCount = 0;
my $ssoAuthCount = 0;
my $ldapTime = 0;
my $ssoTime = 0;


my $ldapAuthIter = 0;
my $ssoAuthIter = 0;
if( $parameters{'authType'} == AUTH_LDAP ) {
    $ldapAuthIter = $parameters{'loop'};
}elsif( $parameters{'authType'} == AUTH_SSO ) {
    $ssoAuthIter = $parameters{'loop'};
}else {
    for( my $i=0; $i<$parameters{'loop'}; $i++ ) {
        if( int(rand(2)) ) {
            $ldapAuthIter++;
        }else {
            $ssoAuthIter++;
        }
    }
}

my $ldapBenchmark;
if( $ldapAuthIter ) {
    print 'LDAP auth ('.$ldapAuthIter.' loops) : ';
    $ldapBenchmark = timethis( $ldapAuthIter, sub{ spawn( sub { ldapAuth } ) } );
}

my $ssoBenchmark;
if( $ssoAuthIter ) {
    print 'SSO auth ('.$ssoAuthIter.' loops) : ';
    $ssoBenchmark = timethis( $ssoAuthIter, sub{ spawn( sub { ssoAuth } ) } );
}
