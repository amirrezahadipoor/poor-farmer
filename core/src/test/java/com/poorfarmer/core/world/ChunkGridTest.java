package com.poorfarmer.core.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.poorfarmer.core.world.ChunkGrid.Chunk;
import org.junit.Test;

public class ChunkGridTest {

  private static final float WORLD_HALF = 120f;
  private static final float SIZE = 16f;

  @Test
  public void sideMatchesWorldExtent() {
    ChunkGrid grid = new ChunkGrid(WORLD_HALF, SIZE);
    assertEquals(15, grid.side());
  }

  @Test
  public void sameCellCoordinatesShareChunk() {
    ChunkGrid grid = new ChunkGrid(WORLD_HALF, SIZE);
    Chunk a = grid.chunkAt(10f, -5f);
    Chunk b = grid.chunkAt(12f, 1f);
    assertSame(a, b);
    assertNotNull(a);
  }

  @Test
  public void crossingBoundaryChangesChunk() {
    ChunkGrid grid = new ChunkGrid(WORLD_HALF, SIZE);
    Chunk a = grid.chunkAt(7.9f, 0f);
    Chunk b = grid.chunkAt(8.1f, 0f);
    assertEquals(7, a.indexX);
    assertEquals(8, b.indexX);
  }

  @Test
  public void outOfWorldCoordinatesClampToEdge() {
    ChunkGrid grid = new ChunkGrid(WORLD_HALF, SIZE);
    Chunk far = grid.chunkAt(500f, 500f);
    assertNotNull(far);
    assertEquals(14, far.indexX);
    assertEquals(14, far.indexZ);
  }

  @Test
  public void chunkBoxCoversItsCell() {
    ChunkGrid grid = new ChunkGrid(WORLD_HALF, SIZE);
    Chunk chunk = grid.ensureChunk(12, 14);
    assertEquals(72f, chunk.minX, 0f);
    assertEquals(88f, chunk.maxX, 0f);
    assertEquals(104f, chunk.minZ, 0f);
    assertEquals(120f, chunk.maxZ, 0f);
    assertTrue(chunk.containsPoint(80f, 110f));
  }

  @Test
  public void updateLoadsChunksWithinRadiusOnly() {
    ChunkGrid grid = new ChunkGrid(WORLD_HALF, SIZE);
    grid.update(0f, 0f, 50f);
    assertTrue(grid.activeChunks().size() >= 9);
    for (Chunk chunk : grid.activeChunks()) {
      float dx = chunk.sphereCenterX;
      float dz = chunk.sphereCenterZ;
      assertTrue(dx * dx + dz * dz <= 50f * 50f);
      assertTrue(chunk.loaded);
    }
    Chunk farCorner = grid.chunkAt(100f, 100f);
    assertTrue(!farCorner.loaded);
  }

  @Test
  public void movingCameraUnloadsFarChunks() {
    ChunkGrid grid = new ChunkGrid(WORLD_HALF, SIZE);
    grid.update(0f, 0f, 50f);
    Chunk nearOrigin = grid.chunkAt(0f, 0f);
    assertTrue(nearOrigin.loaded);
    grid.update(100f, 100f, 50f);
    assertTrue(nearOrigin.loaded == false);
    assertNull(nearOrigin.content);
    Chunk newCenter = grid.chunkAt(100f, 100f);
    assertTrue(newCenter.loaded);
  }

  @Test
  public void unloadAllClearsState() {
    ChunkGrid grid = new ChunkGrid(WORLD_HALF, SIZE);
    grid.update(0f, 0f, 50f);
    grid.unloadAll();
    assertTrue(grid.activeChunks().isEmpty());
    assertFalse(grid.chunkAt(0f, 0f).loaded);
  }
}
