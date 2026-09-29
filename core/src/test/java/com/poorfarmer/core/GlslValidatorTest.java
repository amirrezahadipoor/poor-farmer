package com.poorfarmer.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GlslValidatorTest {

  private static final String VALID_VERTEX =
      "#version 300 es\n"
          + "layout(location = 0) in vec3 aPosition;\n"
          + "out vec3 vPosition;\n"
          + "void main() {\n"
          + "  vPosition = aPosition;\n"
          + "  gl_Position = vec4(aPosition, 1.0);\n"
          + "}\n";

  private static final String VALID_FRAGMENT =
      "#version 300 es\n"
          + "precision mediump float;\n"
          + "in vec3 vPosition;\n"
          + "out vec4 outColor;\n"
          + "void main() {\n"
          + "  outColor = vec4(vPosition, 1.0);\n"
          + "}\n";

  @Test
  public void acceptsMinimalVertexShader() {
    assertTrue(GlslValidator.isValid(VALID_VERTEX));
  }

  @Test
  public void acceptsMinimalFragmentShader() {
    assertTrue(GlslValidator.isValid(VALID_FRAGMENT));
  }

  @Test
  public void rejectsMissingVersionPragma() {
    String bad = "precision mediump float;\nvoid main() { outColor = vec4(1.0); }\n";
    assertFalse(GlslValidator.isValid(bad));
  }

  @Test
  public void rejectsMissingMain() {
    String bad = "#version 300 es\nfloat helper() { return 1.0; }\n";
    assertFalse(GlslValidator.isValid(bad));
  }

  @Test
  public void rejectsUnbalancedBraces() {
    String bad = "#version 300 es\nvoid main() { gl_Position = vec4(1.0);\n";
    assertFalse(GlslValidator.isValid(bad));
  }

  @Test
  public void rejectsEmptySource() {
    assertFalse(GlslValidator.isValid(""));
    assertFalse(GlslValidator.isValid(null));
  }

  @Test
  public void acceptsNestedBlocks() {
    String ok = "#version 300 es\n"
        + "void apply() { if (true) { gl_Position = vec4(1.0); } }\n"
        + "void main() { apply(); }\n";
    assertTrue(GlslValidator.isValid(ok));
  }

  @Test
  public void rejectsUnbalancedParentheses() {
    String bad = "#version 300 es\nvoid main() { gl_Position = vec4(1.0; }\n";
    assertFalse(GlslValidator.isValid(bad));
  }
}
