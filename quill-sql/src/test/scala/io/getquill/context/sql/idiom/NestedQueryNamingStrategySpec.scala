package io.getquill.context.sql.idiom

import io.getquill.base.Spec
import io.getquill.{ EntityQuery, Quoted }
import io.getquill.context.sql.testContextEscapeElements

class NestedQueryNamingStrategySpec extends Spec {

  case class Person(id: Int, name: String)

  case class Address(ownerFk: Int, street: String)

  "with escape naming strategy" - {
    import io.getquill.context.sql.testContextEscape
    import io.getquill.context.sql.testContextEscape._

    "inner aliases should not use naming strategy" in {
      val q = quote {
        query[Person].map {
          p => (p, sql"foobar".as[Int])
        }.filter(_._1.id == 1)
      }
      testContextEscape.run(q).string mustEqual
        """SELECT p._1id AS id, p._1name AS name, p._2 FROM (SELECT p."id" AS _1id, p."name" AS _1name, foobar AS _2 FROM "Person" p) AS p WHERE p._1id = 1"""
    }

    "inner aliases should use naming strategy only when instructed" in {
      import testContextEscapeElements._
      val q = quote {
        query[Person].map {
          p => (p, sql"foobar".as[Int])
        }.filter(_._1.id == 1)
      }
      testContextEscapeElements.run(q).string mustEqual
        """SELECT "p"."_1id" AS "id", "p"."_1name" AS "name", "p"."_2" FROM (SELECT "p"."id" AS "_1id", "p"."name" AS "_1name", foobar AS "_2" FROM "Person" "p") AS "p" WHERE "p"."_1id" = 1"""
    }
  }

  "with upper naming strategy" - {
    import io.getquill.context.sql.testContextUpper
    import io.getquill.context.sql.testContextUpper._

    "inner aliases should use naming strategy" in {
      val q = quote {
        query[Person].map {
          p => (p, sql"foobar".as[Int])
        }.filter(_._1.id == 1)
      }
      testContextUpper.run(q).string mustEqual
        "SELECT p._1id AS id, p._1name AS name, p._2 FROM (SELECT p.ID AS _1id, p.NAME AS _1name, foobar AS _2 FROM PERSON p) AS p WHERE p._1id = 1"
    }

    "inner aliases should use naming strategy with override" in {
      val qs = quote {
        querySchema[Person]("ThePerson", _.id -> "theId", _.name -> "theName")
      }

      val q = quote {
        qs.map {
          p => (p, sql"foobar".as[Int])
        }.filter(_._1.id == 1)
      }
      testContextUpper.run(q).string mustEqual
        "SELECT p._1theId AS theId, p._1theName AS theName, p._2 FROM (SELECT p.theId AS _1theId, p.theName AS _1theName, foobar AS _2 FROM ThePerson p) AS p WHERE p._1theId = 1"
    }

    "inner aliases should use naming strategy with override - two tables" in {
      val qs = quote {
        querySchema[Person]("ThePerson", _.id -> "theId", _.name -> "theName")
      }

      val joined = quote {
        qs.join(query[Person]).on((a, b) => a.name == b.name)
      }

      val q = quote {
        joined.map { (ab) =>
          val (a, b) = ab
          (a, b, sql"foobar".as[Int])
        }.filter(_._1.id == 1)
      }
      testContextUpper.run(q).string mustEqual
        "SELECT ab._1theId AS theId, ab._1theName AS theName, ab._2id AS id, ab._2name AS name, ab._3 FROM (SELECT a.theId AS _1theId, a.theName AS _1theName, b.ID AS _2id, b.NAME AS _2name, foobar AS _3 FROM ThePerson a INNER JOIN PERSON b ON a.theName = b.NAME) AS ab WHERE ab._1theId = 1"
    }

    "inner alias should nest properly in multiple levels" in {
      val q = quote {
        query[Person].map {
          p => (p, sql"foobar".as[Int])
        }.filter(_._1.id == 1).map {
          pp => (pp, sql"barbaz".as[Int])
        }.filter(_._1._1.id == 2)
      }

      testContextUpper.run(q).string mustEqual
        "SELECT p._1_1id AS id, p._1_1name AS name, p._1_2 AS _2, p._2 FROM (SELECT p._1id AS _1_1id, p._1name AS _1_1name, p._2 AS _1_2, barbaz AS _2 FROM (SELECT p.ID AS _1id, p.NAME AS _1name, foobar AS _2 FROM PERSON p) AS p WHERE p._1id = 1) AS p WHERE p._1_1id = 2"
    }

    "inner alias should nest properly in multiple levels - with query schema" in {

      val qs = quote {
        querySchema[Person]("ThePerson", _.id -> "theId", _.name -> "theName")
      }

      val q = quote {
        qs.map {
          p => (p, sql"foobar".as[Int])
        }.filter(_._1.id == 1).map {
          pp => (pp, sql"barbaz".as[Int])
        }.filter(_._1._1.id == 2)
      }

      testContextUpper.run(q).string mustEqual
        "SELECT p._1_1theId AS theId, p._1_1theName AS theName, p._1_2 AS _2, p._2 FROM (SELECT p._1theId AS _1_1theId, p._1theName AS _1_1theName, p._2 AS _1_2, barbaz AS _2 FROM (SELECT p.theId AS _1theId, p.theName AS _1theName, foobar AS _2 FROM ThePerson p) AS p WHERE p._1theId = 1) AS p WHERE p._1_1theId = 2"
    }
  }
}
