package com.poorfarmer.core.math;

public final class Mat4 {

  public final float[] data = new float[16];

  public Mat4() {
    identity();
  }

  public Mat4 identity() {
    for (int i = 0; i < 16; i++) {
      data[i] = 0f;
    }
    data[0] = 1f;
    data[5] = 1f;
    data[10] = 1f;
    data[15] = 1f;
    return this;
  }

  public Mat4 copyOf(Mat4 other) {
    System.arraycopy(other.data, 0, data, 0, 16);
    return this;
  }

  private static final ThreadLocal<float[]> ALIAS_TEMPORARY =
      ThreadLocal.withInitial(() -> new float[16]);

  public static void multiply(Mat4 a, Mat4 b, Mat4 out) {
    float[] r = out.data;
    float[] sa = a.data;
    float[] sb = b.data;
    if (out == a || out == b) {
      float[] tmp = ALIAS_TEMPORARY.get();
      for (int col = 0; col < 4; col++) {
        for (int row = 0; row < 4; row++) {
          tmp[col * 4 + row] =
              sa[0 * 4 + row] * sb[col * 4 + 0]
                  + sa[1 * 4 + row] * sb[col * 4 + 1]
                  + sa[2 * 4 + row] * sb[col * 4 + 2]
                  + sa[3 * 4 + row] * sb[col * 4 + 3];
        }
      }
      System.arraycopy(tmp, 0, r, 0, 16);
      return;
    }
    for (int col = 0; col < 4; col++) {
      for (int row = 0; row < 4; row++) {
        r[col * 4 + row] =
            sa[0 * 4 + row] * sb[col * 4 + 0]
                + sa[1 * 4 + row] * sb[col * 4 + 1]
                + sa[2 * 4 + row] * sb[col * 4 + 2]
                + sa[3 * 4 + row] * sb[col * 4 + 3];
      }
    }
  }

  public Mat4 multiply(Mat4 other) {
    Mat4.multiply(this, other, this);
    return this;
  }

  public Mat4 translation(float x, float y, float z) {
    identity();
    data[12] = x;
    data[13] = y;
    data[14] = z;
    return this;
  }

  public Mat4 rotationX(float radians) {
    identity();
    float c = (float) Math.cos(radians);
    float s = (float) Math.sin(radians);
    data[5] = c;
    data[6] = s;
    data[9] = -s;
    data[10] = c;
    return this;
  }

  public Mat4 rotationY(float radians) {
    identity();
    float c = (float) Math.cos(radians);
    float s = (float) Math.sin(radians);
    data[0] = c;
    data[2] = -s;
    data[8] = s;
    data[10] = c;
    return this;
  }

  public Mat4 rotationZ(float radians) {
    identity();
    float c = (float) Math.cos(radians);
    float s = (float) Math.sin(radians);
    data[0] = c;
    data[1] = s;
    data[4] = -s;
    data[5] = c;
    return this;
  }

  public Mat4 scale(float x, float y, float z) {
    identity();
    data[0] = x;
    data[5] = y;
    data[10] = z;
    return this;
  }

  public Mat4 perspective(float fovyRadians, float aspect, float near, float far) {
    for (int i = 0; i < 16; i++) {
      data[i] = 0f;
    }
    float f = (float) (1.0 / Math.tan(fovyRadians / 2.0));
    data[0] = f / aspect;
    data[5] = f;
    data[10] = (far + near) / (near - far);
    data[11] = -1f;
    data[14] = 2f * far * near / (near - far);
    return this;
  }

  public Mat4 lookAt(Vec3 eye, Vec3 center, Vec3 up) {
    Vec3 back = new Vec3(eye).sub(center).normalize();
    Vec3 right = new Vec3();
    Vec3.cross(up, back, right);
    right.normalize();
    Vec3 camUp = new Vec3();
    Vec3.cross(back, right, camUp);
    for (int i = 0; i < 16; i++) {
      data[i] = 0f;
    }
    data[0] = right.x;
    data[1] = camUp.x;
    data[2] = back.x;
    data[3] = 0f;
    data[4] = right.y;
    data[5] = camUp.y;
    data[6] = back.y;
    data[7] = 0f;
    data[8] = right.z;
    data[9] = camUp.z;
    data[10] = back.z;
    data[11] = 0f;
    data[12] = -right.dot(eye);
    data[13] = -camUp.dot(eye);
    data[14] = -back.dot(eye);
    data[15] = 1f;
    return this;
  }

  public void transformPoint(Vec3 point, Vec4 out) {
    out.x = data[0] * point.x + data[4] * point.y + data[8] * point.z + data[12];
    out.y = data[1] * point.x + data[5] * point.y + data[9] * point.z + data[13];
    out.z = data[2] * point.x + data[6] * point.y + data[10] * point.z + data[14];
    out.w = data[3] * point.x + data[7] * point.y + data[11] * point.z + data[15];
  }

  public void transformDirection(Vec3 point, Vec3 out) {
    out.x = data[0] * point.x + data[4] * point.y + data[8] * point.z;
    out.y = data[1] * point.x + data[5] * point.y + data[9] * point.z;
    out.z = data[2] * point.x + data[6] * point.y + data[10] * point.z;
  }

  public Mat4 invert(Mat4 out) {
    float det = determinant();
    if (Math.abs(det) < 1e-12f) {
      return out.identity();
    }
    float invDet = 1f / det;
    for (int col = 0; col < 4; col++) {
      for (int row = 0; row < 4; row++) {
        out.data[col * 4 + row] = cofactor(col, row) * invDet;
      }
    }
    return out;
  }

  private float determinant() {
    float sum = 0f;
    for (int c = 0; c < 4; c++) {
      int sign = (3 + c) % 2 == 0 ? 1 : -1;
      sum += data[3 + 4 * c] * sign * minor(3, c);
    }
    return sum;
  }

  private float cofactor(int row, int col) {
    int sign = (row + col) % 2 == 0 ? 1 : -1;
    return sign * minor(row, col);
  }

  private float minor(int delRow, int delCol) {
    float e0 = 0f, e1 = 0f, e2 = 0f, e3 = 0f, e4 = 0f, e5 = 0f, e6 = 0f, e7 = 0f, e8 = 0f;
    int k = 0;
    for (int r = 0; r < 4; r++) {
      if (r == delRow) {
        continue;
      }
      for (int c = 0; c < 4; c++) {
        if (c == delCol) {
          continue;
        }
        float v = data[c * 4 + r];
        switch (k) {
          case 0: e0 = v; break;
          case 1: e1 = v; break;
          case 2: e2 = v; break;
          case 3: e3 = v; break;
          case 4: e4 = v; break;
          case 5: e5 = v; break;
          case 6: e6 = v; break;
          case 7: e7 = v; break;
          default: e8 = v; break;
        }
        k++;
      }
    }
    return e0 * (e4 * e8 - e5 * e7)
        - e1 * (e3 * e8 - e5 * e6)
        + e2 * (e3 * e7 - e4 * e6);
  }

  public Mat4 transposed(Mat4 out) {
    float[] a = data;
    float[] r = out.data;
    r[0] = a[0];
    r[1] = a[4];
    r[2] = a[8];
    r[3] = a[12];
    r[4] = a[1];
    r[5] = a[5];
    r[6] = a[9];
    r[7] = a[13];
    r[8] = a[2];
    r[9] = a[6];
    r[10] = a[10];
    r[11] = a[14];
    r[12] = a[3];
    r[13] = a[7];
    r[14] = a[11];
    r[15] = a[15];
    return out;
  }
}
