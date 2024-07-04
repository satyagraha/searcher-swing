package org.satyagraha.searcher
package helpers

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.*
import scala.swing.Reactor
import scala.swing.event.Event

trait Sub extends Reactor:
  
  def subscribeTo(pubs: Pub*): Unit =
    pubs foreach: pub =>
      pub.publishTo(this)

  def handle(event: Event): Unit =
    if (reactions.isDefinedAt(event))
      reactions(event)

trait Pub:
  protected val subscribers: collection.mutable.Set[Sub] = ConcurrentHashMap.newKeySet[Sub]().asScala
  
  def publishTo(subs: Sub*): Unit =
    subscribers ++= subs

  def publish(event: Event): Unit =
    subscribers.foreach: sub =>
      sub.handle(event)

trait PubSub extends Pub with Sub
