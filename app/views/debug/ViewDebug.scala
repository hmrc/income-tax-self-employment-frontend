/*
 * Copyright 2025 HM Revenue & Customs
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

// app/views/debug/ViewDebug.scala
package views.debug

import play.api.Environment
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class ViewDebug @Inject() (environment: Environment, servicesConfig: ServicesConfig) {

  def appInfo(): String =
    s"""
       |<p style="font-size: 1.2em; color: #34495e; font-weight: bold;">
       |  ${servicesConfig.getString("appName")}
       |</p>
    """.stripMargin

  def stackTrace(): String =
    try
      throw new Exception("Debug stack trace")
    catch {
      case e: Exception =>
        val stackTrace = e.getStackTrace
        val res = stackTrace.collect {
          case x if x.getClassName.endsWith("View") =>
            viewStackElement(x, "#28a745", "View")
          case x if x.getClassName.endsWith("Controller") =>
            viewStackElement(x, "#007bff", "Controller")
        }
        s"""
           |<div class="debug-container" style="font-family: -apple-system,
           |BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;">
           |  ${res.mkString("\n")}
           |</div>
        """.stripMargin
    }

  def showViewInfo(): Html =
    if (java.lang.Boolean.getBoolean("debug.views") && environment.mode == play.api.Mode.Dev) {
      Html(
        s"""
           |<div style="
           |  padding: 1em;
           |  background-color: #f9f9f9;
           |  border: 1px solid #ddd;
           |  border-radius: 8px;
           |  max-width: 400px;
           |  font-family: Arial,
           |  sans-serif;">
           |
           |${appInfo()}
           |${stackTrace()}
           |</div>
           """.stripMargin
      )
    } else Html("")

  private def viewStackElement(x: StackTraceElement, borderColor: String, componentType: String): String =
    s"""
       |<div class="debug-info ${componentType.toLowerCase}" style="
       |  margin: 10px 0;
       |  padding: 10px;
       |  background: #f8f9fa;
       |  border-radius: 4px;
       |  border-left: 4px solid $borderColor">
       |  <div style="margin: 0 0 10px 0">$componentType</div>
       |  <div style="margin: 0 0 10px 0">${x.getClassName}</div>
       |</div>
    """.stripMargin

}
