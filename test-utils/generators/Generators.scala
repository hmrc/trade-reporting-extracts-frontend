/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package generators

import java.time.{Instant, LocalDate, ZoneOffset}
import org.scalacheck.Arbitrary.*
import org.scalacheck.Gen.*
import org.scalacheck.{Gen, Shrink}

import scala.util.matching.Regex

trait Generators extends ModelGenerators {

  implicit val dontShrink: Shrink[String] = Shrink.shrinkAny

  def genIntersperseString(gen: Gen[String], value: String, frequencyV: Int = 1, frequencyN: Int = 10): Gen[String] = {

    val genValue: Gen[Option[String]] = Gen.frequency(frequencyN -> None, frequencyV -> Gen.const(Some(value)))

    for {
      seq1 <- gen
      seq2 <- Gen.listOfN(seq1.length, genValue)
    } yield seq1.toSeq.zip(seq2).foldLeft("") {
      case (acc, (n, Some(v))) =>
        acc + n + v
      case (acc, (n, _))       =>
        acc + n
    }
  }

  def intsInRangeWithCommas(min: Int, max: Int): Gen[String] = {
    val numberGen = choose[Int](min, max).map(_.toString)
    genIntersperseString(numberGen, ",")
  }

  def intsLargerThanMaxValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x > Int.MaxValue)

  def intsSmallerThanMinValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x < Int.MinValue)

  def nonNumerics: Gen[String] =
    alphaStr suchThat (_.size > 0)

  def decimals: Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_.abs < Int.MaxValue)
      .suchThat(!_.isValidInt)
      .map("%f".format(_))

  def intsBelowValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ < value)

  def intsAboveValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ > value)

  def intsOutsideRange(min: Int, max: Int): Gen[Int] =
    arbitrary[Int] suchThat (x => x < min || x > max)

  def nonBooleans: Gen[String] =
    arbitrary[String]
      .suchThat(_.nonEmpty)
      .suchThat(_ != "true")
      .suchThat(_ != "false")

  def nonEmptyString: Gen[String] =
    arbitrary[String] suchThat (_.nonEmpty)

  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars  <- listOfN(length, arbitrary[Char])
    } yield chars.mkString

  def stringsMatchingRegexWithMaxLength(regexString: String, maxLength: Int): Gen[String] = {
    val regex = new Regex(regexString)

    for {
      length <- Gen.choose(1, maxLength)
      chars  <- Gen.listOfN(length, Gen.alphaNumChar.suchThat(c => regex.pattern.matcher(c.toString).matches))
    } yield chars.mkString
  }

  def nonDefaultStrings(default: String): Gen[String] =
    Gen.alphaStr.suchThat(s => s.nonEmpty && s != default)

  def stringsLongerThan(minLength: Int): Gen[String] = for {
    maxLength <- (minLength * 2).max(100)
    length    <- Gen.chooseNum(minLength + 1, maxLength)
    chars     <- listOfN(length, arbitrary[Char])
  } yield chars.mkString

  def stringsShorterThan(n: Int): Gen[String] =
    Gen.choose(1, n - 1).flatMap(len => Gen.listOfN(len, Gen.alphaNumChar).map(_.mkString))

  def stringsOfExactLength(length: Int): Gen[String] =
    Gen.listOfN(length, Gen.alphaNumChar).map(_.mkString)

  def stringsNotMatchingRegexWithExactLength(regexString: String, exactLength: Int): Gen[String] = {
    val regex = new Regex(regexString)

    Gen
      .listOfN(exactLength, Gen.alphaNumChar)
      .map(_.mkString)
      .suchThat(s => !regex.pattern.matcher(s).matches)
  }

  def stringsExceptSpecificValues(excluded: Seq[String]): Gen[String] =
    nonEmptyString suchThat (!excluded.contains(_))

  def oneOf[T](xs: Seq[Gen[T]]): Gen[T] =
    if (xs.isEmpty) {
      throw new IllegalArgumentException("oneOf called on empty collection")
    } else {
      val vector = xs.toVector
      choose(0, vector.size - 1).flatMap(vector(_))
    }

  def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map { millis =>
      Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }

  def validEoriGen: Gen[String] = for {
    prefix   <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
    digits   <- Gen.listOfN(12, Gen.numChar).map(_.mkString)
    optional <- Gen.option(Gen.listOfN(3, Gen.numChar).map(_.mkString))
  } yield prefix + digits + optional.getOrElse("")

  def invalidEoriStringsOfExactLength(exactLength: Int): Gen[String] = {
    val invalidChars = Gen.oneOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '虛', '聾', '€', '♥')

    Gen.listOfN(exactLength, invalidChars).map(_.mkString)
  }

}
