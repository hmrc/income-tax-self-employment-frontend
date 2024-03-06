/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
