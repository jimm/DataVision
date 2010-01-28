#! /usr/bin/env ruby

require 'copyBlock.rb'

IN_FILE = 'DataVision/intro.html'
OUT_FILE = '../jimm/overview.html'

CopyBlock.new.run(IN_FILE, OUT_FILE, /BEGIN OVERVIEW/, /END OVERVIEW/,
		  /body/, /\/body/)
