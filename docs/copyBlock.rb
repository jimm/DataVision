require 'cgi'

# This class is useful for copying a block of text from one file into
# another. It finds all text between two markers and puts the text between
# the same two markers in the output file (replacing anything there).

class CopyBlock

    # Read lines from infile inbetween begin_marker and end_marker
    # and write them to outfile.
    def run(infile, outfile, in_begin_marker, in_end_marker,
	    out_begin_marker=nil, out_end_marker=nil)
	write_block(outfile, read_block(infile, in_begin_marker,
					in_end_marker),
		    out_begin_marker || in_begin_marker,
		    out_end_marker || in_end_marker)
    end

    # Given a line of text, perform upon it all of the substutions in
    # xlations and return the line.
    def translate(line, xlations)
	line = CGI.escapeHTML(line)
	xlations.each { | regex, substitution |
	    line.gsub!(regex, substitution)
	}
	return line
    end

    # Open the LaTeX file and read the section between begin_marker and
    # end_marker. Save each line, translating it from LaTeX to HTML. Return
    # the lines as an array.
    def read_block(infile, begin_marker, end_marker)
	lines = Array.new()
	inside = false
	possibly_new_paragraph = false

	IO.foreach(infile) { | line |
	    case line
	    when begin_marker
		inside = true
		next
	    when end_marker
		inside = false
		next
	    end
	    
	    lines << line if inside
	}
	return lines
    end

    # Given an array of lines and two begin/end regexes, put the lines
    # between the corresponding begin/end lines in the output file.
    def write_block(out_file_name, lines, begin_marker, end_marker)
	File.open(out_file_name + '.tmp', 'w') { | f |
	    inside = false
	    IO.readlines(out_file_name).each { | line |
		case line
		when begin_marker
		    inside = true
		    f.print line
		    f.print lines
		when end_marker
		    inside = false
		end
		
		f.print line unless inside
	    }
	}
	File.rename(out_file_name + '.tmp', out_file_name)
    end

end
