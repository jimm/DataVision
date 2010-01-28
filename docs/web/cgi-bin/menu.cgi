#! /usr/bin/perl
# -*- ruby -*-

use CGI;

$MENU_ITEMS = [
    ['/index.html', 'Home', [
	    ['/index.html#intro', 'Introduction'],
	    ['/index.html#news', 'News'],
	    ['/index.html#features', 'Features'],
	    ['/index.html#changes', 'Changes'],
	    ['/index.html#bugs', 'Bugs'],
	    ['/index.html#users', 'Community'],
	    ['/index.html#others', 'Other Projs']]],
    ['http://sourceforge.net/projects/datavision/', 'SourceForge [*]'],
    ['http://sourceforge.net/project/showfiles.php?group_id=33343',
     'Downloads [*]'],
    ['/docs.html', 'Documentation', [
		['/DataVision/DataVision.html', "User's Manual"],
		['/faq/faq.html', 'FAQ'],
		['/javadoc/index.html', 'Javadocs'],
		['/docs.html#volunteering', 'Volunteering'],
		['/docs.html#errata', 'Errata']]],
    ['/credits.html', 'Credits'],
    ['http://sourceforge.net/mail/?group_id=33343', 'Mailing Lists [*]'],
    ['/screenshot.html', 'Screen Shots'],
    ['/DataVision/legal.html', 'Legal']];

$this_url = $ENV{'REQUEST_URI'};

print "Content-type: text/html\n";
print "\n";

output_menu($MENU_ITEMS);
exit(0);

sub output_menu {
    my($items) = @_;

    print "\n<ul>\n";
    foreach $item (@$items) {
	print "<li>";
	output_item($item);
	output_menu($$item[2]) if $$item[2];
	print "</li>\n";
    }
    print "</ul>\n"
}

sub output_item {
    my($item) = @_;
    my($url) = $$item[0];
    my($name) = $$item[1];
    my($is_this_page) = ($this_url eq $url);

    print "<a href='$url'>" unless $is_this_page;
    print $name;
    print '</a>' unless $is_this_page;
}
