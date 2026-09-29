package com.poorfarmer.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.math.Vec3;
import org.junit.Test;

public final class MeshBuilderTest {

  private static final float TOL = 1e-4f;

  private static void assertWindingMatchesNormals(MeshGeometry g, String label) {
    int checked = 0;
    for (int t = 0; t < g.indices.length; t += 3) {
      int a = g.indices[t];
      int b = g.indices[t + 1];
      int c = g.indices[t + 2];
      Vec3 pa = vertex(g, a);
      Vec3 pb = vertex(g, b);
      Vec3 pc = vertex(g, c);
      Vec3 edge1 = new Vec3(pb).sub(pa);
      Vec3 edge2 = new Vec3(pc).sub(pa);
      Vec3 n = new Vec3();
      Vec3.cross(edge1, edge2, n);
      if (n.length() < 1e-6f) {
        continue;
      }
      n.normalize();
      Vec3 avg = new Vec3(
          (g.normals[a * 3] + g.normals[b * 3] + g.normals[c * 3]) / 3f,
          (g.normals[a * 3 + 1] + g.normals[b * 3 + 1] + g.normals[c * 3 + 1]) / 3f,
          (g.normals[a * 3 + 2] + g.normals[b * 3 + 2] + g.normals[c * 3 + 2]) / 3f).normalize();
      assertTrue(label + " triangle " + (t / 3) + " winding vs normal dot " + n.dot(avg),
          n.dot(avg) > 0.5f);
      checked++;
    }
    assertTrue(label + " must check at least one triangle", checked > 0);
  }

  private static void assertNormalsUnit(MeshGeometry g, String label) {
    for (int i = 0; i < g.normals.length; i += 3) {
      float len = (float) Math.sqrt(
          g.normals[i] * g.normals[i]
              + g.normals[i + 1] * g.normals[i + 1]
              + g.normals[i + 2] * g.normals[i + 2]);
      assertTrue(label + " normal length", Math.abs(len - 1f) < 1e-3f);
    }
  }

  private static Vec3 vertex(MeshGeometry g, int index) {
    return new Vec3(g.positions[index * 3], g.positions[index * 3 + 1], g.positions[index * 3 + 2]);
  }

  @Test
  public void boxHasExpectedTopology() {
    MeshGeometry g = MeshBuilder.box(2f, 1f, 3f);
    assertEquals(24, g.vertexCount());
    assertEquals(36, g.indices.length);
    assertEquals(12, g.triangleCount());
    Vec3[] box = g.boundingBox();
    assertEquals(2f, box[1].x - box[0].x, TOL);
    assertEquals(1f, box[1].y - box[0].y, TOL);
    assertEquals(3f, box[1].z - box[0].z, TOL);
    assertNormalsUnit(g, "box");
    assertWindingMatchesNormals(g, "box");
  }

  @Test
  public void boxFaceNormalsPointOutward() {
    MeshGeometry g = MeshBuilder.box(1f, 1f, 1f);
    for (int i = 0; i < g.positions.length; i += 3) {
      Vec3 p = new Vec3(g.positions[i], g.positions[i + 1], g.positions[i + 2]);
      Vec3 n = new Vec3(g.normals[i], g.normals[i + 1], g.normals[i + 2]);
      assertTrue("box vertex dot " + p.dot(n), p.dot(n) > 0f);
    }
  }

  @Test
  public void cylinderSideNormalsAreHorizontal() {
    MeshGeometry g = MeshBuilder.cylinder(1f, 2f, 16, true, true);
    for (int i = 0; i < 16 * 4 * 3; i += 3) {
      float ny = g.normals[i + 1];
      assertTrue("cylinder side normal y", Math.abs(ny) < 1e-5f);
      float radius = (float) Math.sqrt(g.positions[i] * g.positions[i] + g.positions[i + 2] * g.positions[i + 2]);
      assertEquals(1f, radius, TOL);
    }
    assertNormalsUnit(g, "cylinder");
    assertWindingMatchesNormals(g, "cylinder");
  }

  @Test
  public void cylinderCapsHaveVerticalNormals() {
    MeshGeometry g = MeshBuilder.cylinder(1f, 2f, 16, true, true);
    int sideVertices = 16 * 4;
    for (int i = sideVertices; i < g.vertexCount(); i++) {
      float ny = g.normals[i * 3 + 1];
      assertEquals(1f, Math.abs(ny), TOL);
    }
  }

  @Test
  public void coneApexAtTopAndOutwardNormals() {
    MeshGeometry g = MeshBuilder.cone(1f, 2f, 12);
    float maxTop = Float.MIN_VALUE;
    for (int i = 0; i < g.positions.length; i += 3) {
      if (Math.abs(g.positions[i]) < 1e-6f && Math.abs(g.positions[i + 2]) < 1e-6f) {
        maxTop = Math.max(maxTop, g.positions[i + 1]);
      }
    }
    assertEquals(1f, maxTop, TOL);
    for (int i = 0; i < g.positions.length; i += 3) {
      float radial = (float) Math.sqrt(g.positions[i] * g.positions[i] + g.positions[i + 2] * g.positions[i + 2]);
      if (radial > 0.5f) {
        float nx = g.normals[i];
        float nz = g.normals[i + 2];
        assertTrue("cone outward radial", nx * g.positions[i] / radial + nz * g.positions[i + 2] / radial > 0f);
        assertTrue("cone upward", g.normals[i + 1] > 0f);
      }
    }
    assertWindingMatchesNormals(g, "cone");
  }

  @Test
  public void sphereSurfaceAndNormals() {
    MeshGeometry g = MeshBuilder.sphere(2f, 10, 8);
    assertEquals((10 + 1) * (8 + 1), g.vertexCount());
    assertEquals(10 * 8 * 6, g.indices.length);
    for (int i = 0; i < g.positions.length; i += 3) {
      Vec3 p = new Vec3(g.positions[i], g.positions[i + 1], g.positions[i + 2]);
      assertEquals(2f, p.length(), 1e-3f);
      Vec3 n = new Vec3(g.normals[i], g.normals[i + 1], g.normals[i + 2]);
      assertEquals(p.normalize().dot(n), 1f, 1e-3f);
    }
    assertWindingMatchesNormals(g, "sphere");
  }

  @Test
  public void latheCylinderProfileIsOutward() {
    MeshGeometry g = MeshBuilder.lathe(new float[][]{{1f, 1f}, {1f, -1f}}, 12);
    assertEquals(13 * 2, g.vertexCount());
    assertEquals(12 * 6, g.indices.length);
    for (int i = 0; i < g.positions.length; i += 3) {
      float ny = g.normals[i + 1];
      assertTrue("lathe cylinder normal y", Math.abs(ny) < 1e-5f);
      float rx = g.positions[i];
      float rz = g.positions[i + 2];
      assertTrue("lathe outward", g.normals[i] * rx + g.normals[i + 2] * rz > 0f);
    }
    assertWindingMatchesNormals(g, "lathe");
  }

  @Test
  public void latheConicalProfileHasTiltedNormals() {
    MeshGeometry g = MeshBuilder.lathe(new float[][]{{0f, 1f}, {1f, -1f}}, 12);
    for (int i = 0; i < g.positions.length; i += 3) {
      float rx = g.positions[i];
      float rz = g.positions[i + 2];
      float radial = (float) Math.sqrt(rx * rx + rz * rz);
      if (radial > 0.01f) {
        assertTrue("lathe cone outward", g.normals[i] * rx / radial + g.normals[i + 2] * rz / radial > 0f);
        assertTrue("lathe cone upward", g.normals[i + 1] > 0f);
      }
    }
    assertWindingMatchesNormals(g, "latheCone");
  }

  @Test
  public void extrudeSquareFootprint() {
    MeshGeometry g = MeshBuilder.extrude(new float[][]{{0f, 0f}, {2f, 0f}, {2f, 2f}, {0f, 2f}}, 3f);
    assertEquals(4 * 4 + 4 + 4, g.vertexCount());
    assertEquals((4 * 2 + 2 + 2) * 3, g.indices.length);
    boolean topSeen = false;
    boolean bottomSeen = false;
    for (int i = 0; i < g.positions.length; i += 3) {
      if (Math.abs(g.positions[i + 1] - 3f) < TOL) {
        topSeen = true;
      }
      if (Math.abs(g.positions[i + 1]) < TOL) {
        bottomSeen = true;
      }
    }
    assertTrue(topSeen);
    assertTrue(bottomSeen);
    assertNormalsUnit(g, "extrude");
    assertWindingMatchesNormals(g, "extrude");
  }

  @Test
  public void extrudeReversedOutlineStillValid() {
    MeshGeometry g = MeshBuilder.extrude(new float[][]{{0f, 2f}, {0f, 0f}, {2f, 0f}, {2f, 2f}}, 1f);
    assertWindingMatchesNormals(g, "extrudeReversed");
    assertNormalsUnit(g, "extrudeReversed");
  }

  @Test
  public void mergeOffsetsIndicesAndSumsCounts() {
    MeshGeometry a = MeshBuilder.box(1f, 1f, 1f);
    MeshGeometry b = MeshBuilder.cone(0.5f, 1f, 8);
    int verticesA = a.vertexCount();
    MeshGeometry merged = MeshBuilder.merge(new MeshGeometry[]{a, b});
    assertEquals(verticesA + b.vertexCount(), merged.vertexCount());
    assertEquals(a.indices.length + b.indices.length, merged.indices.length);
    for (int i = a.indices.length; i < merged.indices.length; i++) {
      assertTrue("merged index offset", merged.indices[i] >= verticesA);
    }
  }

  @Test
  public void mergePreservesColorsOnTrimmedParts() {
    MeshGeometry a = MeshBuilder.box(1f, 1f, 1f);
    MeshGeometry b = MeshBuilder.box(2f, 2f, 2f);
    for (int i = 0; i < a.vertexCount(); i++) {
      a.setVertexColor(i, 1f, 0f, 0f, 1f);
    }
    for (int i = 0; i < b.vertexCount(); i++) {
      b.setVertexColor(i, 0f, 1f, 0f, 1f);
    }
    MeshGeometry merged = MeshBuilder.merge(new MeshGeometry[]{a, b});
    int va = a.vertexCount();
    assertTrue(merged.hasColors());
    assertEquals(1f, merged.colors[0], 0f);
    assertEquals(0f, merged.colors[va * 4], 0f);
    assertEquals(1f, merged.colors[va * 4 + 1], 0f);
    for (int i = va; i < merged.vertexCount(); i++) {
      assertTrue("index out of range for colors", (i + 1) * 4 <= merged.colors.length);
    }
  }

  @Test
  public void translateAndScaleMoveGeometry() {
    MeshGeometry g = MeshBuilder.box(1f, 1f, 1f);
    g.scale(2f, 1f, 1f).translate(5f, 0f, 0f);
    Vec3[] box = g.boundingBox();
    assertEquals(4f, box[0].x, TOL);
    assertEquals(6f, box[1].x, TOL);
  }

  @Test
  public void rotateYSwapsAxisRanges() {
    MeshGeometry g = MeshBuilder.box(1f, 1f, 3f);
    g.rotateY((float) Math.PI / 2f);
    Vec3[] box = g.boundingBox();
    assertTrue("rotated x range large", box[1].x - box[0].x > 2.9f);
    assertTrue("rotated z range original x", Math.abs(box[1].z - box[0].z - 1f) < 0.1f);
  }

  @Test
  public void triangulatorCoversLShape() {
    float[] xs = {0f, 2f, 2f, 1f, 1f, 0f};
    float[] zs = {0f, 0f, 2f, 2f, 1f, 1f};
    var triangles = PolygonTriangulator.triangulate(xs, zs);
    assertEquals(4, triangles.size());
    for (int[] t : triangles) {
      float cross = (xs[t[1]] - xs[t[0]]) * (zs[t[2]] - zs[t[0]])
          - (zs[t[1]] - zs[t[0]]) * (xs[t[2]] - xs[t[0]]);
      assertTrue("ear must be convex", cross > 0f);
    }
    assertFalse(triangles.isEmpty());
  }
}
