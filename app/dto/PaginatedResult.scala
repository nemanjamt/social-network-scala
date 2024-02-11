package dto

import play.api.libs.json.{Json, Writes}

case class PaginatedResult[T](
    totalCount: Int,
    entities: Seq[T],
    currentPage: Int,
    totalPage: Int
)

object PaginatedResult {
  implicit val friendshipDetailedWrites: Writes[PaginatedResult[PostDetailed]] = Json.writes[PaginatedResult[PostDetailed]]
}
