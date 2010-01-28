#! /usr/bin/env ruby
#
# usage: preFilter.rb in_file out_file
#
# Filters a file, making it acceptable for a <pre> XHTML block. Munges
# all email addresses, too.
#

require 'cgi'

File.open(ARGV[1], 'w') { | out |
    IO.readlines(ARGV[0]).each { | line |
	line.chomp!
	line.gsub!(/(\w)\@(\w)/, '\1 at \2')
	line = CGI.escapeHTML(line)
	out.puts line
    }
}
