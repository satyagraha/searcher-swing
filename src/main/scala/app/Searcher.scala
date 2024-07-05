package org.satyagraha.searcher
package app

import engine.*
import io.*
import model.*
import view.*

import pureconfig.*

object Searcher:

  def main(args: Array[String]): Unit =
    val appConfig = ConfigSource.default.loadOrThrow[AppConfig]
    import appConfig.*
//    println(s"appConfig: $appConfig")

    val engine = new Engine
    val ioSupport = new IoSupport(ioPreferences)
    val ui = new Ui(uiPreferences, ioSupport)
    
    engine.subscribeTo(ui)
    ui.subscribeTo(engine)
    
    ui.handle(UiStateEvent(uiState))
    ui.handle(ControlStateEvent(ControlState.Idle))
    
    ui.run()
