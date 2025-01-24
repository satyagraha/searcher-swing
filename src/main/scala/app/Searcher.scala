package org.satyagraha.searcher
package app

import engine.*
import io.*
import view.*

import com.typesafe.scalalogging.StrictLogging
import pureconfig.*

object Searcher extends StrictLogging :

  def main(args: Array[String]): Unit =
    val appConfig = ConfigSource.default.loadOrThrow[AppConfig]
    logger.info(s"appConfig: $appConfig")

    import appConfig.*

    val engine = new Engine
    val ioSupport = new IoSupport(ioPreferences)
    val ui = new Ui(uiPreferences, ioSupport)
    
    engine.subscribeTo(ui)
    ui.subscribeTo(engine)
    
    ui.handle(UiStateEvent(uiState))
    ui.handle(ControlStateEvent(ControlState.Idle))
    
    ui.run()
