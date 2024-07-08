package org.satyagraha.searcher
package engine

import search.*

import cats.implicits.given
import mouse.all.*

import java.nio.file.Path

case class EngineState(baseDir: Option[Path], subDir: Option[Path], filename: Option[Path])

class EngineStateManager(baseDir: Path):

  def next(currentState: EngineState, fileMatch: FileMatch): (EngineState, List[EngineEvent]) =
    val baseDirEvent = currentState.baseDir.isEmpty.option(BaseDirEvent(baseDir))

    val subDir = fileMatch.subDir(baseDir)
    val subDirEvent = (baseDirEvent.isDefined || (!currentState.subDir.contains(subDir))).option(SubDirEvent(subDir))

    val filename = fileMatch.filename
    val filenameEvent = (subDirEvent.isDefined || !currentState.filename.contains(filename)).option(FilenameEvent(filename))

    val matchEvent = MatchEvent(fileMatch.matchPosition).some

    val streamEvents = List(baseDirEvent, subDirEvent, filenameEvent, matchEvent).flatten
    val streamState = EngineState(baseDir.some, subDir.some, filename.some)

    (streamState, streamEvents)

object EngineStateManager:
  val initialState: EngineState = EngineState(None, None, None)
