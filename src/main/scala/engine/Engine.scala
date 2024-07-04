package org.satyagraha.searcher
package engine

import helpers.*
import model.*
import domain.*

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.effect.{Deferred, IO}
import cats.effect.unsafe.implicits.global
import cats.implicits.given

import java.nio.file.{FileSystems, Path}
import java.util.concurrent.atomic.AtomicReference

class Engine extends PubSub {

  private val interruptorRef = new AtomicReference[Option[Deferred[IO, Either[Throwable, Unit]]]]

  reactions += {
    case StartEvent(uiState: UiState) =>
      //      println("StartEvent")
      validate(uiState) match
        case Validated.Valid(fileSearchCriteria) =>
          searchUsing(fileSearchCriteria)
        case Validated.Invalid(errors) =>
          publish(InvalidFormEvent(errors))
    case StopEvent() =>
      //      println("StopEvent")
      stop()
    case event =>
      println(s"Engine: received unhandled event: $event")
  }

  private def isValidDirectory(path: Path): Validated[String, Path] =
    Validated.cond(path.toFile.isDirectory, path, s"$path is not a valid directory")

  private val fileSystem = FileSystems.getDefault

  private def glob(wildcard: String) = fileSystem.getPathMatcher(s"glob:${wildcard.trim}")

  private def validate(uiState: UiState): ValidatedNel[String, FileSearchCriteria] = {
    val baseDirValid = Validated
      .fromOption(uiState.baseDir.trimmedNonBlank, "base dir may not be empty")
      .map(Path.of(_))
      .andThen(isValidDirectory)
      .toValidatedNel
    val includeFilesValid = uiState.includeFiles.split(',').toList.flatMap(_.trimmedNonBlank).map(glob).validNel
    val excludeFilesValid = uiState.excludeFiles.split(',').toList.flatMap(_.trimmedNonBlank).map(glob).validNel
    val excludeDirsValid = uiState.excludeDirs.split(',').toList.flatMap(_.trimmedNonBlank).map(glob).validNel
    val matchTextValid = Validated.fromOption(uiState.matchText.trimmedNonBlank, "match text may not be empty").toValidatedNel
    val matchCaseValid = uiState.matchCase.validNel
    val isRegexValid = uiState.isRegex.validNel
    val recurseValid = uiState.recurse.validNel

//    val tried = Try {
//      FileSearchCriteria(
//        baseDir = Path.of(uiState.baseDir),
//        includeFiles = uiState.includeFiles.split(',').toList.map(_.trim),
//        excludeFiles = uiState.excludeFiles.split(',').toList.map(_.trim).map(Path.of(_)),
//        excludeDirs = uiState.excludeDirs.split(',').toList.map(_.trim).map(Path.of(_)),
//        matchText = uiState.matchText,
//        matchCase = uiState.matchCase,
//        isRegex = uiState.isRegex,
//        recurse = uiState.recurse)
//    }
//    tried.toEither.leftMap(_.toString).toValidatedNel

    (baseDirValid, includeFilesValid, excludeFilesValid, excludeDirsValid, matchTextValid, matchCaseValid, isRegexValid, recurseValid)
      .mapN(FileSearchCriteria.apply)
  }

  private def searchUsing(fileSearchCriteria: FileSearchCriteria): Unit = {
    publish(ControlStateEvent(ControlState.Running))
    val interruptor = Deferred[IO, Either[Throwable, Unit]].unsafeRunSync()
    interruptorRef.set(interruptor.some)
    val launchIO = StreamEventGenerator.launch(fileSearchCriteria, interruptor, handler)
    launchIO.unsafeRunAndForget()
  }

  private def handler(streamEvent: StreamEvent): IO[Unit] = IO {
    publish(streamEvent)
    streamEvent match
      case EndOfStreamEvent() =>
        publish(ControlStateEvent(ControlState.Idle))
      case _ =>
  }

  private def stop(): Unit =
    interruptorRef.get().foreach(_.complete(().asRight).unsafeRunSync())

}
