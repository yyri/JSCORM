package com.arcusys.learn.questionbank.service

import com.arcusys.learn.questionbank.model._
import com.arcusys.scorm.util._
import com.arcusys.scala.json.Json._
import org.scala_tools.subcut.inject.BindingModule
import com.arcusys.learn.web.ServletBase
import com.arcusys.learn.ioc.Configuration
import com.liferay.portal.kernel.util.WebKeys
import com.liferay.portal.theme.ThemeDisplay

class QuestionService(configuration: BindingModule) extends ServletBase(configuration) {
  def this() = this(Configuration)
  import storageFactory._
  get("/id/:id") {
    val id = parameter("id").intRequired
    json(QuestionSerializer.buildItemMap(questionStorage.getByID(id).get))
  }

  get("/children/:id") {
  val categoryID = parameter("id").intOption(-1)
  val courseID = parameter("courseID").intOption(-1)
  json(QuestionSerializer.buildOutputJSON(questionStorage.getByCategory(categoryID, courseID)))
}

  post("/") {
    val questionType = parameter("questionType").intRequired
    val categoryID = parameter("categoryID").intOption(-1)
    val title = parameter("title").required
    val text = parameter("text").withDefault("")
    val explanationText = parameter("explanationText").withDefault("")
    val forceCorrectCount = parameter("forceCorrectCount").booleanRequired
    val isCaseSensitive = parameter("isCaseSensitive").booleanRequired
    val courseID = parameter("courseID").intOption(-1)
    val entity = questionType match {
      case 0 => new ChoiceQuestion(0, categoryID, title, text, explanationText, Nil, forceCorrectCount,courseID)
      case 1 => new TextQuestion(0, categoryID, title, text, explanationText, Nil, isCaseSensitive,courseID)
      case 2 => new NumericQuestion(0, categoryID, title, text, explanationText, Nil,courseID)
      case 3 => new PositioningQuestion(0, categoryID, title, text, explanationText, Nil, forceCorrectCount,courseID)
      case 4 => new MatchingQuestion(0, categoryID, title, text, explanationText, Nil,courseID)
      case 5 => new EssayQuestion(0, categoryID, title, text, explanationText,courseID)
      case 6 => new EmbeddedAnswerQuestion(0, categoryID, title, text, explanationText,courseID)
      case 7 => new CategorizationQuestion(0, categoryID, title, text, explanationText, Nil,courseID)
      case _ => halt(405, "Service: Oops! Can't create question")
    }
    json(QuestionSerializer.buildItemMap(questionStorage.getByID(questionStorage.createAndGetID(entity)).get))
  }

  post("/update/:id") {
    val id = parameter("id").intRequired
    val categoryId = questionStorage.getByID(id).get.categoryID
    val questionType = parameter("questionType").intRequired
    val title = parameter("title").required
    val text = parameter("text").required
    val explanationText = parameter("explanationText").required
    val forceCorrectCount = parameter("forceCorrectCount").booleanRequired
    val isCaseSensitive = parameter("isCaseSensitive").booleanRequired
    val answersMap = toObject(parameter("answers").withDefault("[]")).asInstanceOf[List[Map[String, Any]]]
    val entity = questionType match {
      case 0 => new ChoiceQuestion(id, categoryId, title, text, explanationText, answersMap.map(parseChoiceAnswer(_)), forceCorrectCount, None)
      case 1 => new TextQuestion(id, categoryId, title, text, explanationText, answersMap.map(parseTextAnswer(_)), isCaseSensitive, None)
      case 2 => new NumericQuestion(id, categoryId, title, text, explanationText, answersMap.map(parseNumericAnswer(_)), None)
      case 3 => new PositioningQuestion(id, categoryId, title, text, explanationText, answersMap.map(parsePositioningAnswer(_)), forceCorrectCount, None)
      case 4 => new MatchingQuestion(id, categoryId, title, text, explanationText, answersMap.map(parseMatchingAnswer(_)), None)
      case 5 => new EssayQuestion(id, categoryId, title, text, explanationText, None)
      case 6 => new EmbeddedAnswerQuestion(id, categoryId, title, text, explanationText, None)
      case 7 => new CategorizationQuestion(id, categoryId, title, text, explanationText, answersMap.map(parseCategorizationAnswer(_)), None)
      case _ => halt(405, "Service: Oops! Can't update question")
    }
    questionStorage.modify(entity)
    json(QuestionSerializer.buildItemMap(entity))
  }

  post("/move/:id") {
    val id = parameter("id").intRequired
    val dndMode = parameter("dndMode").required
    val targetID = parameter("targetId").intOption(-1)
    val itemType = parameter("itemType").required

    val moveAfterTarget = dndMode == "after"

    val siblingID = if (dndMode == "last" || (dndMode == "after" && itemType == "folder")) None
    else targetID

    val parentID = if (targetID != None && dndMode == "last") targetID
    else if (targetID != None && (dndMode == "after" && itemType == "folder")) questionCategoryStorage.getByID(targetID.get).getOrElse(halt(404, "Can't find category")).parentID
    else if (siblingID != None) questionStorage.getByID(targetID.get).getOrElse(halt(404, "Can't find category")).categoryID
    else None

    questionStorage.move(id, parentID, siblingID, moveAfterTarget)
    json(QuestionSerializer.buildItemMap(questionStorage.getByID(id).get))

  }
  post("/delete/:id") {
    val id = parameter("id").intRequired
    questionStorage.delete(id)
  }

  private def parseChoiceAnswer(data: Map[String, Any]) = new ChoiceAnswer(0, data.getOrElse("answerText", "").toString, data.getOrElse("isCorrect", "false").toString.toBoolean)

  private def parseTextAnswer(data: Map[String, Any]) = new TextAnswer(0, data.getOrElse("answerText", "").toString)

  private def parseNumericAnswer(data: Map[String, Any]) = new NumericAnswer(0, BigDecimal(data.getOrElse("rangeFrom", 0).toString), BigDecimal(data.getOrElse("rangeTo", 0).toString))

  private def parsePositioningAnswer(data: Map[String, Any]) = new PositioningAnswer(0, data.getOrElse("answerText", "").toString, data.getOrElse("isCorrect", "false").toString.toBoolean)

  private def parseMatchingAnswer(data: Map[String, Any]) = new MatchingAnswer(0, data.getOrElse("answerText", "").toString, data.get("matchingText").asInstanceOf[Option[String]])

  private def parseCategorizationAnswer(data: Map[String, Any]) = new CategorizationAnswer(0, data.getOrElse("answerText", "").toString, data.get("matchingText").asInstanceOf[Option[String]])
}
