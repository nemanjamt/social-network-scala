package dto

import play.api.libs.json.{Json, Reads}

case class Credentials(username: String, password: String)

object Credentials {
  implicit val newPostReads: Reads[Credentials] = Json.reads[Credentials]
}
