package org.satyagraha.searcher

import io.MatchPosition

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.nio.file.Path
import scala.swing.event.Event

package object view:

  def copyToClipboard(text: String): Unit =
    val stringSelection = new StringSelection(text)
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    clipboard.setContents(stringSelection, null)

  enum ContextMenuChoice:
    case CopyFilename, CopyPath, EditSelected
  
  case class ContextMenuEvent(contextMenuChoice: ContextMenuChoice) extends Event
  
  case class EditEvent(path: Path, matchPosition: Option[MatchPosition]) extends Event
  