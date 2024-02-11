package dto

import models.User
import play.api.libs.json.{Json, Reads}

case class UpdateUser(firstName: String, lastName: String, password: String)

object UpdateUser {
  implicit val updateUserReads: Reads[UpdateUser] = Json.reads[UpdateUser]
}
