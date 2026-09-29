package com.poorfarmer.core;

import java.util.ArrayList;
import java.util.List;

public final class GlslValidator {

  public static final String VERSION_PRAGMA = "#version 300 es";

  public static List<String> validate(String source) {
    List<String> problems = new ArrayList<>();
    if (source == null || source.trim().isEmpty()) {
      problems.add("empty shader source");
      return problems;
    }
    String firstLine = source.trim().split("\n", 2)[0].trim();
    if (!firstLine.equals(VERSION_PRAGMA)) {
      problems.add("first line must be '#version 300 es'");
    }
    if (!containsMainFunction(source)) {
      problems.add("missing main() function");
    }
    int depth = 0;
    for (int i = 0; i < source.length(); i++) {
      char ch = source.charAt(i);
      if (ch == '{') {
        depth++;
      } else if (ch == '}') {
        depth--;
        if (depth < 0) {
          problems.add("unbalanced closing brace at char " + i);
          break;
        }
      }
    }
    if (depth > 0) {
      problems.add("unbalanced braces, missing " + depth + " closing");
    }
    if (depth == 0 && countOf(source, '(') != countOf(source, ')')) {
      problems.add("unbalanced parentheses");
    }
    return problems;
  }

  public static boolean isValid(String source) {
    return validate(source).isEmpty();
  }

  private static boolean containsMainFunction(String source) {
    for (int i = 0; i + 4 <= source.length(); i++) {
      if (!source.startsWith("main", i)) {
        continue;
      }
      boolean wordStart = i == 0 || !Character.isLetterOrDigit(source.charAt(i - 1));
      if (!wordStart) {
        continue;
      }
      int next = i + 4;
      if (next < source.length() && Character.isLetterOrDigit(source.charAt(next))) {
        continue;
      }
      while (next < source.length() && Character.isWhitespace(source.charAt(next))) {
        next++;
      }
      if (next < source.length() && source.charAt(next) == '(') {
        return true;
      }
    }
    return false;
  }

  private static int countOf(String text, char target) {
    int count = 0;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == target) {
        count++;
      }
    }
    return count;
  }

  private GlslValidator() {
  }
}
