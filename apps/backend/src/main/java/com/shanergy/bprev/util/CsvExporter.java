package com.shanergy.bprev.util;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvExporter {

    private CsvExporter() {
    }

    public static byte[] toCsv(List<? extends List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        boolean firstRow = true;
        for (List<String> row : rows) {
            if (!firstRow) {
                sb.append('\n');
            }
            firstRow = false;
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                appendValue(sb, i < row.size() ? row.get(i) : "");
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void appendValue(StringBuilder sb, String value) {
        String v = value == null ? "" : value;
        boolean needsQuotes = v.contains(",") || v.contains("\n") || v.contains("\r") || v.contains("\"");
        if (!needsQuotes) {
            sb.append(v);
            return;
        }
        sb.append('"');
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (c == '"') {
                sb.append('"').append('"');
            } else {
                sb.append(c);
            }
        }
        sb.append('"');
    }
}
