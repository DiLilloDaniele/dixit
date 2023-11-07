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
