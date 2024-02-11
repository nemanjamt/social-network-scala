package dto

import play.api.libs.json.{Json, Reads, Writes}

case class UserInfo(id: Long, firstName: String, lastName: String, username: String)

object UserInfo {
  implicit val userInfoWrites: Writes[UserInfo] = Json.writes[UserInfo]
}
