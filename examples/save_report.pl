#! /usr/bin/env perl
#
# This sample script goes with ../applet.html. It saves all input to
# the file $SAVE_DIR/report.xml. There is no attempt at creating unique
# report file names or doing any error checking.
#
# You may want to edit $SAVE_DIR. Make sure that directory is writable by
# the process running the Web server.
#

$SAVE_DIR = '/tmp';

$fileName = "$SAVE_DIR/report.xml";
open(OUT, ">$fileName") || reply("can't open output file $fileName: $!");
while (<STDIN>) {
    print OUT $_;
}
close(OUT);
reply("Report XML file saved to file $fileName.");


sub reply {
    my($msg) = @_;

    print "Content-type: text/plain\n";
    print "\n\n";
    print "$msg\n";
    exit(0);
}
