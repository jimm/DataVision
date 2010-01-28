#! /usr/bin/env ruby
#
# This script copies files from one directory to another, translating
# the file contents from XHTML to HTML that the Swing HTML window can
# deal with.

$LOAD_PATH[0,1] = File.join(File.dirname(__FILE__), '..', 'bin')
require 'versionNumber'

if ARGV.length < 2
    $stderr.puts "usage: #{$0} input-xhtml-dir output-html-dir"
    exit
end

src_dir = ARGV[0]
src_dir[-1,1] = '' if src_dir[-1,1] == File::SEPARATOR
dest_dir = ARGV[1]
dest_dir[-1,1] = '' if dest_dir[-1,1] == File::SEPARATOR

Dir.mkdir(dest_dir) unless File.exist?(dest_dir)

text = nil
Dir[File.join(src_dir, '*')].each { | xhtml_file |
    File.open(xhtml_file, 'r') { | f |
	text = f.read()
    }

    # Remove XML declaration
    text.sub!(/<\?\s*xml\s*version.*?\?>\s*/im, '')

    # Modify doctype
    text.sub!(/<!DOCTYPE.*?>/m,
	      '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">')

    # Insert version number directly.
    text.sub!(/<!--#include virtual="\/version\.txt"-->/, versionFromInfo())

    # HTML tag has no attributes
    text.sub!(/<html\s+xmlns=.*?>/im, '<html>')

    # Change "id" attribute to "name"
    text.gsub!(/<a\s+id="([^"]+)\"\s*\/>/im, '<a name="\1">')

    # Change XML empty tag closing
    text.gsub!(/\s*\/>/m, '>')

    # Remove link to W3C validators
    text.gsub!(/\<!-- begin validator images.*end validator images --\>/m,
	       '&nbsp;')

    File.open(File.join(dest_dir, File.basename(xhtml_file)), 'w') { | f |
	f.write(text)
    }
}
