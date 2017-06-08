package org.deviceconnect.codegen;


import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.models.Swagger;

import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public abstract class AbstractCodegenConfig extends DefaultCodegen implements DConnectCodegenConfig {

    protected Map<String, Swagger> profileSpecs;

    protected abstract String profileFileFolder();

    @Override
    public Map<String, Swagger> getProfileSpecs() {
        return this.profileSpecs;
    }

    @Override
    public void setProfileSpecs(final Map<String, Swagger> profileSpecs) {
        this.profileSpecs = profileSpecs;
    }

    protected static String toUpperCapital(final String str, final boolean onlyFirstChar) {
        StringBuffer buf = new StringBuffer(str.length());
        buf.append(str.substring(0, 1).toUpperCase());
        if (onlyFirstChar) {
            buf.append(str.substring(1).toLowerCase());
        } else {
            buf.append(str.substring(1));
        }
        return buf.toString();
    }

    protected static String toUpperCapital(final String str) {
        return toUpperCapital(str, true);
    }

    protected void generateProfile(final ProfileTemplate template, final Map<String, Object> properties) throws IOException {
        final CodegenConfig config = this;
        String templateFile = getFullTemplateFile(this, template.templateFile);
        Template tmpl = Mustache.compiler()
                .withLoader(new Mustache.TemplateLoader() {
                    @Override
                    public Reader getTemplate(String name) {
                        return getTemplateReader(getFullTemplateFile(config, name + ".mustache"));
                    }
                })
                .defaultValue("")
                .compile(readTemplate(templateFile));

        String outputFileName = profileFileFolder() + File.separator + template.outputFile;
        writeToFile(outputFileName, tmpl.execute(properties));
    }

    protected void writeFile(final String source, final File destination) throws IOException {
        if (destination.exists()) {
            throw new IOException("Profile Spec File is already created: " + destination.getAbsolutePath());
        }
        if (!destination.createNewFile()) {
            throw new IOException("Failed to create Profile Spec File: " + destination.getAbsolutePath());
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(destination);
            out.write(source.getBytes());
            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected File writeToFile(final String filename, final String contents) throws IOException {
        LOGGER.info("writing file " + filename);
        File output = new File(filename);

        if (output.getParent() != null && !new File(output.getParent()).exists()) {
            File parent = new File(output.getParent());
            parent.mkdirs();
        }
        Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(output), "UTF-8"));

        out.write(contents);
        out.close();
        return output;
    }

    private String readTemplate(final String name) {
        try {
            Reader reader = getTemplateReader(name);
            if (reader == null) {
                throw new RuntimeException("no file found");
            }
            Scanner s = new Scanner(reader).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        throw new RuntimeException("can't load template " + name);
    }

    private Reader getTemplateReader(final String name) {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(getCPResourcePath(name));
            if (is == null) {
                is = new FileInputStream(new File(name)); // May throw but never return a null value
            }
            return new InputStreamReader(is);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        throw new RuntimeException("can't load template " + name);
    }

    /**
     * Get the template file path with template dir prepended, and use the
     * library template if exists.
     *
     * @param config Codegen config
     * @param templateFile Template file
     * @return String Full template file path
     */
    private String getFullTemplateFile(final CodegenConfig config, final String templateFile) {
        String template = config.templateDir() + File.separator + templateFile;
        if (new File(template).exists()) {
            return template;
        } else {
            String library = config.getLibrary();
            if (library != null && !"".equals(library)) {
                String libTemplateFile = config.embeddedTemplateDir() + File.separator +
                        "libraries" + File.separator + library + File.separator +
                        templateFile;
                if (embeddedTemplateExists(libTemplateFile)) {
                    // Fall back to the template file embedded/packaged in the JAR file...
                    return libTemplateFile;
                }
            }
            // Fall back to the template file embedded/packaged in the JAR file...
            return config.embeddedTemplateDir() + File.separator + templateFile;
        }
    }

    private String readResourceContents(final String resourceFilePath) {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(this.getClass().getResourceAsStream(getCPResourcePath(resourceFilePath)), "UTF-8");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private boolean embeddedTemplateExists(final String name) {
        return this.getClass().getClassLoader().getResource(getCPResourcePath(name)) != null;
    }

    private String getCPResourcePath(final String name) {
        if (!"/".equals(File.separator)) {
            return name.replaceAll(Pattern.quote(File.separator), "/");
        }
        return name;
    }

}
