package practice

object practice extends App {

  def whileLoopByName(condition: => Boolean)(body: => Long): Unit = {
    if(condition){
      println(body)
      whileLoopByName(condition)(body)
    }
  }

  def whileLoopByValue(condition: => Boolean)(body: () => Long): Unit = {
    if(condition){
      println(body())
      whileLoopByValue(condition)(body)
    }
  }

  //we want this condition to be something that gens a random number, and returns false
  def cond: Boolean = {
    val result = new java.util.Random()
    val nxt = result.nextInt(11)
    nxt match {
      case 1 => false
      case _ => true
    }
  }

  def getTime() = {
    println("getting time")
    System.nanoTime()
  }

  //val t = getTime()
  //whileLoopByValue(cond)(getTime)
  //whileLoopByName(cond)(getTime())

  //call by value functions are useful to ensure the "body" of your code gets executed EVERY time you call. so,
  //in our while loop we get this behavior



  /*demo that shows when a by-name parameter is evaluated, vs a by-value */
  def incByName(x: => Int) = {
    println("begin inc")
    val nxt = x + 1
    println(nxt)
  }
  //on this call, the body supplied here isn't evaluated (and thus "started body" isn't printed)
  //until the "begin inc" already started
  incByName{ println("started body"); 1 + 1}

  def incByValue(x: () => Int): Unit = {
    println("begin inc")
    println(x() + 1)
  }
  println("\n\nend by name demo \n\n")

  //incByValue { println("started body"); 1 + 1 }
}
