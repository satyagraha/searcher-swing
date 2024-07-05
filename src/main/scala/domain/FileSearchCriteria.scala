package org.satyagraha.searcher
package domain

import java.nio.file.{Path, PathMatcher}
import java.util.regex.Pattern

case class FileSearchCriteria(baseDir: Path,
                              includeFiles: List[PathMatcher],
                              excludeFiles: List[PathMatcher],
                              excludeDirs: List[PathMatcher],
                              matchText: String,
                              pattern: Pattern,
                              recurse: Boolean)
