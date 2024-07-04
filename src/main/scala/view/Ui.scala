package org.satyagraha.searcher
package view

import ui.MainWindow
import domain.*
import helpers.{given, *}
import io.*
import model.*

import cats.data.NonEmptyList
import cats.implicits.given

import java.awt.Dimension
import java.awt.event.{MouseAdapter, MouseEvent}
import java.nio.file.Path
import javax.swing
import javax.swing.JOptionPane.*
import javax.swing.SwingUtilities.{invokeLater, isRightMouseButton}
import javax.swing.*
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel, TreePath}
import scala.swing.{Component, MenuItem, PopupMenu, Publisher}
import scala.swing.event.{ButtonClicked, Event}

class Ui(uiPreferences: UiPreferences) extends PubSub {
  import uiPreferences.*

  val frame = new JFrame(title)
  val mainWindow = new MainWindow(this)
  frame.setContentPane(mainWindow.rootPanel)
  frame.setPreferredSize(new Dimension(width, height));
  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  frame.pack()
  frame.setLocationRelativeTo(null)

  val icon = UIManager.getIcon("OptionPane.questionIcon")
  frame.setIconImage(icon.asInstanceOf[ImageIcon].getImage)

  val treeModel = new DefaultTreeModel(null)
  val tree = new JTree(treeModel)
  mainWindow.getScrollPane.setViewportView(tree)

  var baseDirNode: DefaultMutableTreeNode = _
  var subDirNode: DefaultMutableTreeNode = _
  var filenameNode: DefaultMutableTreeNode = _

  subscribeTo(this)

  trait Republish:
    self: Publisher =>
    override def publish(e: Event): Unit =
      Ui.this.publish(e)

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

  class PathTreeNode(val content: Path | String) extends DefaultMutableTreeNode(content)

  def pathTreePaths(treePath: TreePath): List[Path] =
    treePath.getPath.toList.map(_.asInstanceOf[PathTreeNode].content).collect:
      case path: Path => path

  reactions += {
    case InvalidFormEvent(messages) =>
      invokeLater: () =>
        showMessageDialog(frame, messages.toList.mkString(", "), "Form Error", WARNING_MESSAGE)
    case UiStateEvent(uiState) =>
      invokeLater: () =>
        mainWindow.uiStateSet(uiState)
    case ControlStateEvent(controlState) =>
      invokeLater: () =>
        mainWindow.controlStateSet(controlState == ControlState.Idle)
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
      println(s"Ui: received unhandled event: $event")
  }

  private def render(matchPosition: MatchPosition): String =
    import matchPosition.*

    s"$lineNumber, $columnNumber : ${lineText.trim.take(100)}"

  def run(): Unit =
    frame.setVisible(true)
}
