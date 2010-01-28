#!/bin/sh
# datavision.sh -- script to DataVision under Linux/Unix with an 
#                  appropriate classpath.  If first parameter is -v, 
#                  will output java command line before running it.

# Find out the directory where this script is,
# so we can find the jar files underneath it.
scriptpath=`dirname $0`

# dvclasspath must contain paths to all needed classes for DV,
# separated by colons.    All .jar files under lib/
# below the directory the .sh file is in are automatically added.
# If you need others, either drop them in that directory, or add 
# them here manually (dropping them in the directory is better!).
dvclasspath=""
for l in ${scriptpath}/lib/*.jar ; do
    dvclasspath=$dvclasspath:$l
done

# Output what we will execute, for debugging, if we have a -v switch
if [ "$1" = "-v" ] ; then
    shift
    echo java -classpath "$dvclasspath" -Dswing.aatext=true jimm.datavision.DataVision $*
fi

# Run the relevant Java code with the appropriate classpath
java -classpath "$dvclasspath" -Dswing.aatext=true jimm.datavision.DataVision $*
