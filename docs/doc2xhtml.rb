#! /usr/bin/env ruby
#
# usage: doc2xhtml.rb in_dir out_dir
#
# Expands short tags in pseudo-XHTML into full span or other tags. For
# example, turns "<file>foo</file>" into '<span class="file">foo</span>". Also
# expandes include_header and include_footer comments and turns "``" and "''"
# into "&quot;". The special comment "para-gen" turns generation of "<p>" and
# "</p>" on or off. It's on by default.
#
# This script prints an error message and exist with an error status of 1
# if the same file is found to contain two link ids that are the same (a
# common mistake when I cut and paste).

BASE_DIR = File.dirname($0)
STYLE_FILE = File.join(BASE_DIR, 'style.css')
META_FILE = File.join(BASE_DIR, 'xhtml', 'meta.html')
HEADER_FILE = File.join(BASE_DIR, 'xhtml', 'header.html')
FOOTER_FILE = File.join(BASE_DIR, 'xhtml', 'footer.html')

class TableOfContents
    def initialize(out)
	@out = out		# We don't own this; don't close it
	@level_stack = [0]	# index 0 never used
    end
    def add_header(fname, link_id, level, title)
	level = level.to_i
	if level >= @level_stack.length
	    @level_stack << 1
	elsif level < (@level_stack.length - 1)
	    @level_stack[level+1 .. -1] = nil
	    @level_stack[level] += 1
	else
	    @level_stack[level] += 1
	end
	output_header(fname, link_id, title)
    end
    def level_string
	return @level_stack[1 .. -1].join('.')
    end
    def output_header(fname, link_id, title)
	depth = @level_stack.length - 1
	is_top_level = depth == 1
	@out.print "\n<br />" if is_top_level
	indent(depth)
	@out.print "<a href=\"#{fname}"
	@out.print "##{link_id}" if link_id
	@out.print "\">"
	@out.print '<span class="toc-chapter">' if is_top_level
	@out.print "#{level_string()} #{title}"
	@out.print '</span>' if is_top_level
	@out.puts "</a><br />"
    end
    def indent(depth)
	@out.print '&nbsp;' * ((depth - 1) * 4)
    end
end

class Tag
    attr_reader :tag, :open, :close
    def initialize(tag, open, close)
	@tag = tag
	@open = open
	@close = close
    end
end

class TagExpander

    def initialize
	read_tags
    end

    def read_tags
	@tags = Array.new
	IO.readlines(STYLE_FILE).each { | line |
	    line.chomp!
	    if line =~ /^(\w+)\.([-\w]+)\s*\{/
		@tags << Tag.new($2, "<#$1 class=\"#$2\">", "</#$1>")
	    end
	}
    end

    def expand(in_dir, out_dir)
	Dir.mkdir(out_dir) unless File.exist?(out_dir)
	
	expand_toc(File.join(in_dir, File.basename(in_dir) + '.html'),
		   out_dir)
    end

    def expand_toc(toc_file, out_dir)
	expand_file(toc_file, File.join(out_dir, File.basename(toc_file)),
		    true)
    end

    def expand_file(in_file, out_file, is_toc_file=false)
	prev_is_blank = false
	prev_tag = nil
	prev_para_start_tag = nil
	skip_para = false
	inside_pre = false
	link_id = nil
	seen_ids = []

	File.open(out_file, 'w') { | out |
	    @toc = TableOfContents.new(out) if is_toc_file
	    IO.readlines(in_file).each { | line |
		case line
		when /\<!--\s*generate\s+(\w+)\s*--\>/
		    fname = $1 + '.html'
		    expand_file(File.join(File.dirname(in_file), fname),
				File.join(File.dirname(out_file), fname))
		    next
		when /\<a\s+id\s*=\s*\"(.*?)\"/
		    link_id = $1
		    if seen_ids.include?link_id
			$stderr.puts "error: id \"#{link_id}\" seen twice" +
			    " in " + in_file
			$stderr.flush
			exit(1)
		    end
		    seen_ids << link_id
		when /\<h(\d+)\>(.*?)\<\/h\d+\>/
		    if !is_toc_file
			level, title = $1, $2
			title.gsub!(/<(\/?[-\w]+)>/) { expand_tag($1) }
			title.gsub!(/(``|'')/, "&quot;")

			@toc.add_header(File.basename(out_file), link_id,
					level, title)

			line = "<h#{level}>#{@toc.level_string()} #{title}" +
			    "</h#{level}>"
		    end
		when /\<!--\s*include_header\s+(\w+)\s+(\w+)\s+(\w+)\s+(\w+)(\s+(\w+))?\s*--\>/
		    include_header(out, $1, $2, $3, $4, $6)
		    prev_tag = 'include_header'
		    next
		when /\<!--\s*include_footer\s+(\w+)\s+(\w+)\s+(\w+)\s+(\w+)\s*--\>/
		    include_footer(out, $1, $2, $3, $4)
		    prev_tag = 'include_footer'
		    next
		when /\<!--\s*para-gen\s+(\w+)\s*-->/
		    skip_para = $1 == 'off'
		    next
		end

		line.gsub!(/<(\/?[-\w]+)>/) { expand_tag($1) }
		line.gsub!(/(``|'')/, "&quot;")

		case line
		when /<pre/
		    inside_pre = true
		when /<\/pre/
		    inside_pre = false
		end

		this_is_blank = line =~ /^\s*(\<!--.*?--\>)?\s*$/

		if prev_is_blank
		    # Start new paragraph if this is a non-blank line and
		    # if the current non-blank line does not start
		    # with certain tags.
		    if !this_is_blank
			if !skip_para && !inside_pre &&
				line !~ /^\s*\<\/?(ul|dl|dt|dd|ol|li|h[0-9]|center|pre|table|td|tr|th|head|body|html|hr|blockquote|a id=).*?\>/
			    out.puts "<p>"
			end
			line =~ /^\s*\<\/?(\w+)/
			prev_para_start_tag = $1 # May be nil
		    end
		else		# Prev line not blank
		    # End previous paragraph if this is a blank line and
		    # if the previous non-blank line did not end with
		    # certain tags.
		    if this_is_blank && !skip_para && !inside_pre &&
			    prev_tag !~ /(ul|dl|dt|dd|ol|li|h[0-9]|center|pre|table|td|tr|th|head|body|html|hr|include_header|blockquote)/ &&
			    prev_para_start_tag != 'li'
			out.puts "</p>"
		    end
		end

		prev_is_blank = this_is_blank

		if !prev_is_blank # Remember tag at line end, if any
		    line =~ /\<\/?(\w+)[^\>]*?\>\s*$/
		    prev_tag = $1 # May be nil
		end

		out.print line
	    }
	}
    end

    def expand_tag(tag_text)
	close = false
	if tag_text[0] == ?/
	    close = true
	    tag_text = tag_text[1 .. -1]
	end
	tag = @tags.detect { | t | t.tag == tag_text }
	return tag == nil ? "<#{close ? '/' : ''}#{tag_text}>" : (close ? tag.close : tag.open)
    end

    def include_header(out, which, prev_page, next_page, toc_page, meta_flag)
	prev_link = prev_page == 'nil' ? '&lt;= Previous' :
	    "<a href=\"#{prev_page}.html\">&lt;= Previous</a>"
	next_link = next_page == 'nil' ? 'Next =&gt;' :
	    "<a href=\"#{next_page}.html\">Next =&gt;</a>"
	toc_link = toc_page == 'nil' ? 'Table of Contents' :
	    "<a href=\"#{toc_page}.html\">Table of Contents</a>"
	which_str = which == 'UM' ? 'User\'s Manual' : 'FAQ'
	meta_tag = meta_flag ? IO.readlines(META_FILE).join("\n") : ''

	IO.readlines(HEADER_FILE).each { | line |
	    out.print eval('"' + line.gsub('"', '\'') + '"')
	}
    end


    def include_footer(out, which, prev_page, next_page, toc_page)
	prev_link = prev_page == 'nil' ? '&lt;= Previous' :
	    "<a href=\"#{prev_page}.html\">&lt;= Previous</a>"
	next_link = next_page == 'nil' ? 'Next =&gt;' :
	    "<a href=\"#{next_page}.html\">Next =&gt;</a>"
	toc_link = toc_page == 'nil' ? 'Table of Contents' :
	    "<a href=\"#{toc_page}.html\">Table of Contents</a>"
	which_str = which == 'UM' ? 'User\'s Manual' : 'FAQ'

	IO.readlines(FOOTER_FILE).each { | line |
	    out.print eval('"' + line.gsub('"', '\'') + '"')
	}
    end
end

# ================================================================

in_dir = ARGV.shift
out_dir = ARGV.shift
TagExpander.new.expand(in_dir, out_dir)
