package jm.test.Serialize

import jm.lib.*
import jm.lib.console.abort
import jm.lib.console.note
import jm.lib.console.outn
import jm.lib.console.progress
import java.io.*

class LinkedData private constructor(@JvmField val value : Int) {
  companion object {
    @JvmField val ZERO = LinkedData(0)
    @JvmField val NONZERO = LinkedData(1)
    @JvmStatic fun make(v : Int) = if (v == 0) ZERO else NONZERO
  }
}

open class DataClass : Externalizable {
  @JvmField var link : LinkedData
  @JvmField var intField : Int

  constructor() { link = LinkedData.ZERO; intField = 0 }
  constructor(v : Int) { link = LinkedData.make(v); intField = v }

  fun print() = outn("int = [%d]\nlink = [%s]", intField,
                     if (link == LinkedData.ZERO) "ZERO" else
                       if (link == LinkedData.NONZERO) "NONZERO" else
                         "OTHER!")

  override fun readExternal(s : ObjectInput) {
    link = if ( s.readByte().toInt() == 0 ) LinkedData.ZERO else LinkedData.NONZERO
    intField = s.readInt()
  }

  override fun writeExternal(s : ObjectOutput) {
    s.writeByte(if(link == LinkedData.ZERO) 0 else 1)
    s.writeInt(intField)
  }
}

fun Action() {
  val a = DataClass(100)
  outn("Saved contents:")
  a.print()

  Holder(ObjectOutputStream(File("out.bin").outputStream())).h.writeObject(a)
  val b = Holder(ObjectInputStream(File("out.bin").inputStream())).h.readObject()

  if (b is DataClass) {
    outn("Loaded contents:")
    b.print()
  }
}

// -------------------------------------------------------------------------
abstract class BaseData {
  companion object {
    const val NM_NONAME = "<noname>"
    const val NM_VOID = "void"
  }

  abstract class Named

  open fun CreateArray(count : Int) : List<Named> = listOf<Named>()
  abstract fun CloneData(ar : List<RandomSerialData.MethodItem>) : List<Named>
  abstract fun StoreArray(ar : List<Named>, s : OutputStream)
  abstract fun LoadArray(s : InputStream) : List<Named>
  abstract fun Description() : String
}

// -------------------------------------------------------------------------
class Test {
  class Opts {
    companion object {
      @JvmField var noOut = false
      @JvmField var doGC = false
      @JvmField var baseFile = ""
      @JvmField var nCount = 0
      @JvmField var nRetry = 0L
    }
  }

  // -------------------------------------------------------------------------
  fun OutUsage() {
    val APP = "SerializableTest.jar"
    note("""
Test for store classes tree structure
USAGE: $APP [-opts]

Where OPTS are:
  -Count=<number>        - set number of items to generate
  -Retry=<number>        - set number of iterations for each test
  -Out=<file>            - set file name to output
  -Nout                  - disable items output
  -gc                    - run gc after every test
""")
      .abort("Please specify parameters")
  }

  fun getOutput(a : Args) {
    val fnm = a.Arg("O;OUT", "test_out")
    if (a.Check("O;OUT") || fnm == "-") abort("Invalid output name: %s", fnm)

    val f = File(fnm)
    if (f.exists() && (f.isDirectory || !f.canWrite()))
      abort("Invalid output type %s", fnm)

    Opts.baseFile = f.canonicalFile.absoluteFile.nameWithoutExtension
  }

  // -------------------------------------------------------------------------
  fun PItems(pref : String, ar : List<BaseData.Named>) {
    if (Opts.noOut) return
    note("$pref ITEMS: ${ar.size}\n")
    ar.forEachIndexed { n, it ->
      note("%3d) %s\n", n, it.toString())
    }
  }

  // -------------------------------------------------------------------------
  fun Compare(ar : List<BaseData.Named>, ar1 : List<BaseData.Named>) {
    check(ar.size == ar1.size) { "Diff array size!" }
    ar.forEachIndexed { n, it ->
      val s1 = it.toString()
      val s2 = ar1[n].toString()
      if (s1 != s2)
        abort("Check failed!\nItem1: %s\nItem2: %s\n", s1, s2)
    }
  }

  // -------------------------------------------------------------------------
  class TestRes {
    @JvmField var save = 0L
    @JvmField var load = 0L
    @JvmField var total = 0L

    operator fun plusAssign(v : TestRes) {
      save += v.save
      load += v.load
      total += v.total
    }

    operator fun divAssign(v : Long) {
      save /= v
      load /= v
      total /= v
    }
  }

  // -------------------------------------------------------------------------
  class TestInfo(@JvmField val name : String,
                 @JvmField val test : BaseData,
                 ar : List<RandomSerialData.MethodItem>) {
    @JvmField val items = test.CloneData(ar)
    @JvmField val rc = TestRes()
  }

  fun DoTest(test : TestInfo) : TestRes {
    val per = Period(0)
    val total = Period(0)
    val rc = TestRes()
    val fnm = Opts.baseFile + "." + test.name + ".bin"

    per.Reset()
    progress("Store items...")
    Holder(FileOutputStream(fnm).buffered()).use {
      test.test.StoreArray(test.items, it.h)
    }
    rc.save = per.Period()
    progress("Stored %d in %s sec, file %s \r",
             test.items.size,
             rc.save.millisTimeStr,
             File(fnm).length().formatCps())

    per.Reset()
    progress("Read items...")
    val loaded = Holder(FileInputStream(fnm).buffered()).use {
      test.test.LoadArray(it.h)
    }
    rc.load = per.Period()
    progress("Loaded %d in %s sec", loaded.size, rc.load.millisTimeStr)
    rc.total = total.Period()

    PItems("LOADED", loaded)
    Compare(test.items, loaded)

    return rc
  }

  fun Diff(rc : Long, v : Long) : String =
    if (v > rc) "%3.2f".format(v.toDouble() / rc - 1) else
      if (rc > v) "%3.2f".format(rc.toDouble() / v - 1) else
        "=="

  // -------------------------------------------------------------------------
  fun main(args : Array<String>) {
    val a = Args(args)
    ConsoleProgress
      .preRegister()
      .ParseArgs(a)

    //Action(); abort()

    if (a.Check("?")) OutUsage()
    Opts.nCount = a.Arg("C;COUNT", "5").toInt().max(1)
    Opts.nRetry = a.Arg("R;RETRY", "5").toLong().max(1)
    Opts.noOut = a.CheckDel("N;NOUT")
    Opts.doGC = a.CheckDel("GC")
    getOutput(a)

    note("Output file       : ${Opts.baseFile}\n")
    note("Number of elements: ${Opts.nCount}\n")
    note("Number of retries : ${Opts.nRetry}\n")

    //Test data
    val testItems = TestDataCreator().CreateArray(Opts.nCount)
    PItems("CREATED", testItems)

    //Tests
    val tests = listOf(
      TestInfo("SerialFull", TestSerializableFull(), testItems)
      , TestInfo("ExternFull", TestExternalizableFull(), testItems)
      , TestInfo("Extern+Ser", TestExternalizable(), testItems)
      , TestInfo("XMLw3c", TestXML(), testItems)
      , TestInfo("JsJsonMini", TestJSONMini(), testItems)
      , TestInfo("JsJackAnn", TestJSONJackson(), testItems)
      , TestInfo("JsJackSream", TestJacksonStream(), testItems)
    )

    //Execute
    val per = Period(0)
    for (i in Opts.nRetry) {
      for (n in tests.size) {
        tests[n].rc += DoTest(tests[n])
        if (Opts.doGC) Runtime.getRuntime().gc()
      }
    }

    val tot = tests.map { it.rc.total }.sum()
    val perTm = per.Period()
    note("Tests complete in %s sec :: ", perTm.millisTimeStr)
    note("Save %s, ", tests.map { it.rc.save }.sum().millisTimeStr)
    note("Load %s, ", tests.map { it.rc.load }.sum().millisTimeStr)
    note("Total %s, ", tot.millisTimeStr)
    note("Waste %s\n", (perTm - tot).millisTimeStr)

    //Show results
    val rcTotal = // range by time spend best..worst
      tests
        .map { it.rc.total }
        .sorted()

    note("-".repeat(130) + "\n" +
           "%-4s %15s | %15s | %6s | %6s | %15s | %6s | %6s | %15s | %6s | %6s\n" +
           "-".repeat(130) + "\n",
         "N", "Name",
         "Save", "Best", "Worst",
         "Load", "Best", "Worst",
         "Total", "Best", "Worst")
    tests.forEach {
      note("%-4d %-15s | ", rcTotal.indexOf(it.rc.total) + 1, it.name)

      var times = tests.map { it.rc.save }
      note("%15s | %6s | %6s | ",
           it.rc.save.millisTimeStr,
           Diff(times.min()!!, it.rc.save),
           Diff(times.max()!!, it.rc.save))

      times = tests.map { it.rc.load }
      note("%15s | %6s | %6s | ",
           it.rc.load.millisTimeStr,
           Diff(times.min()!!, it.rc.load),
           Diff(times.max()!!, it.rc.load))

      times = tests.map { it.rc.total }
      note("%15s | %6s | %6s | ",
           it.rc.total.millisTimeStr,
           Diff(times.min()!!, it.rc.total),
           Diff(times.max()!!, it.rc.total))
      note("\n")
    }
    note("-".repeat(130) + "\n")
  }
}

fun main(args : Array<String>) = Test().main(args)

