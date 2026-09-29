package com.poorfarmer.core.save;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class AtomicFileStore {

  private final File file;

  public AtomicFileStore(File file) {
    this.file = file;
  }

  public void write(String content) {
    File temp = new File(file.getParentFile(), file.getName() + ".tmp");
    try {
      Files.write(temp.toPath(), content.getBytes(StandardCharsets.UTF_8));
      try {
        Files.move(temp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException atomicRejected) {
        Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException error) {
      throw new SessionWriteException(error.getMessage(), error);
    } finally {
      if (temp.exists() && !temp.delete()) {
        temp.deleteOnExit();
      }
    }
  }

  public String read() {
    try {
      return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    } catch (IOException error) {
      throw new SessionWriteException(error.getMessage(), error);
    }
  }

  public boolean exists() {
    return file.exists();
  }

  public void delete() {
    if (!file.delete()) {
      file.deleteOnExit();
    }
  }

  public static final class SessionWriteException extends RuntimeException {
    public SessionWriteException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
