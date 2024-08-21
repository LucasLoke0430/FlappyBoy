package flappyboy

import scalafx.geometry.Pos
import scalafx.scene.control.{Alert, Button, Label, TextInputDialog}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType

import scala.io.Source
import scala.util.matching.Regex

object GameOverScene {
  private val gameBackgroundImage = new Image(getClass.getResource("/images/gameb.png").toString)

  def create(score: Int, bestScore: Int, difficulty: String): Scene = {
    new Scene(1280, 720) {
      fill = Color.Black

      val gameOverBackground = new ImageView(gameBackgroundImage) {
        fitWidth.bind(width)
        fitHeight.bind(height)
      }

      val gameoverscore = new Label(s"Score: $score") {
        style = """
          -fx-font-size: 48pt;
          -fx-font-family: "Arial Black";
          -fx-text-fill: white;
          -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 5);
          -fx-text-alignment: center;
        """
      }

      val gameOverLabel = new Label(s"Game Over\nBest Score: $bestScore") {
        style = """
          -fx-font-size: 48pt;
          -fx-font-family: "Arial Black";
          -fx-text-fill: white;
          -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 5);
          -fx-text-alignment: center;
        """
      }

      val restartButton = new Button("Restart") {
        onAction = _ => {
          FlappyBoyApp.stage.scene = GameScene.create()
        }
        style = """
          -fx-font-size: 22pt;
          -fx-font-family: "Arial Black";
          -fx-background-color: #28A745;
          -fx-text-fill: white;
          -fx-padding: 10 30;
          -fx-background-radius: 10;
          -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);
        """
      }

      val menuButton = new Button("Main Menu") {
        onAction = _ => {
          FlappyBoyApp.stage.scene = MenuScene.createMenuScene()
        }
        style = """
          -fx-font-size: 22pt;
          -fx-font-family: "Arial Black";
          -fx-background-color: #DC3545;
          -fx-text-fill: white;
          -fx-padding: 10 30;
          -fx-background-radius: 10;
          -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);
        """
      }

      val leaderboardButton = new Button("Enter Leaderboard") {
        onAction = _ => {
          // Show a dialog to enter the player's name
          val dialog = new TextInputDialog() {
            title = "Enter Your Name"
            headerText = "Leaderboard Entry"
            contentText = "Please enter your name:"
          }

          dialog.showAndWait() match {
            case Some(name) if isValidName(name) =>
              if (isNameTaken(name, difficulty)) {
                showNameTakenAlert()
              } else {
                LeaderboardScene.addEntry(name, score, difficulty)
                FlappyBoyApp.stage.scene = LeaderboardScene.create(difficulty)
              }
            case Some(_) =>
              showInvalidNameAlert()
            case None =>
              println("")
          }
        }
        style = """
          -fx-font-size: 22pt;
          -fx-font-family: "Arial Black";
          -fx-background-color: #007BFF;
          -fx-text-fill: white;
          -fx-padding: 10 30;
          -fx-background-radius: 10;
          -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);
        """
      }

      val buttonBox = new HBox(20) {
        children = List(restartButton, menuButton, leaderboardButton)
        alignment = Pos.Center
      }

      val vbox = new VBox(20) {
        children = List(gameOverLabel, gameoverscore, buttonBox)
        alignment = Pos.Center
        spacing = 2
      }

      val rootPane = new StackPane {
        children = List(gameOverBackground, vbox)
        alignment = Pos.Center
      }

      content = rootPane
    }
  }

  private def isValidName(name: String): Boolean = {
    // Define a regex pattern for valid names (letters, digits, and spaces)
    val namePattern: Regex = "^[a-zA-Z0-9 ]+$".r
    name.nonEmpty && namePattern.findFirstIn(name).isDefined
  }

  private def isNameTaken(name: String, difficulty: String): Boolean = {
    val filePath = s"leaderboard_${difficulty.toLowerCase}.txt"
    try {
      val lines = Source.fromFile(filePath).getLines()
      lines.exists(line => line.split(",")(0) == name)
    } catch {
      case _: java.io.FileNotFoundException => false
    }
  }

  private def showInvalidNameAlert(): Unit = {
    val alert = new Alert(AlertType.Error) {
      title = "Invalid Name"
      headerText = "The name you entered is invalid."
      contentText = "Please enter a name with only letters, numbers, and spaces. The name cannot be empty."
    }
    alert.showAndWait()
  }

  private def showNameTakenAlert(): Unit = {
    val alert = new Alert(AlertType.Error) {
      title = "Name Taken"
      headerText = "The name you entered is already taken."
      contentText = "Please choose a different name."
    }
    alert.showAndWait()
  }
}
