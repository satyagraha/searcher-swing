package org.satyagraha.searcher

import search.*

import cats.data.NonEmptyList

import java.nio.file.Path
import scala.swing.event.Event

package object view:

  case class BrowseDirEvent() extends Event

  enum ControlState extends Enum[ControlState]:
    case Idle, Running

  case class ControlStateEvent(controlState: ControlState) extends Event

  case class InvalidFormEvent(messages: NonEmptyList[String]) extends Event

  enum ContextMenuChoice:
    case CopyFilename, CopyRelativePath, CopyFullPath, CopyLine, EditSelected

  case class ContextMenuEvent(contextMenuChoice: ContextMenuChoice) extends Event

  case class EditEvent(path: Path, matchPosition: Option[MatchPosition]) extends Event

  case class StartEvent(uiState: UiState) extends Event

  case class StopEvent() extends Event

  case class UiStateEvent(uiState: UiState) extends Event
  
  
  