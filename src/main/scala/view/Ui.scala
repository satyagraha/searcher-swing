package org.satyagraha.searcher
package view

import engine.*
import helpers.*
import io.*
import ui.MainWindow

import java.awt.{Dimension, Font}
import java.awt.event.{ActionEvent, ActionListener, KeyEvent}
import javax.swing
import javax.swing.*
import javax.swing.JOptionPane.*
import javax.swing.SwingUtilities.invokeLater
import javax.swing.plaf.FontUIResource
import scala.jdk.CollectionConverters.*
import scala.swing.event.Event
import scala.util.Try

case class UiPreferences(title: String,
                         width: Int,
                         height: Int,
                         fontEncoded: String,
                         fontScaling: Float)

class Ui(uiPreferences: UiPreferences,
         ioSupport: IoSupport) extends PubSub:

  import uiPreferences.*

  private val lookAndFeelDefaults = UIManager.getLookAndFeelDefaults
  lookAndFeelDefaults.keys.asScala foreach: key =>
    UIManager.get(key) match
      case font: FontUIResource =>
        val newFont = fontEncoded.trimmedNonBlank.fold(font.deriveFont(font.getSize * fontScaling))(Font.decode)
        val fontUIResource = new FontUIResource(newFont)
        lookAndFeelDefaults.put(key, fontUIResource)
      case _ =>

  private val mainWindow = new MainWindow()

  private def doAction(action: => Unit): ActionListener =
    (e: ActionEvent) => action

  private def setupButton(button: JButton, keyStroke: KeyStroke, handler: => Unit): Unit =
    val action = new AbstractAction():
      override def actionPerformed(e: ActionEvent): Unit =
        handler
        
    val actionMapKey = button.getText
    button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionMapKey)
    button.getActionMap.put(actionMapKey, action)
    button.addActionListener(doAction(handler))
    
  mainWindow.browseButton.addActionListener(doAction(handle(BrowseDirEvent())))
  
  setupButton(mainWindow.startButton, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), publish(StartEvent(mainWindow.uiState())))
  setupButton(mainWindow.stopButton, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), publish(StopEvent()))

  private val frame = new JFrame(title)
  frame.setContentPane(mainWindow.rootPanel)
  frame.setPreferredSize(new Dimension(width, height))
  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  frame.pack()
  frame.setLocationRelativeTo(null)

  Try:
    val icon = UIManager.getIcon("OptionPane.questionIcon")
    frame.setIconImage(icon.asInstanceOf[ImageIcon].getImage)

  private val matchesTree = new MatchesTree
  mainWindow.scrollPane.setViewportView(matchesTree.tree)

  subscribeTo(matchesTree)

  reactions += {
    case BrowseDirEvent() =>
      invokeLater: () =>
        mainWindow.browseDir()
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
    case EndOfStreamEvent() =>
      ()
    case event =>
      //      println(s"Ui: received unhandled event: $event")
      matchesTree.handle(event)
  }

  def run(): Unit =
    frame.setVisible(true)
