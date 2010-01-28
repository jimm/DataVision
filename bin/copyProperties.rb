#! /usr/bin/env ruby
#
# Copies properties files to ../classes. For resource bundles, creates
# creates the (for lack of a better phrase) "superclass" files. For
# example, if datavision_fr_FR.properties exists but
# datavision_fr.properties does not and no other datavision_fr_*.properties
# files exist, we copy datavision_fr_FR.properties to
# datavision_fr.properties.
#
# We also copy the default (US English) files.
#
# One more thing: we make sure the files have permission 644.
#

require 'ftools'

DV_DIR = File.join(File.dirname(__FILE__), '..')
FROM = File.join(DV_DIR, 'jimm', 'properties')
TO = File.join(DV_DIR, 'classes')

# Remove properties files if they exist.
Dir[File.join(TO, "*.properties")].each { | f | File.delete(f) }

# Copy properties files.
%w(datavision paper menu).each { | t |
  Dir[File.join(FROM, "#{t}*.properties")].each { | f | File.cp(f, TO) }
}

# Fill in the holes by providing "superclass" properties files. For example,
# copy "datavision_bg_BG.properties" to "TO/datavision_bg.properties".
# For languages with more than one region (e.g., pt_PT and pt_BR), we
# pick a "dominant" version.
%w(bg_BG de_DE es_MX fr_FR it_IT nl_NL pt_PT ru_RU sk_SK tr_TR).each { | s |
  %w(datavision paper menu).each { | t |
    f = File.join(FROM, "#{t}_#{s}.properties")
    lang = s.sub(/_../, '')
    File.cp(f, File.join(TO, "#{t}_#{lang}.properties")) if File.exist?(f)
  }
}

# Copy the default (US English) files explicitly as en_US.
%w(datavision paper menu).each { | s |
  File.cp(File.join(FROM, "#{s}.properties"),
	  File.join(TO, "#{s}_en_US.properties"))
}

# Change permissions.
File.chmod(0644, *Dir[File.join(TO, '*.properties')])
