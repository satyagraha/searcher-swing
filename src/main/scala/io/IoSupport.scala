package org.satyagraha.searcher
package io

import search.*

import cats.implicits.given
import com.typesafe.scalalogging.StrictLogging

import java.nio.file.Path

class IoSupport(ioPreferences: IoPreferences) extends StrictLogging:

  import ioPreferences._

  def launchEditor(path: Path, matchPosition: Option[MatchPosition]): Unit =
    logger.debug(s"startProcess: $path $matchPosition")
    val editorArgsSubstituted = editorArgs.split(' ').map: arg =>
      arg
        .replace("<space>", " ")
        .replace("<file>", path.toString)
        .replace("<lineNumber>", matchPosition.map(_.lineNumber.toString).orEmpty)
        .replace("<columnNumber>", matchPosition.map(_.columnNumber.toString).orEmpty)
    val commands = editorPath +: editorArgsSubstituted
    logger.debug(s"commands: ${commands.toList}")
    val thread = new Thread(
      new Runnable:
        override def run(): Unit =
          Runtime.getRuntime.exec(commands)
    )
    thread.start()
