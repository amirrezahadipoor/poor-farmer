package com.poorfarmer.core.world;

import com.poorfarmer.core.math.Mat4;
import com.poorfarmer.core.math.Vec2;
import com.poorfarmer.core.math.Vec3;
import com.poorfarmer.core.math.Vec4;

public final class GameCamera {

  public static final float PITCH_DEG = 50f;
  public static final float FOV_DEG = 60f;
  public static final float NEAR = 0.1f;
  public static final float FAR = 500f;
  public static final float MIN_ZOOM = 8f;
  public static final float MAX_ZOOM = 45f;
  public static final float SMOOTHING = 10f;

  private final float worldHalf;
  private final float pitch = (float) Math.toRadians(PITCH_DEG);
  private final Vec3 eyeScratch = new Vec3();
  private final Vec3 lookScratch = new Vec3();

  private Vec2 center = new Vec2(0f, 0f);
  private Vec2 targetCenter = new Vec2(0f, 0f);
  private float zoom = MIN_ZOOM;
  private float targetZoom = MIN_ZOOM;
  private float yaw;
  private float targetYaw;

  public GameCamera(float worldHalf) {
    this.worldHalf = worldHalf;
  }

  public void snap() {
    center.set(targetCenter);
    zoom = targetZoom;
    yaw = targetYaw;
  }

  public void update(float dt) {
    float alpha = 1f - (float) Math.exp(-dt * SMOOTHING);
    center.x += (targetCenter.x - center.x) * alpha;
    center.y += (targetCenter.y - center.y) * alpha;
    zoom += (targetZoom - zoom) * alpha;
    yaw += (targetYaw - yaw) * alpha;
  }

  public Vec2 center() {
    return center;
  }

  public Vec2 targetCenter() {
    return targetCenter;
  }

  public float zoom() {
    return zoom;
  }

  public float targetZoom() {
    return targetZoom;
  }

  public float yawDegrees() {
    float d = (float) Math.toDegrees(yaw) % 360f;
    if (d < 0) {
      d += 360f;
    }
    return d;
  }

  public void panByScreen(float dxScreen, float dyScreen, float screenH) {
    float metersPerPixel = metersPerPixel(screenH);
    Vec2 right = rightDir();
    Vec2 up = forward();
    targetCenter.x += (-dxScreen * right.x + dyScreen * up.x) * metersPerPixel;
    targetCenter.y += (-dxScreen * right.y + dyScreen * up.y) * metersPerPixel;
    targetCenter.x = clamp(targetCenter.x, -worldHalf, worldHalf);
    targetCenter.y = clamp(targetCenter.y, -worldHalf, worldHalf);
  }

  public void rotate(int steps) {
    targetYaw += steps * (float) Math.PI / 2f;
  }

  public void zoomBy(float amount) {
    targetZoom = clamp(targetZoom + amount, MIN_ZOOM, MAX_ZOOM);
  }

  public void setZoom(float value) {
    targetZoom = clamp(value, MIN_ZOOM, MAX_ZOOM);
  }

  public void setCenter(float x, float z) {
    targetCenter.set(clamp(x, -worldHalf, worldHalf), clamp(z, -worldHalf, worldHalf));
  }

  public Vec2 forward() {
    return new Vec2(-(float) Math.sin(yaw), -(float) Math.cos(yaw));
  }

  public Vec2 rightDir() {
    return new Vec2((float) Math.cos(yaw), -(float) Math.sin(yaw));
  }

  private float metersPerPixel(float screenH) {
    float horiz = zoom * (float) Math.cos(pitch);
    float groundExtent = 2f * horiz * (float) Math.tan(Math.toRadians(FOV_DEG / 2f)) / (float) Math.cos(pitch);
    return groundExtent / screenH;
  }

  public Vec3 eyePosition() {
    float horiz = zoom * (float) Math.cos(pitch);
    float vert = zoom * (float) Math.sin(pitch);
    return new Vec3(
        center.x + (float) Math.sin(yaw) * horiz,
        vert,
        center.y + (float) Math.cos(yaw) * horiz);
  }

  public static final float TAN_HALF_FOV = (float) Math.tan(Math.toRadians(FOV_DEG / 2f));

  public void fillEye(Vec3 out) {
    float horiz = zoom * (float) Math.cos(pitch);
    float vert = zoom * (float) Math.sin(pitch);
    out.set(center.x + (float) Math.sin(yaw) * horiz, vert, center.y + (float) Math.cos(yaw) * horiz);
  }

  public void fillRight(Vec3 out) {
    out.set((float) Math.cos(yaw), 0f, -(float) Math.sin(yaw));
  }

  public void fillForward(Vec3 out) {
    float cosPitch = (float) Math.cos(pitch);
    out.set(-(float) Math.sin(yaw) * cosPitch, -(float) Math.sin(pitch), -(float) Math.cos(yaw) * cosPitch);
  }

  public Mat4 viewMatrix() {
    return new Mat4().lookAt(eyePosition(), new Vec3(center.x, 0f, center.y), Vec3.UP);
  }

  public void fillView(Mat4 out) {
    float horiz = zoom * (float) Math.cos(pitch);
    float vert = zoom * (float) Math.sin(pitch);
    eyeScratch.set(center.x + (float) Math.sin(yaw) * horiz, vert, center.y + (float) Math.cos(yaw) * horiz);
    lookScratch.set(center.x, 0f, center.y);
    out.lookAt(eyeScratch, lookScratch, Vec3.UP);
  }

  public Mat4 projectionMatrix(float aspect) {
    return new Mat4().perspective((float) Math.toRadians(FOV_DEG), aspect, NEAR, FAR);
  }

  public void fillProjection(Mat4 out, float aspect) {
    out.perspective((float) Math.toRadians(FOV_DEG), aspect, NEAR, FAR);
  }

  public Vec4 screenFromWorld(Mat4 proj, Vec3 world, float screenW, float screenH) {
    Mat4 view = viewMatrix();
    Vec4 clip = new Vec4();
    view.transformPoint(world, clip);
    Vec4 out = new Vec4();
    proj.transformPoint(new Vec3(clip.x, clip.y, clip.z), out);
    if (out.w == 0f) {
      out.set(0f, 0f, 0f, 0f);
      return out;
    }
    float ndcX = out.x / out.w;
    float ndcY = out.y / out.w;
    float ndcZ = out.z / out.w;
    out.set((ndcX * 0.5f + 0.5f) * screenW, (0.5f - ndcY * 0.5f) * screenH, ndcZ, 1f);
    return out;
  }

  public Vec3 worldFromScreen(Mat4 proj, float sx, float sy, float screenW, float screenH, float groundY) {
    Mat4 view = viewMatrix();
    Vec3 eye = eyePosition();
    float ndcX = sx / screenW * 2f - 1f;
    float ndcY = 1f - sy / screenH * 2f;
    Mat4 viewProj = new Mat4();
    Mat4.multiply(proj, view, viewProj);
    Mat4 invViewProj = new Mat4();
    viewProj.invert(invViewProj);
    Vec4 nearVec = new Vec4(ndcX, ndcY, -1f, 1f);
    Vec4 farVec = new Vec4(ndcX, ndcY, 1f, 1f);
    Vec3 nearPoint = invViewPoint(invViewProj, nearVec);
    Vec3 farPoint = invViewPoint(invViewProj, farVec);
    Vec3 dir = farPoint.sub(nearPoint).normalize();
    float t = (groundY - eye.y) / dir.y;
    return new Vec3(
        eye.x + dir.x * t,
        groundY,
        eye.z + dir.z * t);
  }

  private static Vec3 invViewPoint(Mat4 invView, Vec4 ndc) {
    Vec4 world = new Vec4();
    invView.transformPoint(new Vec3(ndc.x, ndc.y, ndc.z), world);
    if (world.w == 0f) {
      return new Vec3(world.x, world.y, world.z);
    }
    return new Vec3(world.x / world.w, world.y / world.w, world.z / world.w);
  }

  private static float clamp(float v, float min, float max) {
    return v < min ? min : (v > max ? max : v);
  }
}
