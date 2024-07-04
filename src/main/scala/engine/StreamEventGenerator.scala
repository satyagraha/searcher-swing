package org.satyagraha.searcher
package engine

import domain.*
import io.*

import cats.effect.IO
import cats.effect.kernel.Deferred

object StreamEventGenerator {

  def launch(fileSearchCriteria: FileSearchCriteria,
             interruptor: Deferred[IO, Either[Throwable, Unit]],
             handler: StreamEvent => IO[Unit]): IO[Unit] =
    for {
      streamStateManager <- IO.pure(new StreamStateManager(fileSearchCriteria.baseDir))
      fileMatchStream =
        FileSearcher.findMatching(fileSearchCriteria)
          .interruptWhen(interruptor)
          .mapAccumulate(StreamStateManager.initialState) { case (streamState, fileMatch) =>
            streamStateManager.next(streamState, fileMatch)
          }
          .map(_._2)
          .flatMap(streamEvents => fs2.Stream.emits(streamEvents))
          .onComplete(fs2.Stream.emit(EndOfStreamEvent()))
      fullStream <- fileMatchStream
        .evalTap(handler)
        .compile.drain
    } yield ()

}
