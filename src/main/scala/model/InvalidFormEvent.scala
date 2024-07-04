package org.satyagraha.searcher
package model

import cats.data.NonEmptyList

import scala.swing.event.Event

case class InvalidFormEvent(messages: NonEmptyList[String]) extends Event
