package com.arcusys.learn.scorm.tracking.storage.impl.orbroker

import org.junit._
import Assert._
import com.arcusys.learn.scorm.tracking.model._
import com.arcusys.learn.storage.impl.orbroker.ParameterizedUnitTests
import runner.RunWith
import runners.Parameterized

@RunWith(value = classOf[Parameterized])
class UserStorageTest(dbFileName: String) extends ParameterizedUnitTests(dbFileName){
  val userStorage = new UserStorageImpl

  @Before
  def setUp() {
    userStorage.renew()
  }

  @Test
  def noDataInitially() {
    // should be one guest
    assertEquals(1, userStorage.getAll.size)
  }

  @Test
  def canCreate() {
    userStorage.createAndGetID(User(0, "user1"))
    userStorage.createAndGetID(User(1, "user2"))
    // should be one guest
    assertEquals(3, userStorage.getAll.size)
  }

  @Test
  def canGetByID() {
    val user = User(0, "user1")
    val testQuizId = userStorage.createAndGetID(user)
    assertEquals(user.id, testQuizId)
    val fetchedQuiz = userStorage.getByID(testQuizId).get
    assertEquals(user, fetchedQuiz)
    assertEquals("user1", fetchedQuiz.name)
  }

  @Test
  def canUpdate() {
    val user = User(0, "user1")
    val testQuizId = userStorage.createAndGetID(user)
    assertEquals(user.id, testQuizId)
    userStorage.modify(user.copy(name="user test"))
    val fetchedQuiz = userStorage.getByID(testQuizId).get
    assertEquals("user test", fetchedQuiz.name)
  }

  @Test
  def canDelete() {
    // should be one guest
    val user1 = User(0, "user1")
    val user2 = User(1, "user2")
    val user3 = User(2, "user3")
    userStorage.createAndGetID(user1)
    userStorage.createAndGetID(user2)
    val testQuizId = userStorage.createAndGetID(user3)

    assertEquals(4, userStorage.getAll.size)

    userStorage.delete(testQuizId)

    assertEquals(3, userStorage.getAll.size)
  }
}