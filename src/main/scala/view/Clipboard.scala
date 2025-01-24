package org.satyagraha.searcher
package view

import com.typesafe.scalalogging.StrictLogging

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object Clipboard extends StrictLogging:

  def copyToClipboard(text: String): Unit =
    logger.debug(s"copying: $text")
    val stringSelection = new StringSelection(text)
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    clipboard.setContents(stringSelection, null)
