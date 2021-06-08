package ru.johnspade.blackbox

import io.circe.Decoder
import io.circe.magnolia.configured.Configuration
import io.circe.magnolia.configured.decoder.semiauto.deriveConfiguredMagnoliaDecoder

import java.time.Instant

case class Event(eventType: String, data: String, timestamp: Instant)

object Event {
  private implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  private implicit val instantDecoder: Decoder[Instant] = Decoder[Long].map(Instant.ofEpochSecond)
  implicit val decoder: Decoder[Event] = deriveConfiguredMagnoliaDecoder[Event]
}
