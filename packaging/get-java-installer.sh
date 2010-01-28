

JAVAURL=http://javadl.sun.com/webapps/download/AutoDL?BundleId=23111
JAVAFILE=jre-6u7-windows-i586-p-s.exe

[ -f $JAVAFILE ] || wget -O $JAVAFILE $JAVAURL
