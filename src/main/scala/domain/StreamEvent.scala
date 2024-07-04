package org.satyagraha.searcher
package domain

import io.*

import java.nio.file.Path
import scala.swing.event.Event

sealed trait StreamEvent extends Event

case class BaseDirEvent(baseDir: Path) extends StreamEvent

case class SubDirEvent(subDir: Path) extends StreamEvent

case class FilenameEvent(filename: Path) extends StreamEvent

case class MatchEvent(matchPosition: MatchPosition) extends StreamEvent

case class EndOfStreamEvent() extends StreamEvent
