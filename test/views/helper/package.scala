package views

import play.twirl.api.Html

import java.nio.file.{Files, Paths}

package object helper {

  def writeHtmlToTempFile(html: Html): Unit = {
    val directoryPath = Paths.get("./test-output")
    if (!Files.exists(directoryPath)) {
      Files.createDirectory(directoryPath)
    }

    val filePath = directoryPath.resolve("test-actual-view.html")
    Files.write(filePath, html.body.getBytes(java.nio.charset.StandardCharsets.UTF_8))
    println(s"*ViewSpec saved file here: ${filePath.toAbsolutePath}")
    ()
  }
}
