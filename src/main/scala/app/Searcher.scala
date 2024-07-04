package org.satyagraha.searcher
package app

import domain.*
import engine.*
import model.*
import view.*

import pureconfig.*

object Searcher {

//  val initialUiState = UiState(
//    baseDir = raw"D:/development/python",
//    includeFiles = "*.*",
//    excludeDirs = ".git,.svn",
//    matchText = "include",
//    matchCase = false,
//    isRegex = false,
//    recurse = true
//  )

  def main(args: Array[String]): Unit =
    val appConfig = ConfigSource.default.loadOrThrow[AppConfig]

    val engine = new Engine
    val ui = new Ui(appConfig.uiPreferences)
    engine.subscribeTo(ui)
    ui.subscribeTo(engine)
    engine.publish(UiStateEvent(appConfig.uiState))
    engine.publish(ControlStateEvent(ControlState.Idle))
    ui.run()
}
