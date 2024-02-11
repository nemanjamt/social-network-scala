package models

import play.api.libs.json.{Format, Json}

case class User(id: Long, firstName: String, lastName: String, username: String, password: String, deleted: Boolean)

object User {
  implicit val userFormat: Format[User] = Json.format[User]
}
