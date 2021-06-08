package ru.johnspade

import java.time.Instant
import scala.collection.immutable.SortedMap

package object blackbox {
  type Stats = Map[String, SortedMap[Instant, Map[String, Int]]]
  object Stats {
    val Empty: Stats = Map.empty
  }
}
