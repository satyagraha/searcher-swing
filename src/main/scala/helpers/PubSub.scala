package org.satyagraha.searcher
package helpers

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.*
import scala.swing.Reactions.Reaction
import scala.swing.Reactor
import scala.swing.event.Event

trait Sub extends Reactor {
  
  def subscribeTo(pubs: Pub*): Unit = {
    pubs foreach { pub =>
      pub.publishTo(this)
    }
  }
}

trait Pub {
  protected val subscribers: collection.mutable.Set[Sub] = ConcurrentHashMap.newKeySet[Sub]().asScala
  
  def publishTo(subs: Sub*): Unit = {
    subscribers ++= subs
  }

  def publish(event: Event): Unit = {
//    println
//    println(s"${getClass} listeners: $listeners")
    subscribers foreach { sub =>
//      println(s"$getClass publishing $event to $l")
      if (sub.reactions.isDefinedAt(event)) sub.reactions(event)
    }
  }

}

trait PubSub extends Pub with Sub
