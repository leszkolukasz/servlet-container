package servletcontainer.jsp;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSPTranspiler {
    final private static Pattern declarationTag = Pattern.compile("<%!([\\s\\S]*?)%>");
    final private static Pattern scripletTag = Pattern.compile("<%([\\s\\S]*?)%>");
    final private static Pattern expressionTag = Pattern.compile("<%=([\\s\\S]*?)%>");
    final private static Pattern commentTag = Pattern.compile("<%--([\\s\\S]*?)--%>");
    final private static Pattern directiveTag = Pattern.compile("<%@([\\s\\S]*?)%>");
    final private static Pattern importTag = Pattern.compile("import\\s*=\\s*\"([\\s\\S]+)\"");
    final private static Pattern includeTag = Pattern.compile("include\\s*file\\s*=\\s*\"(\\S+)\"");
    final private static Pattern segmentInEL = Pattern.compile("([a-zA-Z._\\s]+)");
    final private JavaCompiler compiler;
    private final List<String> definitions;
    private final List<String> imports;
    private final List<String> content;

    public JSPTranspiler() {
        compiler = ToolProvider.getSystemJavaCompiler();
        definitions = new ArrayList<>();
        imports = new ArrayList<>();
        content = new ArrayList<>();
    }

    private static String escapeString(String str) {
        StringBuilder builder = new StringBuilder();

        for (char c : str.toCharArray()) {
            switch (c) {
                case '\"':
                    builder.append("\\\"");
                    break;
                case '\'':
                    builder.append("\\'");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.toString();
    }

    // Access property with getter. Getter is called using Java Reflection.
    public static String wrapWithPropertyAccessor(String obj, String property) {
        var accessor = convertToPropertyAccessor(property);
        return escapeString(obj) + ".getClass().getMethod(\"" + accessor + "\").invoke(" + escapeString(obj) + ")";
    }

    public static String convertToPropertyAccessor(String propertyName) {
        StringBuilder sb = new StringBuilder();
        sb.append("get");

        if (propertyName.length() > 0) {
            sb.append(Character.toUpperCase(propertyName.charAt(0)));
            if (propertyName.length() > 1) {
                sb.append(propertyName.substring(1));
            }
        }

        return sb.toString();
    }

    public synchronized void compile(File appDir, File file, String servletUrl) {
        String javaSource = transpile(file, servletUrl);

        URI sourceUri = URI.create(file.getName().replace(".jsp", ".java"));
        JavaFileObject sourceFile = new JavaStringSource(sourceUri, javaSource);

        List<String> options = new ArrayList<>();
        options.add("-d");
        options.add(appDir.getAbsolutePath());
        options.add("-cp");

        String classpaths = "src/main/java:" +
                "src/main/java:" +
                "src/main/resources:" +
                "src/main/resources/servlet-api.jar:" +
                appDir.getAbsolutePath() + ":" +
                Paths.get(appDir.getAbsolutePath(), "WEB-INF") + ":" +
                Paths.get(appDir.getAbsolutePath(), "WEB-INF", "classes") + ":" +
                Paths.get(appDir.getAbsolutePath(), "WEB-INF", "lib") + ":";

        options.add(classpaths);

        Boolean ok = compiler.getTask(null, null, null, options,
                null, List.of(sourceFile)).call();

        if (!ok)
            throw new RuntimeException("JSP compilation failed.");
    }

    private String transpile(File file, String servlerUrl) {
        clear();
        parse(file);
        return buildString(file, servlerUrl);
    }

    private void parse(File file) {
        try {
            String jspCode = Files.readString(file.toPath());

            while (true) {
                int tagStart = jspCode.indexOf("<%");

                if (tagStart == -1) {
                    findExpressionLanguageAndParse(jspCode);
                    break;
                }

                if (tagStart != 0) {
                    findExpressionLanguageAndParse(jspCode.substring(0, tagStart));
                    jspCode = jspCode.substring(tagStart);
                    continue;
                }

                Matcher declarationMatcher = declarationTag.matcher(jspCode);
                Matcher scripletMatcher = scripletTag.matcher(jspCode);
                Matcher commentMatcher = commentTag.matcher(jspCode);
                Matcher directiveMatcher = directiveTag.matcher(jspCode);
                Matcher expressionMatcher = expressionTag.matcher(jspCode);

                if (declarationMatcher.find() && declarationMatcher.start() == 0) {
                    handleDeclarationTag(declarationMatcher.group(1));
                    jspCode = jspCode.substring(declarationMatcher.end());
                } else if (commentMatcher.find() && commentMatcher.start() == 0) {
                    handleCommentTag(commentMatcher.group(1));
                    jspCode = jspCode.substring(commentMatcher.end());
                } else if (directiveMatcher.find() && directiveMatcher.start() == 0) {
                    handleDirectiveTag(directiveMatcher.group(1));
                    jspCode = jspCode.substring(directiveMatcher.end());
                } else if (expressionMatcher.find() && expressionMatcher.start() == 0) {
                    handleExpressionTag(expressionMatcher.group(1));
                    jspCode = jspCode.substring(expressionMatcher.end());
                }
                // Must be last.
                else if (scripletMatcher.find() && scripletMatcher.start() == 0) {
                    handleScripletTag(scripletMatcher.group(1));
                    jspCode = jspCode.substring(scripletMatcher.end() + 1);
                } else {
                    throw new RuntimeException("Invalid syntax in: " + jspCode);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findExpressionLanguageAndParse(String body) {
        while (!body.isEmpty()) {
            int ELstart = body.indexOf("${");

            if (ELstart == -1) {
                content.add("out.print(\"" + escapeString(body) + "\");");
                break;
            }

            int ELend = body.indexOf("}", ELstart);

            if (ELend == -1) {
                content.add("out.print(\"" + escapeString(body) + "\");");
                break;
            }

            if (ELstart != 0) {
                content.add("out.print(\"" + escapeString(body.substring(0, ELstart)) + "\");");
                body = body.substring(ELstart);
            }

            String EL = body.substring(0, ELend - ELstart);
            EL = EL.replace("${", "");

            handleELTag(EL.trim());

            body = body.substring(ELend - ELstart + 1);
        }
    }

    private void handleELTag(String body) {
        Matcher matcher = segmentInEL.matcher(body);

        // Tries to extract segments from string. In:
        // "10" + 10 + x + x.foo + y.foo.goo
        // segments are: x, x.foo, y.foo.goo
        // Then changes segments into request.getAttribute(...).
        String newBody = matcher.replaceAll(match -> {
            String text = match.group(1);

            // Skip if text is surrounded by ""
            if (matcher.start(1) != 0 && body.charAt(matcher.start(1) - 1) == '\"')
                return text;

            String textStripped = text.strip();

            if (textStripped.length() == 0)
                return text;

            var parts = textStripped.split("\\.");
            var output = "request.getAttribute(\"" + parts[0] + "\")";

            for (int i = 1; i < parts.length; i++)
                output = wrapWithPropertyAccessor(output, parts[i]);

            return output;
        });

        handleExpressionTag(newBody);
    }

    private void handleDeclarationTag(String body) {
        definitions.add(body);
    }

    private void handleScripletTag(String body) {
        content.add(body);
    }

    private void handleCommentTag(String body) {
        content.add("/* " + body + " */");
    }

    private void handleDirectiveTag(String body) {
        Matcher matcher = importTag.matcher(body);
        if (matcher.find()) {
            String[] importedClasses = matcher.group(1).split(", ");
            for (var c : importedClasses)
                imports.add("import " + c + ";");
        }

        matcher = includeTag.matcher(body);
        if (!matcher.find())
            return;

        content.add("request.getRequestDispatcher(\"" + matcher.group(1) + "\").include(request, response);");
    }

    private void handleExpressionTag(String body) {
        content.add("out.print(" + body + ");");
    }


    // Returns .java file code as string.
    private String buildString(File file, String servlerUrl) {
        String fileName = file.getName().replace(".jsp", "");
        StringBuilder code = new StringBuilder();

        code.append("import java.io.IOException;\n");
        code.append("import javax.servlet.annotation.*;\n");
        code.append("import javax.servlet.http.*;\n");
        code.append("import javax.servlet.*;\n");

        for (String i : imports)
            code.append(i + "\n");

        code.append("@WebServlet(value=\"" + servlerUrl + "\")\n");
        code.append("public class " + fileName + " extends HttpServlet {\n");

        for (String def : definitions)
            code.append(def + "\n");

        code.append("""
                    @Override
                    public void doGet(HttpServletRequest request, HttpServletResponse response) {
                        try {
                            var out = response.getWriter();
                """);

        for (String c : content)
            code.append(c + "\n");

        code.append("""
                        } catch (Exception e) {
                                    e.printStackTrace();
                        }
                    }
                }
                """);

        return code.toString();
    }

    private void clear() {
        definitions.clear();
        imports.clear();
        content.clear();
    }

    private static class JavaStringSource extends SimpleJavaFileObject {
        private final String code;

        public JavaStringSource(URI uri, String code) {
            super(uri, Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
