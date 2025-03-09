package org.satyagraha.searcher
package view

import engine.*
import helpers.{*, given}
import search.*

import cats.data.NonEmptyList
import cats.implicits.given
import com.google.common.base.CaseFormat
import com.typesafe.scalalogging.StrictLogging

import java.awt.event.{MouseAdapter, MouseEvent}
import java.nio.file.Path
import javax.swing.JTree
import javax.swing.SwingUtilities.{invokeLater, isRightMouseButton}
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel, TreePath}
import scala.collection.concurrent.TrieMap
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

class MatchesTree extends PubSub with StrictLogging:

  import MatchesTree.*
  import ContextMenuChoice.*

  private val treeModel = new DefaultTreeModel(null)
  val tree = new JTree(treeModel)

  private var baseDirNode: DefaultMutableTreeNode = _
  private var subDirNode: DefaultMutableTreeNode = _
  private var filenameNode: DefaultMutableTreeNode = _
  
  private val nodeMap = new TrieMap[Seq[Path], DefaultMutableTreeNode]()

  private class ContextMenuItem(title: String,
                                contextMenuChoice: ContextMenuChoice) extends MenuItem(title):
    override def publish(e: Event): Unit =
      MatchesTree.this.handle(ContextMenuEvent(contextMenuChoice))

  private val menuItems = ContextMenuChoice.values.toSeq.map: cmc =>
    val menuText = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, cmc.toString)
      .replace("-", " ").capitalize
    ContextMenuItem(menuText, cmc)

  private val popupMenu = new PopupMenu:
    contents ++= menuItems

  private val wrappedtree = Component.wrap(tree)

  private val mouseListener = new MouseAdapter:
    override def mouseClicked(e: MouseEvent): Unit =
      if isRightMouseButton(e) then
        popupMenu.show(wrappedtree, e.getX, e.getY)

  tree.addMouseListener(mouseListener)

  private def expandTo(leafNode: DefaultMutableTreeNode): Unit =
    val parentNodes = leafNode.getPath.toSeq.dropRight(1)
    if parentNodes.nonEmpty then
      val parentPath = new TreePath(parentNodes.toArray[AnyRef])
      tree.expandPath(parentPath)

  reactions +=
    locally:
      case ControlStateEvent(controlState) =>
        if controlState == ControlState.Running then
          logger.debug("Running")
          treeModel.setRoot(null)
          nodeMap.clear()
      case BaseDirEvent(baseDir) =>
        invokeLater: () =>
          logger.debug(s"baseDir: $baseDir")
          baseDirNode = new PathTreeNode(NodeContent(baseDir))
          treeModel.setRoot(baseDirNode)
          nodeMap.put(Seq.empty, baseDirNode)
          expandTo(baseDirNode)
      case SubDirEvent(subDir) =>
        invokeLater: () =>
          logger.debug(s"subDir: $subDir")
          subDir.elements.inits.toSeq.reverse foreach: pathPrefix =>
            nodeMap.get(pathPrefix) match
              case Some(node) =>
                subDirNode = node
              case None =>
                val lastDir = pathPrefix.last
                val dirNode = new PathTreeNode(NodeContent(lastDir))
                treeModel.insertNodeInto(dirNode, subDirNode, subDirNode.getChildCount)
                nodeMap.put(pathPrefix, dirNode)
                subDirNode = dirNode
          expandTo(subDirNode)
                
      case FilenameEvent(filename) =>
        invokeLater: () =>
          logger.debug(s"filename: $filename")
          filenameNode = new PathTreeNode(NodeContent(filename))
          treeModel.insertNodeInto(filenameNode, subDirNode, subDirNode.getChildCount)
          expandTo(filenameNode)

      case MatchEvent(matchPosition) =>
        invokeLater: () =>
          logger.debug(s"matchPosition: $matchPosition")
          val matchPositionNode = new PathTreeNode(NodeContent(matchPosition))
          treeModel.insertNodeInto(matchPositionNode, filenameNode, filenameNode.getChildCount)
          expandTo(matchPositionNode)

      case ContextMenuEvent(contextMenuChoice) =>
        import Clipboard.*

        logger.debug(s"contextMenuChoice: $contextMenuChoice")
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
              case CopyLine =>
                matchPosition.foreach: matched =>
                  copyToClipboard(matched.lineText)
              case EditSelected =>
                publish(EditEvent(fullPath, matchPosition))

      case event =>
        logger.debug(s"MatchesTree: received unhandled event: $event")
