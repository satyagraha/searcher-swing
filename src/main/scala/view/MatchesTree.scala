package org.satyagraha.searcher
package view

import engine.*
import helpers.{*, given}
import search.*

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

  private case class NodeContent(content: Path | MatchPosition):
    override def toString: String =
      content match
        case path: Path =>
          path.toString
        case matchPosition: MatchPosition =>
          import matchPosition.*

          s"$lineNumber, $columnNumber : ${lineText.trim.take(100)}"


  private class PathTreeNode(val nodeContent: NodeContent) extends DefaultMutableTreeNode(nodeContent)

  private def pathTreePaths(treePath: TreePath): (List[Path], Option[MatchPosition]) =
    val nodeContents = treePath.getPath.toList.map(_.asInstanceOf[PathTreeNode].nodeContent.content)
    val paths = nodeContents.collect:
      case path: Path => path
    val matchPosition = nodeContents.lastOption.collect:
      case matchPosition: MatchPosition => matchPosition
    (paths, matchPosition)

class MatchesTree extends PubSub:

  import MatchesTree.*
  import ContextMenuChoice.*

  private val treeModel = new DefaultTreeModel(null)
  val tree = new JTree(treeModel)

  private var baseDirNode: DefaultMutableTreeNode = _
  private var subDirNode: DefaultMutableTreeNode = _
  private var filenameNode: DefaultMutableTreeNode = _

  private class ContextMenuItem(title: String,
                                contextMenuChoice: ContextMenuChoice) extends MenuItem(title):
    override def publish(e: Event): Unit =
      MatchesTree.this.handle(ContextMenuEvent(contextMenuChoice))

  private val copyFilename = new ContextMenuItem("Copy filename", CopyFilename)
  private val copyRelativePath = new ContextMenuItem("Copy relative path", CopyRelativePath)
  private val copyFullPath = new ContextMenuItem("Copy full path", CopyFullPath)
  private val editSelected = new ContextMenuItem("Edit", EditSelected)
  private val popupMenu = new PopupMenu:
    contents ++= Seq(copyFilename, copyRelativePath, copyFullPath, editSelected)

  private val wrappedtree = Component.wrap(tree)

  private val mouseListener = new MouseAdapter:
    override def mouseClicked(e: MouseEvent): Unit =
      if isRightMouseButton(e) then
        popupMenu.show(wrappedtree, e.getX, e.getY)

  tree.addMouseListener(mouseListener)

  reactions +=
    locally:
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
      case ContextMenuEvent(contextMenuChoice) =>
        import Clipboard.*

        Option(tree.getSelectionPath) match
          case None =>
            publish(InvalidFormEvent(NonEmptyList.one("No tree row selected")))
          case Some(treePath) =>
            val (paths, matchPosition) = pathTreePaths(treePath)
            val fullPath = paths.combineAll
            contextMenuChoice match
              case CopyFilename =>
                copyToClipboard(fullPath.getFileName.toString)
              case CopyRelativePath =>
                paths match
                  case head +: tail =>
                    val relativePath = tail.combineAll
                    copyToClipboard(relativePath.toString)
                  case _ =>
                    copyToClipboard("")
              case CopyFullPath =>
                copyToClipboard(fullPath.toString)
              case EditSelected =>
                publish(EditEvent(fullPath, matchPosition))
      case event =>
        println(s"MatchesTree: received unhandled event: $event")
