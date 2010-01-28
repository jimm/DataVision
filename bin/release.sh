#!/bin/sh
#
# Called from the Makefile, this script creates the tarball
# /tmp/datavision-<version>.tar.gz.
#

appdir=datavision
dv_dir=`dirname $0`/..
version=`$dv_dir/bin/versionNumber.rb -n`

cd $dv_dir/..

# Make a copy of the datavision directory, adding the current version number
# to its name.
rm -fr $appdir-$version
cp -R $appdir $appdir-$version

# Remove CVS directories and .cvsignore, .DS_Store, TAGS, and '~' files
# from the copy.
rm -fr `find -d $appdir-$version -type d -name CVS`
rm -f `find $appdir-$version -name .cvsignore -o -name .DS_Store -o -name '*~'`
rm -f $appdir-$version/TAGS

# Make an archive of the copy.
tar -cf - $appdir-$version | gzip >/tmp/$appdir-$version.tar.gz

# Remove the copy.
rm -fr $appdir-$version

cd $appdir
