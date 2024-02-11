package dto

import models.User
import play.api.libs.json.{Json, Reads}
import scala.language.implicitConversions

case class NewUser(firstName: String, lastName: String, username: String, password: String)

object NewUser {
  implicit val newPostReads: Reads[NewUser] = Json.reads[NewUser]
}
