package flappyboy

import java.io.{BufferedWriter, FileWriter, FileReader, BufferedReader}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, Label, ListView}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.Scene

object LeaderboardScene {

  private var leaderboardEntries: Map[String, List[(String, Int)]] = Map(
    "Easy" -> List(),
    "Medium" -> List(),
    "Hard" -> List()
  )

  private val filePaths: Map[String, String] = Map(
    "Easy" -> "leaderboard_easy.txt",
    "Medium" -> "leaderboard_medium.txt",
    "Hard" -> "leaderboard_hard.txt"
  )

  def create(difficulty: String): Scene = {
    initialize(difficulty) // Ensure leaderboard data is loaded when creating the scene

    new Scene(1280, 720) {
      fill = Color.Black

      val entries = leaderboardEntries.getOrElse(difficulty, List())
      val leaderboardBuffer = ObservableBuffer(entries.map { case (name, score) => s"$name: $score" }: _*)

      val leaderboardListView = new ListView[String](leaderboardBuffer) {
        prefWidth = 500
        prefHeight = 400
        style = """
          -fx-background-color: #ffffff;
          -fx-border-color: #007BFF;
          -fx-border-width: 2px;
          -fx-font-size: 16pt;
          -fx-font-family: "Arial";
        """
      }

      val backButton = new Button("Back to Menu") {
        onAction = _ => {
          FlappyBoyApp.stage.scene = MenuScene.createMenuScene()
        }
        style = """
          -fx-font-size: 18pt;
          -fx-font-family: "Arial Black";
          -fx-background-color: #007BFF;
          -fx-text-fill: white;
          -fx-padding: 10 30;
          -fx-background-radius: 10;
          -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);
        """
      }

      // Load background image
      val backgroundImagePath = getClass.getResource("/images/background.jpg").toString
      val backgroundImage = new Image(backgroundImagePath)
      val backgroundImageView = new ImageView(backgroundImage) {
        fitWidth.bind(width)
        fitHeight.bind(height)
      }

      // VBox for layout
      val vbox = new VBox(20) {
        children = List(
          new Label(s"Leaderboard - $difficulty") {
            style = """
              -fx-font-size: 36pt;
              -fx-font-family: "Arial Black";
              -fx-text-fill: #FFD700;
              -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 5);
              -fx-text-alignment: center;
            """
          },
          leaderboardListView,
          backButton
        )
        alignment = Pos.Center
        spacing = 20
        padding = scalafx.geometry.Insets(20)
      }

      // StackPane to hold background and other elements
      val rootPane = new StackPane {
        children = List(backgroundImageView, vbox)
      }

      content = rootPane
    }
  }

  def addEntry(name: String, score: Int, difficulty: String): Unit = {
    val updatedEntries = leaderboardEntries.getOrElse(difficulty, List()) :+ (name, score)
    leaderboardEntries = leaderboardEntries.updated(difficulty, updatedEntries.sortBy(-_._2).take(10))
    saveLeaderboard(difficulty)
  }

  private def saveLeaderboard(difficulty: String): Unit = {
    val filePath = filePaths(difficulty)
    val entries = leaderboardEntries(difficulty)
    val writer = new BufferedWriter(new FileWriter(filePath))
    try {
      entries.foreach { case (name, score) =>
        writer.write(s"$name,$score\n")
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      writer.close()
    }
  }

  private def loadLeaderboard(difficulty: String): Unit = {
    val filePath = filePaths(difficulty)
    val file = new java.io.File(filePath)
    if (file.exists()) {
      val reader = new BufferedReader(new FileReader(filePath))
      try {
        val entries = Iterator.continually(reader.readLine()).takeWhile(_ != null).toList
        leaderboardEntries = leaderboardEntries.updated(
          difficulty,
          entries.map { line =>
            val Array(name, score) = line.split(",")
            (name, score.toInt)
          }.sortBy(-_._2).take(10) // Keep top 10 scores
        )
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {
        reader.close()
      }
    }
  }

  def initialize(difficulty: String): Unit = {
    loadLeaderboard(difficulty)
  }
}
