#! /usr/bin/env ruby
#
# Reads the current version number from jimm/datavision/info.java and
# prints it. If '-n' specified, suppress newline.
#
# This script is also included by other scripts that want to call
# versionFromInfo() to get the version number.
#

BASEDIR = File.dirname(__FILE__)

def versionFromInfo
    filename = File.join(BASEDIR, '../jimm/datavision/info.java')
    lines = IO.readlines(filename).grep(/(int|String) VERSION_/)
    major = ''
    minor = ''
    tweak = ''
    suffix = ''
    lines.each { | line |
	line =~ /VERSION_(\w+)\s*=\s*(\d+|".+")/
	which = $1
	val = $2
	case $1
	when 'MAJOR'
	    major = val
	when 'MINOR'
	    minor = val
	when 'TWEAK'
	    tweak = val
	when 'SUFFIX'
	    suffix = val.chop!	# Remove trailing "
	    suffix[0] = ""	# Remove leading " too
	end
    }
    return "#{major}.#{minor}.#{tweak}#{suffix}"
end

if __FILE__ == $0
    # Output version number. If -n, suppress newline.
    print versionFromInfo()
    puts unless ARGV.include?('-n')
end
