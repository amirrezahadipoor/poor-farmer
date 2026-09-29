package com.poorfarmer.core.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.poorfarmer.core.json.Json.Value;
import org.junit.Test;

public class JsonTest {

  @Test
  public void parsePrimitives() {
    assertEquals(true, Json.parse("true").asBoolean());
    assertEquals(false, Json.parse("false").asBoolean());
    assertEquals(42.0, Json.parse("42").asNumber(), 0f);
    assertEquals(-1.5, Json.parse("-1.5").asNumber(), 0f);
    assertEquals("hi", Json.parse("\"hi\"").asString());
    assertTrue(Json.parse("null").isNull());
  }

  @Test
  public void parseObjectAndArray() {
    Value root = Json.parse("{\"a\":[1,2,{\"b\":null}],\"s\":\"x\"}");
    assertEquals(3, root.get("a").items.size());
    assertEquals(2.0, root.get("a").items.get(1).asNumber(), 0f);
    assertTrue(root.get("a").items.get(2).get("b").isNull());
    assertEquals("x", root.get("s").asString());
    assertTrue(root.has("a"));
    assertFalse(root.has("missing"));
    assertNull(root.get("missing"));
  }

  @Test
  public void stringEscapesRoundTrip() {
    String original = "line1\nline2\t\"quoted\" \\ unicode: أ ب ٪";
    Value value = Value.stringValue(original);
    String serialized = Json.stringify(value);
    assertEquals(original, Json.parse(serialized).asString());
  }

  @Test
  public void unicodeEscapeParses() {
    assertEquals("٪", Json.parse("\"\\u066a\"").asString());
    assertEquals("A", Json.parse("\"\\u0041\"").asString());
  }

  @Test
  public void objectRoundTripPreservesKeyOrder() {
    Value root = Value.objectValue();
    root.set("z", Value.numberValue(1));
    root.set("a", Value.numberValue(2));
    root.set("m", Value.stringValue("x"));
    String serialized = Json.stringify(root);
    Value parsed = Json.parse(serialized);
    assertEquals("z", parsed.members.keySet().iterator().next());
    assertEquals(2.0, parsed.get("a").asNumber(), 0f);
  }

  @Test
  public void whitespaceTolerated() {
    Value root = Json.parse(" { \"a\" : [ 1 , 2 ] } ");
    assertEquals(2, root.get("a").items.size());
  }

  @Test
  public void malformedInputThrows() {
    String[] bad = {"", "   ", "{", "[1,}", "{\"a\" 1}", "[1 2]", "\"abc", "tru", "01", "{\"a\":}"};
    for (String input : bad) {
      try {
        Json.parse(input);
        fail("expected failure for: " + input);
      } catch (Json.JsonException expected) {
      }
    }
  }

  @Test
  public void trailingCharactersRejected() {
    try {
      Json.parse("1 2");
      fail("expected trailing rejection");
    } catch (Json.JsonException expected) {
    }
  }

  @Test
  public void nanRejectedInSerializer() {
    try {
      Json.stringify(Value.numberValue(Double.NaN));
      fail("expected NaN rejection");
    } catch (Json.JsonException expected) {
    }
  }

  @Test
  public void typeMismatchThrows() {
    try {
      Json.parse("1").asString();
      fail("expected type mismatch");
    } catch (Json.JsonException expected) {
    }
  }

  @Test
  public void nestedArrays() {
    Value root = Json.parse("[[[],1],[2,[]]]");
    assertEquals(2, root.items.size());
    assertEquals(2, root.items.get(0).items.size());
    assertTrue(root.items.get(0).items.get(0).items.isEmpty());
    assertTrue(root.items.get(1).items.get(1).items.isEmpty());
  }
}
