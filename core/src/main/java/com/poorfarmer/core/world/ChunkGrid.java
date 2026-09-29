package com.poorfarmer.core.world;

import com.poorfarmer.core.math.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ChunkGrid {

  public static final float DEFAULT_CHUNK_SIZE = 16f;

  public static final class Chunk {
    public final int indexX;
    public final int indexZ;
    public final float minX;
    public final float minZ;
    public final float maxX;
    public final float maxZ;
    public final Vec3 boxMin;
    public final Vec3 boxMax;
    public final float sphereCenterX;
    public final float sphereCenterZ;
    public final float sphereRadius;
    public boolean loaded;
    public Object content;

    Chunk(int indexX, int indexZ, float size, float worldHalf) {
      this.indexX = indexX;
      this.indexZ = indexZ;
      minX = indexX * size - worldHalf;
      minZ = indexZ * size - worldHalf;
      maxX = minX + size;
      maxZ = minZ + size;
      boxMin = new Vec3(minX, 0f, minZ);
      boxMax = new Vec3(maxX, 30f, maxZ);
      sphereCenterX = (minX + maxX) / 2f;
      sphereCenterZ = (minZ + maxZ) / 2f;
      float half = size / 2f;
      sphereRadius = (float) Math.sqrt(half * half + half * half + 30f * 30f);
    }

    public boolean containsPoint(float x, float z) {
      return x >= minX && x < maxX && z >= minZ && z < maxZ;
    }
  }

  private final float worldHalf;
  private final float chunkSize;
  private final int side;
  private final HashMap<Long, Chunk> chunks = new HashMap<>();
  private final ArrayList<Chunk> active = new ArrayList<>();

  public ChunkGrid(float worldHalf, float chunkSize) {
    this.worldHalf = worldHalf;
    this.chunkSize = chunkSize;
    this.side = (int) Math.ceil((worldHalf * 2f) / chunkSize);
  }

  public float chunkSize() {
    return chunkSize;
  }

  public int side() {
    return side;
  }

  public int indexForCoordinate(float coordinate) {
    int index = (int) Math.floor((coordinate + worldHalf) / chunkSize);
    if (index < 0) {
      index = 0;
    }
    if (index >= side) {
      index = side - 1;
    }
    return index;
  }

  public Chunk chunkAt(float x, float z) {
    return ensureChunk(indexForCoordinate(x), indexForCoordinate(z));
  }

  public Chunk ensureChunk(int indexX, int indexZ) {
    if (indexX < 0 || indexX >= side || indexZ < 0 || indexZ >= side) {
      return null;
    }
    long key = ((long) indexX) * side + indexZ;
    Chunk chunk = chunks.get(key);
    if (chunk == null) {
      chunk = new Chunk(indexX, indexZ, chunkSize, worldHalf);
      chunks.put(key, chunk);
    }
    return chunk;
  }

  public void unloadAll() {
    for (Chunk chunk : chunks.values()) {
      chunk.loaded = false;
      chunk.content = null;
    }
    active.clear();
  }

  public void update(float cameraX, float cameraZ, float radius) {
    for (Chunk chunk : chunks.values()) {
      if (chunk.loaded) {
        chunk.loaded = false;
        chunk.content = null;
      }
    }
    active.clear();
    int minI = indexForCoordinate(cameraX - radius);
    int maxI = indexForCoordinate(cameraX + radius);
    int minJ = indexForCoordinate(cameraZ - radius);
    int maxJ = indexForCoordinate(cameraZ + radius);
    for (int i = minI; i <= maxI; i++) {
      for (int j = minJ; j <= maxJ; j++) {
        Chunk chunk = ensureChunk(i, j);
        float dx = chunk.sphereCenterX - cameraX;
        float dz = chunk.sphereCenterZ - cameraZ;
        if (dx * dx + dz * dz <= radius * radius) {
          chunk.loaded = true;
          active.add(chunk);
        }
      }
    }
  }

  public List<Chunk> activeChunks() {
    return active;
  }
}
