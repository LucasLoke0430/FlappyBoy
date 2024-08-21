package flappyboy

import scalafx.Includes.jfxDialogPane2sfx
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Alert, Button, ButtonType, Dialog, Label, Menu, MenuBar, MenuItem}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.Scene
import scalafx.scene.media.AudioClip
import scalafx.scene.text.{Font, Text}

object MenuScene {
  var currentAudioClip: Option[AudioClip] = None
  val tracks = List(
    getClass.getResource("/music/track1.mp3").toString
  )
  var currentTrackIndex = 0
  val playIcon = new Image(getClass.getResource("/images/play.png").toString)
  val stopIcon = new Image(getClass.getResource("/images/stop.png").toString)

  def playTrack(index: Int): Unit = {
    if (index >= 0 && index < tracks.length) {
      currentAudioClip.foreach(_.stop()) // Stop the current track if any
      val newClip = new AudioClip(tracks(index))
      newClip.setCycleCount(AudioClip.Indefinite) // Loop the track
      newClip.setVolume(0.2)
      newClip.play()
      currentAudioClip = Some(newClip)
    }
  }

  def stopCurrentTrack(): Unit = {
    currentAudioClip.foreach(_.stop())
    currentAudioClip = None
  }

  // Call this method to start music when the application starts
  def startBackgroundMusic(): Unit = {
    playTrack(currentTrackIndex)
  }

  val difficultyLabel = new Label {
    text = "Difficulty: Easy"
    style = """-fx-font-size: 16pt; -fx-font-family: "Arial Black"; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 1);"""
  }

  def createMenuBar(): MenuBar = {
    val gameMenu = new Menu("Game")
    val startGameItem = new MenuItem("Start Game")
    val exitItem = new MenuItem("Exit")

    startGameItem.onAction = _ => {
      FlappyBoyApp.stage.scene = GameScene.create()
    }

    exitItem.onAction = _ => {
      val exitAlert = new Alert(Alert.AlertType.Confirmation) {
        title = "Exit Confirmation"
        headerText = "Are you sure you want to exit?"
        contentText = "Your progress will be lost."
      }
      exitAlert.getButtonTypes.setAll(ButtonType.Yes, ButtonType.No)

      val result = exitAlert.showAndWait()
      if (result.contains(ButtonType.Yes)) {
        System.exit(0)
      }
    }

    gameMenu.items = List(startGameItem, exitItem)

    val helpMenu = new Menu("Help")
    val aboutItem = new MenuItem("About") {
      onAction = _ => showAboutDialog()  // Call the custom dialog method
    }
    helpMenu.items = List(aboutItem)

    val difficultyMenu = new Menu("Difficulty")
    val easyItem = new MenuItem("Easy") {
      onAction = _ => {
        GameScene.setDifficulty("Easy")
        difficultyLabel.text = "Difficulty: Easy"
      }
    }
    val mediumItem = new MenuItem("Medium") {
      onAction = _ => {
        GameScene.setDifficulty("Medium")
        difficultyLabel.text = "Difficulty: Medium"
      }
    }
    val hardItem = new MenuItem("Hard") {
      onAction = _ => {
        GameScene.setDifficulty("Hard")
        difficultyLabel.text = "Difficulty: Hard"
      }
    }

    difficultyMenu.items = List(easyItem, mediumItem, hardItem)

    new MenuBar {
      menus = List(gameMenu, helpMenu, difficultyMenu)
    }
  }

  def createMenuScene(): Scene = {
    new Scene(1280, 720) {
      fill = Color.LightBlue
      val menuBar = createMenuBar()

      val imagePath = getClass.getResource("/images/background.jpg")
      val backgroundImage = new ImageView(new Image(imagePath.toString)) {
        fitWidth <== width
        fitHeight <== height
      }

      val titleImagePath = getClass.getResource("/images/title.png")
      val titleImage = new ImageView(new Image(titleImagePath.toString)) {
        fitWidth = 800
        preserveRatio = true
      }

      val startButton = new Button("Start Game") {
        onAction = _ => {
          FlappyBoyApp.stage.scene = GameScene.create()
        }
        style =
          """
      -fx-font-size: 22pt;
      -fx-font-family: "Arial Black";
      -fx-background-color: #28A745;
      -fx-text-fill: white;
      -fx-padding: 10 30;
      -fx-background-radius: 10;
      -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);
    """
      }

      val exitButton = new Button("Exit") {
        onAction = _ => {
          val exitAlert = new Alert(Alert.AlertType.Confirmation) {
            title = "Exit Confirmation"
            headerText = "Are you sure you want to exit?"
            contentText = "Your progress will be lost."
          }
          exitAlert.getButtonTypes.setAll(
            ButtonType.Yes,
            ButtonType.No
          )

          val result = exitAlert.showAndWait()
          if (result.contains(ButtonType.Yes)) {
            System.exit(0)
          }
        }
        style =
          """
          -fx-font-size: 22pt;
          -fx-font-family: "Arial Black";
          -fx-background-color: #DC3545;
          -fx-text-fill: white;
          -fx-padding: 10 30;
          -fx-background-radius: 10;
          -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);
        """
      }

      val leaderboardButton = new Button("Leaderboard") {
        onAction = _ => {
          val difficulty = difficultyLabel.text.value.split(": ").last // Extract the current difficulty
          FlappyBoyApp.stage.scene = LeaderboardScene.create(difficulty)
        }
        style =
          """
          -fx-font-size: 22pt;
          -fx-font-family: "Arial Black";
          -fx-background-color: #007BFF;
          -fx-text-fill: white;
          -fx-padding: 10 30;
          -fx-background-radius: 10;
          -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);
        """
      }

      val playMusicButton = new Button {
        graphic = new ImageView(playIcon) {
          fitWidth = 30
          fitHeight = 30
        }
        onAction = _ => playTrack(currentTrackIndex)
        style =
          """
      -fx-background-color: grey;
      -fx-background-radius: 4;
      -fx-padding: 5;
    """
      }

      val stopMusicButton = new Button {
        graphic = new ImageView(stopIcon) {
          fitWidth = 30
          fitHeight = 30
        }
        onAction = _ => stopCurrentTrack()
        style =
          """
      -fx-background-color: grey;
      -fx-background-radius: 4;
      -fx-padding: 5;
    """
      }

      val menuBox = new VBox(20) {
        children = List(titleImage, startButton, leaderboardButton, exitButton)
        alignment = Pos.Center
        spacing = 20
        style = "-fx-padding: 50;"
      }

      val musicControlsBox = new HBox(10) {
        children = List(difficultyLabel, playMusicButton, stopMusicButton)
        alignment = Pos.TopRight
        style = "-fx-padding: 20;"
      }

      val rootPane = new StackPane {
        children = List(backgroundImage, new VBox {
          children = List(menuBar, musicControlsBox, menuBox)
        })
      }
      content = rootPane
    }
  }

  def showAboutDialog(): Unit = {
    // Create a new Dialog
    val dialog = new Dialog[ButtonType]() {
      title = "About"
      headerText = "Tutorial"
    }

    // Create content for the dialog
    val content = new VBox(10) {
      padding = Insets(15)
      children = List(
        new Label("Welcome to Flappy Boy!") {
          font = Font.font("Arial", 20)
          textFill = Color.Black
        },
        new Text("1. Start the game by clicking \"Start Game\" on the main menu or in the Menu Bar.") {
          font = Font.font("Arial", 16)
          fill = Color.Black
        },
        new Text("2. Control Flappy Boy by clicking the left mouse button to make him flap and rise. Navigate through moving pipes and avoid collisions.") {
          font = Font.font("Arial", 16)
          fill = Color.Black
        },
        new Text("3. You score 1 point for each pipe pair you pass. The game ends if Flappy Boy hits a pipe or falls off the screen.") {
          font = Font.font("Arial", 16)
          fill = Color.Black
        },
        new Text("4. On the game over screen, you can view your score and best score, enter the leaderboard, and choose to restart or return to the main menu.") {
          font = Font.font("Arial", 16)
          fill = Color.Black
        },
        new Text("5. Use the \"Play Music\" and \"Stop Music\" buttons to control background music.") {
          font = Font.font("Arial", 16)
          fill = Color.Black
        },
        new Text("6. Practice to improve your timing and achieve a higher score.") {
          font = Font.font("Arial", 16)
          fill = Color.Black
        }
      )
    }

    // Set content and button types for the dialog
    dialog.dialogPane().content = content
    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK)

    // Show the dialog
    dialog.showAndWait()
  }
  MenuScene.startBackgroundMusic()
}

