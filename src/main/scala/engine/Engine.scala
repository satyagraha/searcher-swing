package org.satyagraha.searcher
package engine

import helpers.*
import search.*
import view.*

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.effect.{Deferred, IO}
import cats.effect.unsafe.implicits.global
import cats.implicits.given
import com.typesafe.scalalogging.StrictLogging
import mouse.all.given

import java.nio.file.{FileSystems, Path}
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern

class Engine extends PubSub with StrictLogging:

  private val interruptorRef = new AtomicReference[Option[Deferred[IO, Either[Throwable, Unit]]]]

  reactions +=
    locally:
      case StartEvent(uiState: UiState) =>
        logger.debug("StartEvent")
        validate(uiState) match
          case Validated.Valid(fileSearchCriteria) =>
            searchUsing(fileSearchCriteria)
          case Validated.Invalid(errors) =>
            publish(InvalidFormEvent(errors))
      case StopEvent() =>
        logger.debug("StopEvent")
        stop()
      case event =>
        logger.info(s"Engine: received unhandled event: $event")

  private def isValidDirectory(path: Path): Validated[String, Path] =
    Validated.cond(path.toFile.isDirectory, path, s"$path is not a valid directory")

  private val fileSystem = FileSystems.getDefault

  private def glob(wildcard: String) = fileSystem.getPathMatcher(s"glob:${wildcard.trim}")

  private def validate(uiState: UiState): ValidatedNel[String, FileSearchCriteria] =
    import uiState.*

    val baseDirValid = Validated
      .fromOption(baseDir.trimmedNonBlank, "base dir may not be empty")
      .map(Path.of(_))
      .andThen(isValidDirectory)
      .toValidatedNel
    val includeFilesValid = includeFiles.split(',').toList.flatMap(_.trimmedNonBlank).map(glob).validNel
    val excludeFilesValid = excludeFiles.split(',').toList.flatMap(_.trimmedNonBlank).map(glob).validNel
    val excludeDirsValid = excludeDirs.split(',').toList.flatMap(_.trimmedNonBlank).map(glob).validNel
    val matchTextValid = Validated.fromOption(matchText.trimmedNonBlank, "match text may not be empty").toValidatedNel
    val caseSensitiveFlag = matchCase.fold(0, Pattern.CASE_INSENSITIVE)
    val regexFlag = isRegex.fold(0, Pattern.LITERAL)
    val patternValid = Validated.catchNonFatal(Pattern.compile(matchText, caseSensitiveFlag | regexFlag)).leftMap(_.toString).toValidatedNel
    val recurseValid = recurse.validNel


    (baseDirValid, includeFilesValid, excludeFilesValid, excludeDirsValid, matchTextValid, patternValid, recurseValid)
      .mapN(FileSearchCriteria.apply)

  private def searchUsing(fileSearchCriteria: FileSearchCriteria): Unit =
    publish(ControlStateEvent(ControlState.Running))
    val interruptor = Deferred[IO, Either[Throwable, Unit]].unsafeRunSync()
    interruptorRef.set(interruptor.some)
    val launchIO = EngineEventGenerator.launch(fileSearchCriteria, interruptor, handler)
    launchIO.unsafeRunAndForget()

  private def handler(engineEvent: EngineEvent): IO[Unit] = IO:
    publish(engineEvent)
    engineEvent match
      case EndOfStreamEvent() =>
        publish(ControlStateEvent(ControlState.Idle))
      case _ =>

  private def stop(): Unit =
    interruptorRef.get().foreach(_.complete(().asRight).unsafeRunSync())
