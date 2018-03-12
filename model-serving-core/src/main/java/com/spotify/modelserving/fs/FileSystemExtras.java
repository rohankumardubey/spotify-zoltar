/*-
 * -\-\-
 * model-serving-tensorflow
 * --
 * Copyright (C) 2016 - 2018 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.modelserving.fs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

public final class FileSystemExtras {

  private FileSystemExtras() {
  }

  public static Path path(URI uri) {
    if (uri.getScheme() == null) {
      return Paths.get(uri.toString());
    }

    return Paths.get(uri);
  }

  /**
   * If the path is not on a local filesystem, it will download the resource to a temporary path on
   * a local filesystem.
   */
  public static URI downloadIfNonLocal(URI path) throws IOException {
    String fixedPath = path.toString().endsWith("/") ? path.toString() : path.toString() + "/";
    Path src = path(URI.create(fixedPath));
    if (src.getFileSystem().equals(FileSystems.getDefault())) {
      return src.toUri();
    }

    Path temp = Files.createTempDirectory("model-serving-");
    return copyDir(src, temp, true).toUri();
  }

  private static Path copyDir(Path src, Path dest, boolean overwrite) throws IOException {
    final List<Path> paths = Files.walk(src)
        .filter(path -> !path.equals(src))
        .collect(Collectors.toList());

    for (Path path : paths) {
      final String relative = path.toString().substring(src.toString().length() - 1);
      final Path fullDst = Paths.get(dest.toString(), relative);
      CopyOption[] flags = overwrite ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING}
          : new CopyOption[]{};
      Files.copy(path, fullDst, flags);
    }

    return dest;
  }
}