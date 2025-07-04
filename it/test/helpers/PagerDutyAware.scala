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

package helpers

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters.CollectionHasAsScala

trait PagerDutyAware {
  private val listAppender: ListAppender[ILoggingEvent] = createAndStartListAppender("utils.PagerDutyHelper")

  def loggedErrors: List[String] = listAppender.list.asScala.toList.map(_.getMessage)

  private def createAndStartListAppender(loggerName: String): ListAppender[ILoggingEvent] = {
    val logger       = LoggerFactory.getLogger(loggerName).asInstanceOf[Logger]
    val listAppender = new ListAppender[ILoggingEvent]()
    listAppender.start()
    logger.addAppender(listAppender)
    listAppender
  }

}
