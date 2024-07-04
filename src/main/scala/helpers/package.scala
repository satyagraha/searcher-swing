package org.satyagraha.searcher

import cats.kernel.Monoid
import mouse.all.*

import java.nio.file.Path

package object helpers:

  extension [A, CC[X] <: Iterable[X]](cca: CC[A])
    def toNonEmpty: Option[CC[A]] =
      cca.nonEmpty.option(cca)

  extension (str: String)
    def trimmedNonBlank: Option[String] =
      val trimmed = str.trim
      trimmed.nonEmpty.option(trimmed)

  given Monoid[Path] with
    override def empty: Path = Path.of("")

    override def combine(x: Path, y: Path): Path = x resolve y

  extension (path: Path)
    def fs2Path: fs2.io.file.Path =
      fs2.io.file.Path.fromNioPath(path)
