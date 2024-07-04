package org.satyagraha.searcher
package app

import model.*
import view.*

import pureconfig.*
import pureconfig.generic.derivation.default.*

case class AppConfig(uiState: UiState,
                     uiPreferences: UiPreferences) derives ConfigReader
