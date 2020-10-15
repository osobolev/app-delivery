package apploader;

import java.util.function.Function;

final class Expander {

    private final String str;
    private final Function<String, String> expander;

    private final StringBuilder buf = new StringBuilder();
    private int i = 0;

    private Expander(String str, Function<String, String> expander) {
        this.str = str;
        this.expander = expander;
    }

    private int nextIdent() {
        i++;
        while (i < str.length()) {
            char ch = str.charAt(i);
            if (!Character.isJavaIdentifierPart(ch))
                break;
            i++;
        }
        return i;
    }

    private int nextBrackets() {
        i++;
        while (i < str.length()) {
            char ch = str.charAt(i);
            if (ch == '}') {
                int end = i;
                i++;
                return end;
            }
            i++;
        }
        return str.length();
    }

    private void expand(int start, int identStart, int identEnd) {
        String id = str.substring(identStart, identEnd);
        String expandTo = expander.apply(id.trim());
        if (expandTo == null) {
            buf.append(str, start, i);
        } else {
            buf.append(expandTo);
        }
    }

    private String parse() {
        while (i < str.length()) {
            int start = i;
            char ch = str.charAt(i++);
            if (ch == '$') {
                if (i < str.length()) {
                    char ch2 = str.charAt(i);
                    if (ch2 == '$') {
                        i++;
                    } else if (ch2 == '{') {
                        int identStart = i + 1;
                        int identEnd = nextBrackets();
                        expand(start, identStart, identEnd);
                        continue;
                    } else if (Character.isJavaIdentifierStart(ch2)) {
                        int identStart = i;
                        int identEnd = nextIdent();
                        expand(start, identStart, identEnd);
                        continue;
                    }
                }
            }
            buf.append(ch);
        }
        return buf.toString();
    }

    static String expand(String str, Function<String, String> expander) {
        return new Expander(str, expander).parse();
    }
}