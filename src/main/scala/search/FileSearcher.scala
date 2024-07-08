package org.satyagraha.searcher
package search

import helpers.fs2Path

import cats.effect.IO
import cats.implicits.*
import fs2.text
import mouse.all.*

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

object FileSearcher:
  type FileMatchStream = fs2.Stream[IO, FileMatch]

  def findMatching(fileSearchCriteria: FileSearchCriteria): FileMatchStream =
    import fileSearchCriteria.*

    def filenameMatches(path: Path): Boolean =
      val filename = path.getFileName
      val filenameIncluded = includeFiles.isEmpty || includeFiles.exists(_.matches(filename))
      val filenameExcluded = excludeFiles.exists(_.matches(filename))
      filenameIncluded && !filenameExcluded

    def directoryMatches(path: Path): Boolean =
      val directory = path.getFileName
      !excludeDirs.exists(_.matches(directory))

    def findMatchingTraverse(currentPath: Path): FileMatchStream =

      def fileContentsMatches(path: Path): FileMatchStream =

        def fileLineMatches(line: String, lineOffset: Long): Option[FileMatch] =
          if validLine(line) then
            val columnOffset = line.indexOf(matchText)
            (columnOffset != -1).option(FileMatch(path, MatchPosition(lineOffset + 1, columnOffset + 1, line)))
            val matcher = pattern.matcher(line)
            matcher.find().option:
              val columnOffset = matcher.start()
              FileMatch(path, MatchPosition(lineOffset + 1, columnOffset + 1, line))
          else
            None

        val linesStream = fs2.io.file.Files[IO]
          .readAll(path.fs2Path)
          .through(text.utf8.decode)
          .through(text.lines)

        linesStream
          .zipWithIndex
          .map(fileLineMatches.tupled)
          .unNone

      val (allFiles, allDirectories) =
        Files.list(currentPath).iterator().asScala.toList.sorted.partition(_.toFile.isFile)

      val checkableFiles = allFiles.filter(filenameMatches)
      val checkableDirs = allDirectories.filter(directoryMatches)

      val fileMatches = fs2.Stream.emits(checkableFiles).flatMap(fileContentsMatches)
      val dirMatches = fs2.Stream.emits(checkableDirs).flatMap(findMatchingTraverse)
      fileMatches append recurse.fold(dirMatches, fs2.Stream.empty)

    findMatchingTraverse(baseDir)

  private def validLine(line: String): Boolean =
    StandardCharsets.ISO_8859_1.newEncoder().canEncode(line)
