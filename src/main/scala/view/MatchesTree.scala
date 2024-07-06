package org.satyagraha.searcher
package view

import domain.*
import helpers.{*, given}
import io.*
import model.*

import cats.data.NonEmptyList
import cats.implicits.given

import java.awt.event.{MouseAdapter, MouseEvent}
import java.nio.file.Path
import javax.swing.JTree
import javax.swing.SwingUtilities.{invokeLater, isRightMouseButton}
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel, TreePath}
import scala.swing.event.Event
import scala.swing.{Component, MenuItem, PopupMenu}

object MatchesTree:

  case class NodeContent(content: Path | MatchPosition):
    override def toString: String =
      content match
        case path: Path =>
          path.toString
        case matchPosition: MatchPosition =>
          import matchPosition.*

          s"$lineNumber, $columnNumber : ${lineText.trim.take(100)}"


  class PathTreeNode(val nodeContent: NodeContent) extends DefaultMutableTreeNode(nodeContent)

  def pathTreePaths(treePath: TreePath): (Path, Option[MatchPosition]) =
    val nodeContents = treePath.getPath.toList.map(_.asInstanceOf[PathTreeNode].nodeContent.content)
    val paths = nodeContents.collect:
      case path: Path => path
    val matchPosition = nodeContents.lastOption.collect:
      case matchPosition: MatchPosition => matchPosition
    (paths.combineAll, matchPosition)

class MatchesTree extends PubSub:
  import MatchesTree.*
  import ContextMenuChoice.*

  val treeModel = new DefaultTreeModel(null)
  val tree = new JTree(treeModel)

  var baseDirNode: DefaultMutableTreeNode = _
  var subDirNode: DefaultMutableTreeNode = _
  var filenameNode: DefaultMutableTreeNode = _

  class ContextMenuItem(title: String,
                        contextMenuChoice: ContextMenuChoice) extends MenuItem(title):
      override def publish(e: Event): Unit =
        MatchesTree.this.handle(ContextMenuEvent(contextMenuChoice))

  val copyFilename = new ContextMenuItem("Copy filename", CopyFilename)
  val copyPath = new ContextMenuItem("Copy path", CopyPath)
  val editSelected = new ContextMenuItem("Edit", EditSelected)
  val popupMenu = new PopupMenu:
    contents ++= Seq(copyFilename, copyPath, editSelected)

  val stree = Component.wrap(tree)

  val mouseListener = new MouseAdapter:
    override def mouseClicked(e: MouseEvent): Unit =
      if isRightMouseButton(e) then
        popupMenu.show(stree, e.getX, e.getY)

  tree.addMouseListener(mouseListener)

  reactions += {
    case ControlStateEvent(controlState) =>
      if controlState == ControlState.Running then
        treeModel.setRoot(null)
    case BaseDirEvent(baseDir) =>
      invokeLater: () =>
        baseDirNode = new PathTreeNode(NodeContent(baseDir))
        treeModel.setRoot(baseDirNode)
    case SubDirEvent(subDir) =>
      invokeLater: () =>
        subDirNode = new PathTreeNode(NodeContent(subDir))
        treeModel.insertNodeInto(subDirNode, baseDirNode, baseDirNode.getChildCount)
        tree.expandPath(new TreePath(baseDirNode))
    case FilenameEvent(filename) =>
      invokeLater: () =>
        filenameNode = new PathTreeNode(NodeContent(filename))
        treeModel.insertNodeInto(filenameNode, subDirNode, subDirNode.getChildCount)
        val nodes = Array[AnyRef](baseDirNode, subDirNode)
        val subDirPath = new TreePath(nodes)
        tree.expandPath(subDirPath)
    case MatchEvent(matchPosition) =>
      invokeLater: () =>
        val matchPositionNode = new PathTreeNode(NodeContent(matchPosition))
        treeModel.insertNodeInto(matchPositionNode, filenameNode, filenameNode.getChildCount)
        val nodes = Array[AnyRef](baseDirNode, subDirNode, filenameNode)
        val filenamePath = new TreePath(nodes)
        tree.expandPath(filenamePath)
//    case EndOfStreamEvent() =>
//      ()
    case ContextMenuEvent(contextMenuChoice) =>
      Option(tree.getSelectionPath) match
        case None =>
          publish(InvalidFormEvent(NonEmptyList.one("No tree row selected")))
        case Some(treePath) =>
          val (path, matchPosition) = pathTreePaths(treePath)
          contextMenuChoice match
            case CopyFilename =>
              copyToClipboard(path.getFileName.toString)
            case CopyPath =>
              copyToClipboard(path.toString)
            case EditSelected =>
              publish(EditEvent(path, matchPosition))
    case event =>
      println(s"MatchesTree: received unhandled event: $event")
  }
