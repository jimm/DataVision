#! /usr/bin/env ruby
#
# Copies sections of files from the docs into Web pages.
#

require 'ftools'
require 'copyBlock.rb'

CHANGES_IN_FILE = 'DataVision/intro.html'
CHANGES_OUT_FILE = 'web/htdocs/index.html'

CopyBlock.new.run(CHANGES_IN_FILE, CHANGES_OUT_FILE,
		  /BEGIN RECENT CHANGES/, /END RECENT CHANGES/)
CopyBlock.new.run(CHANGES_IN_FILE, CHANGES_OUT_FILE,
		  /BEGIN OVERVIEW/, /END OVERVIEW/)
system("chmod +x #{CHANGES_OUT_FILE}")
