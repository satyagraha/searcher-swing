package org.satyagraha.searcher
package view

import helpers.*
import io.*
import model.*
import ui.MainWindow

import java.awt.Dimension
import javax.swing
import javax.swing.*
import javax.swing.JOptionPane.*
import javax.swing.SwingUtilities.invokeLater
import scala.swing.event.Event

class Ui(uiPreferences: UiPreferences,
         ioSupport: IoSupport) extends PubSub {

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

  val matchesTree = new MatchesTree
  mainWindow.getScrollPane.setViewportView(matchesTree.tree)

  subscribeTo(matchesTree)

  reactions += {
    case InvalidFormEvent(messages) =>
      invokeLater: () =>
        showMessageDialog(frame, messages.toList.mkString(", "), "Form Error", WARNING_MESSAGE)
    case UiStateEvent(uiState) =>
      invokeLater: () =>
        mainWindow.uiStateSet(uiState)
    case cse@ControlStateEvent(controlState) =>
      invokeLater: () =>
        mainWindow.controlStateSet(controlState == ControlState.Idle)
        matchesTree.handle(cse)
    case EditEvent(path, matchPosition) =>
      ioSupport.launchEditor(path, matchPosition)
    case event =>
      //      println(s"Ui: received unhandled event: $event")
      matchesTree.handle(event)
  }

  def run(): Unit =
    frame.setVisible(true)
}
