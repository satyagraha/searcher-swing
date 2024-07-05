package org.satyagraha.searcher
package app

import io.*
import model.*
import view.*

import pureconfig.*
import pureconfig.generic.derivation.default.*

case class AppConfig(uiState: UiState,
                     uiPreferences: UiPreferences,
                     ioPreferences: IoPreferences) derives ConfigReader
