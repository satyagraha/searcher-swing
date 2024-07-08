package org.satyagraha.searcher

import java.nio.file.{Path, PathMatcher}
import java.util.regex.Pattern

package object search:

  case class FileSearchCriteria(baseDir: Path,
                                includeFiles: List[PathMatcher],
                                excludeFiles: List[PathMatcher],
                                excludeDirs: List[PathMatcher],
                                matchText: String,
                                pattern: Pattern,
                                recurse: Boolean)

  case class MatchPosition(lineNumber: Long,
                           columnNumber: Int,
                           lineText: String)

  case class FileMatch(path: Path,
                       matchPosition: MatchPosition):

    def filename: Path = path.getFileName

    def dir: Path = path.getParent

    def subDir(baseDir: Path): Path =
      baseDir.relativize(dir)
