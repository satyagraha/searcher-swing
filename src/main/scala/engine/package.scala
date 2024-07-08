package org.satyagraha.searcher

import search.*

import java.nio.file.Path
import scala.swing.event.Event

package object engine:

  sealed trait EngineEvent extends Event

  case class BaseDirEvent(baseDir: Path) extends EngineEvent

  case class SubDirEvent(subDir: Path) extends EngineEvent

  case class FilenameEvent(filename: Path) extends EngineEvent
  
  case class MatchEvent(matchPosition: MatchPosition) extends EngineEvent

  case class EndOfStreamEvent() extends EngineEvent
