package org.satyagraha.searcher
package engine

import search.*

import cats.effect.IO
import cats.effect.kernel.Deferred

object EngineEventGenerator:

  def launch(fileSearchCriteria: FileSearchCriteria,
             interruptor: Deferred[IO, Either[Throwable, Unit]],
             handler: EngineEvent => IO[Unit]): IO[Unit] =
    for {
      streamStateManager <- IO.pure(new EngineStateManager(fileSearchCriteria.baseDir))
      fileMatchStream =
        FileSearcher.findMatching(fileSearchCriteria)
          .interruptWhen(interruptor)
          .mapAccumulate(EngineStateManager.initialState) { case (streamState, fileMatch) =>
            streamStateManager.next(streamState, fileMatch)
          }
          .map(_._2)
          .flatMap(streamEvents => fs2.Stream.emits(streamEvents))
          .onComplete(fs2.Stream.emit(EndOfStreamEvent()))
      fullStream <- fileMatchStream
        .evalTap(handler)
        .compile.drain
    } yield ()
