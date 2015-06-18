#!/usr/bin/perl

sub usage {
    print "Usage: egads <command> args\n";
    print "  Commands:\n";
    print "    StreamForecast\n";
    print "    TrainForecastingModel\n";
    print "    MakeEmptyModel\n";
    exit (0);
}

sub launch {
    my $cmd = "java -cp lib/OpenForecast-0.5.0.jar:target/egads-jar-with-dependencies.jar -Dlog4j.configurationFile=log4j.xml com.yahoo.egads." . join (' ', @ARGV);
    print STDERR $cmd, "\n";
    system $cmd;
}

my $command = $ARGV[0];

if ($command eq "MakeEmptyModel") {
    launch();
} elsif ($command eq "StreamForecast") {
    launch();
} elsif ($command eq "TrainForecastingModel") {
    launch();
} else {
    usage();
}

