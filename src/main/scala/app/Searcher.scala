package org.satyagraha.searcher
package app

import engine.*
import model.*
import view.*

import pureconfig.*

object Searcher:

  def main(args: Array[String]): Unit =
    val appConfig = ConfigSource.default.loadOrThrow[AppConfig]
//    println(s"appConfig: $appConfig")

    val engine = new Engine
    val ui = new Ui(appConfig.uiPreferences)
    
    engine.subscribeTo(ui)
    ui.subscribeTo(engine)
    
    ui.handle(UiStateEvent(appConfig.uiState))
    ui.handle(ControlStateEvent(ControlState.Idle))
    
    ui.run()
