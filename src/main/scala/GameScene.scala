package flappyboy


import javafx.scene.input.{MouseButton, MouseEvent => JFXMouseEvent}
import scalafx.animation.AnimationTimer
import scalafx.beans.property.DoubleProperty
import scalafx.geometry.Pos
import scalafx.scene.control.{Alert, ButtonType, Label, Menu, MenuBar, MenuItem}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{StackPane, VBox}
import scalafx.scene.media.AudioClip
import scalafx.scene.{Group, Scene}

import java.io.{File, PrintWriter}
import scala.io.Source


object GameScene {
  // Boy properties
  private val boyX = DoubleProperty(50)
  private val boyY = DoubleProperty(250)
  private val boyRadius = 10.0

  private var velocity = 0.0
  private val gravity = 0.1
  private var flapStrength = -5.0

  // Pipe properties
  private val pipeWidth = 180
  private val pipeHeight = 800
  private val gap = 320

  // Pipe data
  private var pipes: List[(Double, Double, Boolean)] = List()

  // Score variables
  private var score = 0
  private var bestScore = loadBestScore()
  private val scoreLabel = new Label {
    text = s"Score: $score"
    style = """-fx-font-size: 16pt; -fx-font-family: "Arial Black"; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 1);"""
  }
  private val bestScoreLabel = new Label {
    text = s"Best: $bestScore"
    style = """-fx-font-size: 16pt; -fx-font-family: "Arial Black"; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 1);"""
  }

  private var baseSpeed = 1.0
  private var waitingToStart = true

  // Load images
  private val topPipeImage = new Image(getClass.getResource("/images/top.png").toString)
  private val bottomPipeImage = new Image(getClass.getResource("/images/bottom.png").toString)
  private val boyImage = new Image(getClass.getResource("/images/boy.png").toString)
  private val gameBackgroundImage = new Image(getClass.getResource("/images/gameb.png").toString)
  val imagePath = getClass.getResource("/images/background.jpg")


  private val tracks = List(getClass.getResource("/music/track1.mp3").toString)
  private var currentAudioClip: Option[AudioClip] = None
  private var currentDifficulty: String = "Easy"

  def playTrack(index: Int): Unit = {
    if (index >= 0 && index < tracks.length) {
      currentAudioClip.foreach(_.stop())
      val newClip = new AudioClip(tracks(index))
      newClip.setCycleCount(AudioClip.Indefinite)
      newClip.setVolume(0.2)
      newClip.play()
      currentAudioClip = Some(newClip)
    }
  }

  def stopCurrentTrack(): Unit = {
    currentAudioClip.foreach(_.stop())
    currentAudioClip = None
  }

  def createPipePair(x: Double): (Double, Double, Boolean) = {
    val topPipeY = -pipeHeight + (math.random() * 200)
    (x, topPipeY, false)
  }

  def resetGame(): Unit = {
    boyY.value = 250
    velocity = 0.0
    pipes = List(createPipePair(300), createPipePair(1100))
    score = 0
    scoreLabel.text = s"Score: $score"
    waitingToStart = true
    setDifficulty(currentDifficulty)
  }

  def create(): Scene = {
    resetGame()
    new Scene(1280, 720) {
      val gameBackground = new ImageView(gameBackgroundImage) {
        fitWidth.bind(width)
        fitHeight.bind(height)
      }

      val boy = new ImageView(boyImage) {
        fitWidth = boyRadius * 6
        fitHeight = boyRadius * 6
        x.bind(boyX - boyRadius)
        y.bind(boyY - boyRadius)
      }

      def createPipeImages(pipes: List[(Double, Double, Boolean)]): List[ImageView] = {
        pipes.flatMap { case (pipeX, topPipeY, _) =>
          val bottomPipeY = topPipeY + pipeHeight + gap
          List(
            new ImageView(topPipeImage) {
              fitWidth = pipeWidth
              fitHeight = pipeHeight
              x = pipeX
              y = topPipeY
            },
            new ImageView(bottomPipeImage) {
              fitWidth = pipeWidth
              fitHeight = pipeHeight
              x = pipeX
              y = bottomPipeY
            }
          )
        }
      }
      // Create a MenuBar
      val menuBar = new MenuBar {
        val gameMenu = new Menu("Game") {
          items = List(
            new MenuItem("Main Menu") {
              onAction = _ => {
                val exitAlert = new Alert(Alert.AlertType.Confirmation) {
                  title = "Return to Main Menu"
                  headerText = "Are you sure you want to return to the main menu?"
                  contentText = "You will lose your current game progress."
                }
                exitAlert.getButtonTypes.setAll(ButtonType.Yes, ButtonType.No)
                val result = exitAlert.showAndWait()
                if (result.contains(ButtonType.Yes)) {
                  timer.stop()
                  resetGame()
                  FlappyBoyApp.stage.scene = MenuScene.createMenuScene()
                }
              }
            },
            new MenuItem("Exit") {
              onAction = _ => {
                val exitAlert = new Alert(Alert.AlertType.Confirmation) {
                  title = "Exit Game"
                  headerText = "Are you sure you want to exit the game?"
                  contentText = "Your progress will not be saved."
                }
                exitAlert.getButtonTypes.setAll(ButtonType.Yes, ButtonType.No)
                val result = exitAlert.showAndWait()
                if (result.contains(ButtonType.Yes)) {
                  System.exit(0)
                }
              }
            }
          )
        }
        menus = List(gameMenu)
      }

      val mainLayout = new VBox {
        children = List(menuBar, scoreLabel, bestScoreLabel)
        alignment = Pos.TopLeft
        spacing = 10
      }

      val promptLabel = new Label("Press the left mouse button to start") {
        style = """-fx-font-size: 20pt; -fx-font-family: "Arial Black"; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 5);"""
      }

      val centerPane = new StackPane {
        children = promptLabel
        alignment = Pos.Center
      }
      centerPane.prefWidth.bind(width)
      centerPane.prefHeight.bind(height)

      val contentGroup = new Group()
      contentGroup.children.addAll(gameBackground, boy, centerPane)

      val rootPane = new VBox {
        children = List(mainLayout, contentGroup)
      }

      rootPane.prefWidth = 1920
      rootPane.prefHeight = 1080

      onMouseClicked = (event: JFXMouseEvent) => {
        if (event.getButton == MouseButton.PRIMARY) {
          if (waitingToStart) {
            waitingToStart = false
            centerPane.visible = false
          }
          velocity = flapStrength
        }
      }

      var timer: AnimationTimer = _
      timer = AnimationTimer { _ =>
        if (!waitingToStart) {
          velocity += gravity
          boyY.value += velocity

          pipes = pipes.map { case (pipeX, topPipeY, passed) =>
            val newX = pipeX - baseSpeed
            if (newX < -pipeWidth) {
              (width.value, -pipeHeight + (math.random() * (height.value - gap - 100)), false)
            } else {
              (newX, topPipeY, passed)
            }
          }

          pipes = pipes.map { case (pipeX, topPipeY, passed) =>
            if (!passed && pipeX + pipeWidth < boyX.value) {
              score += 1
              scoreLabel.text = s"Score: $score"
              if (score > bestScore) {
                bestScore = score
                bestScoreLabel.text = s"Best: $bestScore"
                saveBestScore(bestScore)
              }
              (pipeX, topPipeY, true)
            } else {
              (pipeX, topPipeY, passed)
            }
          }
        }

        val pipeImages = createPipeImages(pipes)

        contentGroup.children.clear()
        contentGroup.children.addAll(gameBackground, boy)
        pipeImages.foreach(imageView => contentGroup.children.add(imageView))
        contentGroup.children.add(centerPane)

        val boyBounds = boy.boundsInParent()
        val collided = pipeImages.exists { imageView =>
          imageView.boundsInParent().intersects(boyBounds)
        }

        if (collided || boyY.value < 0 || boyY.value > height.value) {
          timer.stop()
          FlappyBoyApp.stage.scene = GameOverScene.create(score, bestScore, currentDifficulty)
        }
      }

      timer.start()

      content = List(contentGroup, rootPane)
    }
  }



  def setDifficulty(difficulty: String): Unit = {
    currentDifficulty = difficulty
    difficulty match {
      case "Easy" => (baseSpeed = 2.0, flapStrength = -5.0)
      case "Medium" => (baseSpeed = 5.0, flapStrength = -3.0)
      case "Hard" => (baseSpeed = 6.0, flapStrength = -3.0)
      case _ => (baseSpeed = 2.0, flapStrength = -5.0) // Default to easy
    }
  }

  def saveBestScore(score: Int): Unit = {
    val writer = new PrintWriter(new File("bestScore.txt"))
    writer.write(score.toString)
    writer.close()
  }

  def loadBestScore(): Int = {
    val file = new File("bestScore.txt")
    if (file.exists()) {
      val source = Source.fromFile(file)
      val score = source.getLines().next().toInt
      source.close()
      score
    } else {
      0
    }
  }
}
