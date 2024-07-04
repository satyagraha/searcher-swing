package org.satyagraha.searcher
package model

import scala.swing.event.Event

enum ControlState extends Enum[ControlState]:
  case Idle, Running

case class ControlStateEvent(controlState: ControlState) extends Event
