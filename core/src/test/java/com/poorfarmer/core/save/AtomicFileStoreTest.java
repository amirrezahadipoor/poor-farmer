package com.poorfarmer.core.save;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;

public class AtomicFileStoreTest {

  private Path dir;
  private AtomicFileStore store;

  @Before
  public void setUp() throws Exception {
    dir = Files.createTempDirectory("session-test");
    store = new AtomicFileStore(new File(dir.toFile(), "session.json"));
  }

  @Test
  public void writeThenRead() {
    store.write("{\"a\":1}");
    assertTrue(store.exists());
    assertEquals("{\"a\":1}", store.read());
  }

  @Test
  public void overwriteReplacesContent() {
    store.write("first");
    store.write("second");
    assertEquals("second", store.read());
  }

  @Test
  public void noTemporaryFileRemains() {
    store.write("data");
    File temp = new File(dir.toFile(), "session.json.tmp");
    assertFalse(temp.exists());
  }

  @Test
  public void deleteRemovesFile() {
    store.write("data");
    store.delete();
    assertFalse(store.exists());
  }

  @Test
  public void readingMissingFileThrows() {
    try {
      store.read();
      fail("expected missing file failure");
    } catch (AtomicFileStore.SessionWriteException expected) {
    }
  }

  @Test
  public void unicodeContentSurvives() {
    store.write("{\"label\":\"در حال بارگذاری\"}");
    assertEquals("{\"label\":\"در حال بارگذاری\"}", store.read());
  }
}
