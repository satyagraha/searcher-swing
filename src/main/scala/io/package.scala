package org.satyagraha.searcher

import pureconfig.*
import pureconfig.generic.derivation.default.*

package object io:

  case class IoPreferences(editorPath: String,
                           editorArgs: String) derives ConfigReader
