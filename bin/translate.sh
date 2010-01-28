#! /bin/sh
#
# usage: translate.sh xx YY [encoding]
#
# where "xx" is the language code and "YY" is the country code. "encoding"
# is optional. It specifies a character encoding. Possible values include
#
# US_ASCII
# ISO-8859-1 (the default)
# UTF-8
# UTF-16BE
# UTF-16LE
# UTF-16
#

here=`dirname $0`
jar_dir=$here/../lib

if [ -z "$1" -o -z "$2" ] ; then
    echo usage: $0 xx YY
    echo "    " where xx is the language code and YY is the country code
    exit 1
fi

cd $jar_dir
java -classpath .:TranslateOMatic.jar:DataVision.jar \
	jimm.properties.TranslateOMatic $1 $2 $3
