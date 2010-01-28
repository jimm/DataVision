#! /bin/sh
#
# usage: translation_to_jar.sh
#
# moves the three files datavision_xx_YY.properties, menu_xx_YY.properties,
# and paper_xx_YY.properties into DataVision.jar.
#

here=`dirname $0`
jar_dir=$here/../lib

if [ -z "$1" -o -z "$2" ] ; then
    echo usage: $0 xx YY
    echo "    " where xx is the language code and YY is the country code
    exit 1
fi

cd $jar_dir
jar uf DataVision.jar datavision_$1_$2.properties menu_$1_$2.properties \
	paper_$1_$2.properties 
