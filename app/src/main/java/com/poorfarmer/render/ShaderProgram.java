package com.poorfarmer.render;

import android.opengl.GLES30;

import com.poorfarmer.core.GlslValidator;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;

public final class ShaderProgram implements AutoCloseable {

  private static final IntBuffer INT_OUT = IntBuffer.allocate(1);

  private static int shaderCompileStatus(int shader) {
    INT_OUT.clear();
    GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, INT_OUT);
    return INT_OUT.get(0);
  }

  private static int programLinkStatus(int program) {
    INT_OUT.clear();
    GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, INT_OUT);
    return INT_OUT.get(0);
  }

  private final int id;
  private final HashMap<String, Integer> uniformCache = new HashMap<>();
  private final HashMap<String, Integer> attributeCache = new HashMap<>();
  private boolean closed;

  private ShaderProgram(int id) {
    this.id = id;
  }

  public static Builder create(String vertexSource, String fragmentSource) {
    List<String> vertexErrors = GlslValidator.validate(vertexSource);
    if (!vertexErrors.isEmpty()) {
      throw new RenderException("vertex shader rejected: " + vertexErrors);
    }
    List<String> fragmentErrors = GlslValidator.validate(fragmentSource);
    if (!fragmentErrors.isEmpty()) {
      throw new RenderException("fragment shader rejected: " + fragmentErrors);
    }
    int vertex = compileStage(GLES30.GL_VERTEX_SHADER, vertexSource);
    int fragment = compileStage(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
    int program = GLES30.glCreateProgram();
    if (program == 0) {
      GLES30.glDeleteShader(vertex);
      GLES30.glDeleteShader(fragment);
      throw new RenderException("glCreateProgram returned 0");
    }
    GLES30.glAttachShader(program, vertex);
    GLES30.glAttachShader(program, fragment);
    return new Builder(program, vertex, fragment);
  }

  public static ShaderProgram compile(String vertexSource, String fragmentSource) {
    return create(vertexSource, fragmentSource).link();
  }

  public static final class Builder {

    private final int program;
    private final int vertex;
    private final int fragment;

    private Builder(int program, int vertex, int fragment) {
      this.program = program;
      this.vertex = vertex;
      this.fragment = fragment;
    }

    public Builder bindAttribute(int index, String name) {
      GLES30.glBindAttribLocation(program, index, name);
      return this;
    }

    public ShaderProgram link() {
      GLES30.glLinkProgram(program);
      if (programLinkStatus(program) == 0) {
        String log = GLES30.glGetProgramInfoLog(program);
        abort();
        throw new RenderException("program link failed, info log: " + log);
      }
      GLES30.glDeleteShader(vertex);
      GLES30.glDeleteShader(fragment);
      return new ShaderProgram(program);
    }

    public void abort() {
      GLES30.glDetachShader(program, vertex);
      GLES30.glDetachShader(program, fragment);
      GLES30.glDeleteShader(vertex);
      GLES30.glDeleteShader(fragment);
      GLES30.glDeleteProgram(program);
    }
  }

  public static int compileStage(int type, String source) {
    int shader = GLES30.glCreateShader(type);
    if (shader == 0) {
      throw new RenderException("glCreateShader returned 0 for " + stageName(type));
    }
    GLES30.glShaderSource(shader, source);
    GLES30.glCompileShader(shader);
    if (shaderCompileStatus(shader) == 0) {
      String log = GLES30.glGetShaderInfoLog(shader);
      GLES30.glDeleteShader(shader);
      throw new RenderException(stageName(type) + " shader failed to compile, info log: " + log);
    }
    return shader;
  }

  private static String stageName(int type) {
    return type == GLES30.GL_VERTEX_SHADER ? "vertex" : "fragment";
  }

  public void use() {
    throwIfClosed();
    GLES30.glUseProgram(id);
  }

  public int programId() {
    return id;
  }

  public int uniform(String name) {
    throwIfClosed();
    Integer cached = uniformCache.get(name);
    if (cached != null) {
      return cached;
    }
    int location = GLES30.glGetUniformLocation(id, name);
    uniformCache.put(name, location);
    return location;
  }

  public void uniform1f(String name, float value) {
    int location = uniform(name);
    if (location >= 0) {
      GLES30.glUniform1f(location, value);
    }
  }

  public void uniform1i(String name, int value) {
    int location = uniform(name);
    if (location >= 0) {
      GLES30.glUniform1i(location, value);
    }
  }

  public void uniform2f(String name, float x, float y) {
    int location = uniform(name);
    if (location >= 0) {
      GLES30.glUniform2f(location, x, y);
    }
  }

  public void uniform3f(String name, float x, float y, float z) {
    int location = uniform(name);
    if (location >= 0) {
      GLES30.glUniform3f(location, x, y, z);
    }
  }

  public void uniform4f(String name, float x, float y, float z, float w) {
    int location = uniform(name);
    if (location >= 0) {
      GLES30.glUniform4f(location, x, y, z, w);
    }
  }

  public void uniformMat4(String name, float[] matrix) {
    int location = uniform(name);
    if (location >= 0) {
      GLES30.glUniformMatrix4fv(location, 1, false, matrix, 0);
    }
  }

  public int attribute(String name) {
    throwIfClosed();
    Integer cached = attributeCache.get(name);
    if (cached != null) {
      return cached;
    }
    int location = GLES30.glGetAttribLocation(id, name);
    attributeCache.put(name, location);
    return location;
  }

  private void throwIfClosed() {
    if (closed) {
      throw new IllegalStateException("shader program already closed");
    }
  }

  @Override
  public void close() {
    if (!closed) {
      GLES30.glDeleteProgram(id);
      closed = true;
    }
  }
}
