package com.poorfarmer.core.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {

  private Json() {
  }

  public static final class JsonException extends RuntimeException {
    public JsonException(String message) {
      super(message);
    }
  }

  public enum Type {
    NULL,
    BOOLEAN,
    NUMBER,
    STRING,
    ARRAY,
    OBJECT
  }

  public static final class Value {

    public final Type type;
    public Boolean bool;
    public double num;
    public String str;
    public final List<Value> items;
    public final Map<String, Value> members;

    private Value(Type type) {
      this.type = type;
      this.items = null;
      this.members = null;
    }

    private Value(Type type, List<Value> items, Map<String, Value> members) {
      this.type = type;
      this.items = items;
      this.members = members;
    }

    public static Value nullValue() {
      return new Value(Type.NULL);
    }

    public static Value boolValue(boolean value) {
      Value v = new Value(Type.BOOLEAN);
      v.bool = value;
      return v;
    }

    public static Value numberValue(double value) {
      Value v = new Value(Type.NUMBER);
      v.num = value;
      return v;
    }

    public static Value stringValue(String value) {
      Value v = new Value(Type.STRING);
      v.str = value;
      return v;
    }

    public static Value arrayValue() {
      return new Value(Type.ARRAY, new ArrayList<Value>(), null);
    }

    public static Value objectValue() {
      return new Value(Type.OBJECT, null, new LinkedHashMap<String, Value>());
    }

    public Value set(String key, Value value) {
      require(Type.OBJECT);
      members.put(key, value);
      return this;
    }

    public Value add(Value value) {
      require(Type.ARRAY);
      items.add(value);
      return this;
    }

    public boolean has(String key) {
      return type == Type.OBJECT && members.containsKey(key);
    }

    public Value get(String key) {
      if (type != Type.OBJECT) {
        return null;
      }
      return members.get(key);
    }

    public boolean isNull() {
      return type == Type.NULL;
    }

    public boolean asBoolean() {
      require(Type.BOOLEAN);
      return bool;
    }

    public double asNumber() {
      require(Type.NUMBER);
      return num;
    }

    public String asString() {
      require(Type.STRING);
      return str;
    }

    public int asInt() {
      require(Type.NUMBER);
      return (int) num;
    }

    private void require(Type expected) {
      if (type != expected) {
        throw new JsonException("expected " + expected + " but was " + type);
      }
    }

    @Override
    public String toString() {
      return Json.stringify(this);
    }
  }

  public static String stringify(Value value) {
    StringBuilder out = new StringBuilder(128);
    write(value, out);
    return out.toString();
  }

  private static void write(Value value, StringBuilder out) {
    switch (value.type) {
      case NULL:
        out.append("null");
        break;
      case BOOLEAN:
        out.append(value.bool ? "true" : "false");
        break;
      case NUMBER:
        if (Double.isNaN(value.num) || Double.isInfinite(value.num)) {
          throw new JsonException("NaN and Infinity are not valid JSON numbers");
        }
        out.append(Double.toString(value.num));
        break;
      case STRING:
        writeString(value.str, out);
        break;
      case ARRAY:
        out.append('[');
        for (int i = 0; i < value.items.size(); i++) {
          if (i > 0) {
            out.append(',');
          }
          write(value.items.get(i), out);
        }
        out.append(']');
        break;
      case OBJECT:
        out.append('{');
        boolean first = true;
        for (Map.Entry<String, Value> entry : value.members.entrySet()) {
          if (!first) {
            out.append(',');
          }
          first = false;
          writeString(entry.getKey(), out);
          out.append(':');
          write(entry.getValue(), out);
        }
        out.append('}');
        break;
      default:
        throw new JsonException("unknown value type " + value.type);
    }
  }

  private static void writeString(String s, StringBuilder out) {
    out.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"':
          out.append("\\\"");
          break;
        case '\\':
          out.append("\\\\");
          break;
        case '\n':
          out.append("\\n");
          break;
        case '\r':
          out.append("\\r");
          break;
        case '\t':
          out.append("\\t");
          break;
        case '\b':
          out.append("\\b");
          break;
        case '\f':
          out.append("\\f");
          break;
        default:
          if (c < 0x20) {
            out.append(String.format("\\u%04x", (int) c));
          } else {
            out.append(c);
          }
      }
    }
    out.append('"');
  }

  public static Value parse(String text) {
    if (text == null) {
      throw new JsonException("null input");
    }
    Parser parser = new Parser(text);
    Value value = parser.parseValue();
    parser.skipWhitespace();
    if (parser.pos < parser.text.length()) {
      throw new JsonException("trailing characters at " + parser.pos);
    }
    return value;
  }

  private static final class Parser {

    private final String text;
    private int pos;

    Parser(String text) {
      this.text = text;
    }

    Value parseValue() {
      skipWhitespace();
      if (pos >= text.length()) {
        throw new JsonException("unexpected end of input");
      }
      char c = text.charAt(pos);
      if (c == '{') {
        return parseObject();
      }
      if (c == '[') {
        return parseArray();
      }
      if (c == '"') {
        return Value.stringValue(parseString());
      }
      if (c == 't' || c == 'f') {
        return parseBoolean();
      }
      if (c == 'n') {
        parseLiteral("null");
        return Value.nullValue();
      }
      return parseNumber();
    }

    private Value parseObject() {
      Value object = Value.objectValue();
      pos++;
      skipWhitespace();
      if (peek() == '}') {
        pos++;
        return object;
      }
      while (true) {
        skipWhitespace();
        String key = parseString();
        skipWhitespace();
        if (peek() != ':') {
          throw new JsonException("expected : at " + pos);
        }
        pos++;
        Value value = parseValue();
        object.members.put(key, value);
        skipWhitespace();
        char c = peek();
        if (c == ',') {
          pos++;
        } else if (c == '}') {
          pos++;
          return object;
        } else {
          throw new JsonException("expected , or } at " + pos);
        }
      }
    }

    private Value parseArray() {
      Value array = Value.arrayValue();
      pos++;
      skipWhitespace();
      if (peek() == ']') {
        pos++;
        return array;
      }
      while (true) {
        Value value = parseValue();
        array.items.add(value);
        skipWhitespace();
        char c = peek();
        if (c == ',') {
          pos++;
        } else if (c == ']') {
          pos++;
          return array;
        } else {
          throw new JsonException("expected , or ] at " + pos);
        }
      }
    }

    private String parseString() {
      if (peek() != '"') {
        throw new JsonException("expected string at " + pos);
      }
      pos++;
      StringBuilder out = new StringBuilder(32);
      while (true) {
        if (pos >= text.length()) {
          throw new JsonException("unterminated string");
        }
        char c = text.charAt(pos++);
        if (c == '"') {
          return out.toString();
        }
        if (c == '\\') {
          if (pos >= text.length()) {
            throw new JsonException("unterminated escape");
          }
          char esc = text.charAt(pos++);
          switch (esc) {
            case '"':
              out.append('"');
              break;
            case '\\':
              out.append('\\');
              break;
            case '/':
              out.append('/');
              break;
            case 'n':
              out.append('\n');
              break;
            case 'r':
              out.append('\r');
              break;
            case 't':
              out.append('\t');
              break;
            case 'b':
              out.append('\b');
              break;
            case 'f':
              out.append('\f');
              break;
            case 'u':
              if (pos + 4 > text.length()) {
                throw new JsonException("truncated unicode escape");
              }
              String hex = text.substring(pos, pos + 4);
              try {
                out.append((char) Integer.parseInt(hex, 16));
              } catch (NumberFormatException error) {
                throw new JsonException("invalid unicode escape " + hex);
              }
              pos += 4;
              break;
            default:
              throw new JsonException("invalid escape \\" + esc);
          }
        } else {
          out.append(c);
        }
      }
    }

    private Value parseBoolean() {
      if (text.startsWith("true", pos)) {
        pos += 4;
        return Value.boolValue(true);
      }
      if (text.startsWith("false", pos)) {
        pos += 5;
        return Value.boolValue(false);
      }
      throw new JsonException("invalid literal at " + pos);
    }

    private void parseLiteral(String literal) {
      if (!text.startsWith(literal, pos)) {
        throw new JsonException("invalid literal at " + pos);
      }
      pos += literal.length();
    }

    private Value parseNumber() {
      int start = pos;
      if (peek() == '-') {
        pos++;
      }
      while (pos < text.length() && isNumberChar(text.charAt(pos))) {
        pos++;
      }
      if (start == pos) {
        throw new JsonException("invalid number at " + start);
      }
      String token = text.substring(start, pos);
      int firstDigit = token.charAt(0) == '-' ? 1 : 0;
      if (token.length() > firstDigit + 1
          && token.charAt(firstDigit) == '0'
          && Character.isDigit(token.charAt(firstDigit + 1))) {
        throw new JsonException("leading zeros are not valid JSON numbers at " + start);
      }
      try {
        return Value.numberValue(Double.parseDouble(token));
      } catch (NumberFormatException error) {
        throw new JsonException("invalid number at " + start);
      }
    }

    private static boolean isNumberChar(char c) {
      return (c >= '0' && c <= '9') || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-';
    }

    private void skipWhitespace() {
      while (pos < text.length()) {
        char c = text.charAt(pos);
        if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
          pos++;
        } else {
          break;
        }
      }
    }

    private char peek() {
      if (pos >= text.length()) {
        throw new JsonException("unexpected end of input");
      }
      return text.charAt(pos);
    }
  }
}
