package flappyboy

import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.JFXApp

object FlappyBoyApp extends JFXApp {
  stage = new PrimaryStage {
    title = "Flappy Boy"
    scene = MenuScene.createMenuScene()
  }
}
