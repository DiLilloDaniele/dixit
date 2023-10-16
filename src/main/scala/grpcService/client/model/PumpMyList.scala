package grpcService.client.model

object PumpMyList:

  extension[A] (l: List[A])
    def filterMap(cond: A => Boolean)(map: A => A): List[A] =
      l.map { i =>
        if(cond(i)) {
          map(i)
        } else {
          i
        }
      }

    def equals(l2: List[A]): Boolean =
      l.toSet == l2.toSet

    def -(el: A): List[A] =
      l.filter { i => i != el}

    def removeIntersection(l2: List[A]): List[A] =
      l.filter { i => !l2.contains(i)}

  @main def test() =
    var l = List(1,2,3,4,5,6)
    var newL = l.filterMap {
      i => i % 2 == 0
    } {
      i => i * 2
    }
    newL = newL :+ 7
    newL = newL - 8
    val l1 = List(1,2,3,4)
    val l2= List(3,4,5,6)
    println(l1.removeIntersection(l2))
    println(newL)
