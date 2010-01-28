cls
call javac -classpath ../lib/datavision.jar ObjectSourceTest.java
java -cp .;..lib/liquidlnf.jar;../lib/datavision.jar;../lib/commons-logging.jar;../lib/asm.jar;../lib/bsf.jar;../lib/jruby.jar ObjectSourceTest
