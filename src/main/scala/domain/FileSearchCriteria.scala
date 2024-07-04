package org.satyagraha.searcher
package domain

import java.nio.file.{Path, PathMatcher}

case class FileSearchCriteria(baseDir: Path,
                              includeFiles: List[PathMatcher],
                              excludeFiles: List[PathMatcher],
                              excludeDirs: List[PathMatcher],
                              matchText: String,
                              matchCase: Boolean,
                              isRegex: Boolean,
                              recurse: Boolean)
