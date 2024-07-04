package org.satyagraha.searcher
package io

import java.nio.file.Path

case class MatchPosition(lineNumber: Long,
                         columnNumber: Int,
                         lineText: String)

case class FileMatch(path: Path,
                     matchPosition: MatchPosition) {

  def filename: Path = path.getFileName

  def dir: Path = path.getParent

  def subDir(baseDir: Path): Path =
    baseDir.relativize(dir)
}
