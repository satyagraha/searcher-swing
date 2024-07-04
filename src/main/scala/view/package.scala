package org.satyagraha.searcher

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

package object view:

  def copyToClipboard(text: String): Unit =
    val stringSelection = new StringSelection(text)
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    clipboard.setContents(stringSelection, null)
