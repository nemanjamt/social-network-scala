package repositories

import dto.NewUser
import models.User
import org.mindrot.jbcrypt.BCrypt
import play.api.Logger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.sql.SqlProfile.ColumnOption.NotNull

import java.sql.{SQLException, SQLIntegrityConstraintViolationException}
import scala.util.{Failure, Success}

class UserRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  private val users = TableQuery[UserTable]
  val logger: Logger = Logger(this.getClass)
  def hashPassword(password: String): String = {
    val saltRounds = 10
    BCrypt.hashpw(password, BCrypt.gensalt(saltRounds))
  }
  def checkPassword(passwordToCheck: String, hashed: String): Boolean = {
    logger.info("lozinka " + hashed)
    BCrypt.checkpw(passwordToCheck, hashed)
  }

  def createUser(user: User): Future[User] = {
    val userToCreate = user.copy(password = hashPassword(user.password))
    db.run(
      (users returning users.map(_.id)
        into ((user, id) => user.copy(id = id))) += userToCreate
    )
  }

  def findUser(userId: Long): Future[Option[User]] = {
    db.run(
      users.filter(_.id === userId).filter(!_.deleted).result
    ).map(_.headOption)
  }

  def checkIfExistsWithUsername(username: String): Future[Boolean] = {
    db.run(users.filter(u => u.username === username).exists.result)
  }

  def checkIfExistsWithUsernameAndPassword(username: String, password: String): Future[Boolean] = {
    val matchedUser = db.run(
      users.filter(user => user.username === username).result.map(_.headOption)
    )
    matchedUser.flatMap {
      case Some(user) =>
        Future.successful(checkPassword(password, user.password))
      case None => Future.successful(false)
    }
  }
  def checkIfExistsById(id: Long): Future[Boolean] = {
    db.run(users.filter(u => u.id === id && !u.deleted).exists.result)
  }

  def updateUser(user: User): Future[Option[User]] = {
    db.run(users.filter(_.id === user.id).update(user).map {
      case 0       => None
      case 1       => Some(user)
      case updated => throw new RuntimeException(s"Updated $updated rows")
    })
  }

  def findUserByUsername(username: String): Future[Option[User]] = {
    db.run(
      users.filter(user => user.username === username).result.map(_.headOption)
    )
  }

  def deleteUser(user: User): Future[Option[Int]] = {
    db.run(users.filter(_.id === user.id).update(user).map {
      case 0       => None
      case 1       => Some(1)
      case deleted => throw new RuntimeException(s"Deleted $deleted rows")
    })
  }
  class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("firstName", NotNull)
    def lastName = column[String]("lastName", NotNull)
    def username = column[String]("username", NotNull)
    def password = column[String]("password", NotNull)
    def deleted = column[Boolean]("deleted", NotNull)

    override def * : ProvenShape[User] = (id, firstName, lastName, username, password, deleted) <> ((User.apply _).tupled, User.unapply)
  }
}
