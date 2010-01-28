# Makefile for DataVision documentation, both HTML and Web site.
#
# Jim Menard, jimm@io.com
#
# The .PHONY directives are not necessary. If you make doesn't support
# them, delete them.

NAME = DataVision
DOC_DIR = xhtml
WEB_DIR = web
WEB_HTDOCS_DIR = $(WEB_DIR)/htdocs
WEB_VERSION_FILE = $(WEB_HTDOCS_DIR)/version.txt
WEB_TAR_FILE = /tmp/datavision-web.tar.gz

JAVADOC = javadoc
JCLASSPATH = -classpath ..:../lib/jruby.jar:../lib/jcalendar.jar:../lib/iText.jar:/usr/local/lib/junit.jar:../lib/bsf.jar
JAVADOC_DIR = javadoc
OVERVIEW = ../jimm/overview.html
JD = ../jimm/datavision
PACKAGES = `find $(JD) -type d | grep -v CVS | sed -e 's/^\.\.\///g' | sed -e 's/\//./g'`


.PHONY:	all
all:	html

# The in-program Swing HTML help does not understand XHTML, so we have
# to run scripts that turn the XHTML into HTML it can understand.
# Before doing that, we have to run the script that transalates our
# pseudo-XHTML into XHTML.
html:	DataVision faq DataVision/style.css faq/style.css
	./doc2xhtml.rb $(DOC_DIR)/DataVision tmp_DataVision
	./doc2xhtml.rb $(DOC_DIR)/faq tmp_faq
	./xhtml2html.rb tmp_DataVision DataVision
	./xhtml2html.rb tmp_faq faq
	rm -fr tmp_DataVision tmp_faq

DataVision faq:
	mkdir $@

DataVision/style.css faq/style.css:	style.css
	cp style.css $@

$(OVERVIEW):	DataVision/intro.html
	./copyToOverview.rb

# Javadocs
.PHONY:	javadoc
javadoc:	$(OVERVIEW)
	mkdir -p $(JAVADOC_DIR)
	$(JAVADOC) -package -d $(JAVADOC_DIR) $(JCLASSPATH) \
		-overview $(OVERVIEW) \
		-doctitle "$(NAME) v`../bin/versionNumber.rb` User's Manual" \
		-windowtitle "$(NAME) v`../bin/versionNumber.rb` User's Manual" \
		$(PACKAGES)

$(WEB_VERSION_FILE):	$(JD)/info.java
	../bin/versionNumber.rb -n >$(WEB_VERSION_FILE)

# Generate the Web site .tar.gz file. We copy the XHTML dirctories
# (expanding custom tags along the way) and move the javadoc directory.
# We also copy the "recent changes" section from DataVision/intro.html
# to the index page and copy the credits file into the htdocs directory.
.PHONY:	web
web:	release javadoc
	./doc2xhtml.rb $(DOC_DIR)/$(NAME) $(WEB_HTDOCS_DIR)/$(NAME)
	./doc2xhtml.rb $(DOC_DIR)/faq $(WEB_HTDOCS_DIR)/faq
	cp style.css $(WEB_HTDOCS_DIR)/$(NAME)
	cp style.css $(WEB_HTDOCS_DIR)/faq
	./preFilter.rb ../Credits $(WEB_HTDOCS_DIR)/Credits
	mv $(JAVADOC_DIR) $(WEB_HTDOCS_DIR)
	./copyToWeb.rb
	rm -fr /tmp/$(WEB_DIR)
	cp -r $(WEB_DIR) /tmp
	rm -fr `find /tmp/$(WEB_DIR) -type d -name CVS`
	rm -f `find /tmp/$(WEB_DIR) -name .cvsignore -o -name .DS_Store -o -name '*~'`
	cd /tmp/$(WEB_DIR) && tar -cf - * | gzip >$(WEB_TAR_FILE)
	rm -fr /tmp/$(WEB_DIR)

.PHONY:	web-no-javadoc
web-no-javadoc:	release
	./doc2xhtml.rb $(DOC_DIR)/$(NAME) $(WEB_HTDOCS_DIR)/$(NAME)
	./doc2xhtml.rb $(DOC_DIR)/faq $(WEB_HTDOCS_DIR)/faq
	cp style.css $(WEB_HTDOCS_DIR)/$(NAME)
	cp style.css $(WEB_HTDOCS_DIR)/faq
	./preFilter.rb ../Credits $(WEB_HTDOCS_DIR)/Credits
	rm -fr /tmp/$(WEB_DIR)
	cp -r $(WEB_DIR) /tmp
	rm -fr `find /tmp/$(WEB_DIR) -type d -name CVS`
	rm -f `find /tmp/$(WEB_DIR) -name .cvsignore -o -name .DS_Store -o -name '*~'`
	cd /tmp/$(WEB_DIR) && tar -cf - * | gzip >$(WEB_TAR_FILE)
	rm -fr /tmp/$(WEB_DIR)

# The release documentation is the HTML documentation.
.PHONY:	release
release:	$(WEB_VERSION_FILE) clean html
	$(MAKE) clean

.PHONY:	clean
clean:
	/bin/rm -fr $(WEB_HTDOCS_DIR)/$(NAME) $(WEB_HTDOCS_DIR)/faq \
		$(JAVADOC_DIR) $(WEB_HTDOCS_DIR)/$(JAVADOC_DIR) \
		tmp_$(NAME) tmp_faq $(WEB_HTDOCS_DIR)/Credits

.PHONY:	distclean
distclean:	clean
	/bin/rm -fr $(NAME) faq
