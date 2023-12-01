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
