package org.satyagraha.searcher
package view

import domain.*
import helpers.{given, *}
import io.*
import model.*

import cats.data.NonEmptyList
import cats.implicits.given

import java.awt.event.{MouseAdapter, MouseEvent}
import java.nio.file.Path
import javax.swing.JTree
import javax.swing.SwingUtilities.{invokeLater, isRightMouseButton}
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel, TreePath}
import scala.swing.event.{ButtonClicked, Event}
import scala.swing.{Component, MenuItem, PopupMenu, Publisher}

object MatchesTree:
  class PathTreeNode(val content: Path | String) extends DefaultMutableTreeNode(content)

  def pathTreePaths(treePath: TreePath): List[Path] =
    treePath.getPath.toList.map(_.asInstanceOf[PathTreeNode].content).collect:
      case path: Path => path

class MatchesTree extends PubSub:
  import MatchesTree._

  val treeModel = new DefaultTreeModel(null)
  val tree = new JTree(treeModel)

  var baseDirNode: DefaultMutableTreeNode = _
  var subDirNode: DefaultMutableTreeNode = _
  var filenameNode: DefaultMutableTreeNode = _

  subscribeTo(this) // to get the context menu events below

  trait Republish:
    self: Publisher =>
    override def publish(e: Event): Unit =
      MatchesTree.this.publish(e)

  val copyFilename = new MenuItem("Copy filename") with Republish
  val copyPath = new MenuItem("Copy path") with Republish
  val popupMenu = new PopupMenu:
    contents ++= Seq(copyFilename, copyPath)

  val stree = Component.wrap(tree)

  val contextMenuPopup = new MouseAdapter:
    override def mouseClicked(e: MouseEvent): Unit =
      if isRightMouseButton(e) then
        popupMenu.show(stree, e.getX, e.getY)

  tree.addMouseListener(contextMenuPopup)

  reactions += {
    case BaseDirEvent(baseDir) =>
      invokeLater: () =>
        baseDirNode = new PathTreeNode(baseDir)
        treeModel.setRoot(baseDirNode)
    case SubDirEvent(subDir) =>
      invokeLater: () =>
        subDirNode = new PathTreeNode(subDir)
        treeModel.insertNodeInto(subDirNode, baseDirNode, baseDirNode.getChildCount)
        tree.expandPath(new TreePath(baseDirNode))
    case FilenameEvent(filename) =>
      invokeLater: () =>
        filenameNode = new PathTreeNode(filename)
        treeModel.insertNodeInto(filenameNode, subDirNode, subDirNode.getChildCount)
        val nodes = Array[AnyRef](baseDirNode, subDirNode)
        val subDirPath = new TreePath(nodes)
        tree.expandPath(subDirPath)
    case MatchEvent(matchPosition) =>
      invokeLater: () =>
        val matchPositionNode = new PathTreeNode(render(matchPosition))
        treeModel.insertNodeInto(matchPositionNode, filenameNode, filenameNode.getChildCount)
        val nodes = Array[AnyRef](baseDirNode, subDirNode, filenameNode)
        val filenamePath = new TreePath(nodes)
        tree.expandPath(filenamePath)
    case EndOfStreamEvent() =>
      ()
    case ButtonClicked(button) =>
      Option(tree.getSelectionPath) match
        case None =>
          publish(InvalidFormEvent(NonEmptyList.one("No tree row selected")))
        case Some(treePath) =>
          val path = pathTreePaths(treePath).combineAll
          val copyable =
            if button eq copyFilename then
              path.getFileName.some
            else if button eq copyPath then
              path.some
            else
              None
          copyable.foreach(path => copyToClipboard(path.toString))
    case event =>
      println(s"MatchesTree: received unhandled event: $event")
  }

  private def render(matchPosition: MatchPosition): String =
    import matchPosition.*

    s"$lineNumber, $columnNumber : ${lineText.trim.take(100)}"
