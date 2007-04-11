package com.cadrie.hibernate.spatial.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyFileReader {

    private static final Log log = LogFactory.getLog(PropertyFileReader.class);

    /**
     * pattern for comment lines. If it matches, it is a comment.
     */
    private static final Pattern nonCommentPattern = Pattern
            .compile("^([^#]+)");

    private InputStream is = null;

    public PropertyFileReader(InputStream is) {
        this.is = is;
    }

    public Properties getProperties() throws IOException {
        if (is == null)
            return null;
        Properties props = new Properties();
        props.load(is);
        return props;
    }

    /**
     * Returns the non-comment lines in a file.
     * 
     * @return set of non-comment strings.
     * @throws IOException
     */
    public Set<String> getNonCommentLines() throws IOException {
        Set<String> lines = new HashSet<String>();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            Matcher m = nonCommentPattern.matcher(line);
            if (m.find()) {
                lines.add(m.group().trim());
            }
        }
        return lines;
    }

    public void close() {
        try {
            this.is.close();
        } catch (IOException e) {
            log.warn("Exception when closing PropertyFileReader: " + e);
        }
    }

}
