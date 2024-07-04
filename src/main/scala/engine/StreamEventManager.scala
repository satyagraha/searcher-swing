package org.satyagraha.searcher
package engine

import domain.*
import io.*

import cats.implicits.given
import mouse.all.*

import java.nio.file.Path

case class StreamState(baseDir: Option[Path], subDir: Option[Path], filename: Option[Path])

class StreamStateManager(baseDir: Path):

  def next(currentState: StreamState, fileMatch: FileMatch): (StreamState, List[StreamEvent]) =
    val baseDirEvent = currentState.baseDir.isEmpty.option(BaseDirEvent(baseDir))

    val subDir = fileMatch.subDir(baseDir)
    val subDirEvent = (baseDirEvent.isDefined || (!currentState.subDir.contains(subDir))).option(SubDirEvent(subDir))

    val filename = fileMatch.filename
    val filenameEvent = (subDirEvent.isDefined || !currentState.filename.contains(filename)).option(FilenameEvent(filename))

    val matchEvent = MatchEvent(fileMatch.matchPosition).some

    val streamEvents = List(baseDirEvent, subDirEvent, filenameEvent, matchEvent).flatten
    val streamState = StreamState(baseDir.some, subDir.some, filename.some)

    (streamState, streamEvents)

object StreamStateManager:
  val initialState: StreamState = StreamState(None, None, None)
