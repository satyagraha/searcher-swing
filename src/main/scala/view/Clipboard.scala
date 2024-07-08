package org.satyagraha.searcher
package view

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object Clipboard:

  def copyToClipboard(text: String): Unit =
    val stringSelection = new StringSelection(text)
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    clipboard.setContents(stringSelection, null)
