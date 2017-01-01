package jm.test.Serialize

import jm.lib.iterator
import jm.lib.random
import java.io.InputStream
import java.io.OutputStream
import java.util.*

// -------------------------------------------------------------------------
abstract class RandomSerialData : BaseData() {
  companion object {
    const val SRC_NAMES_COUNT = 2
    const val NAME_NAMES_COUNT = 5
    const val MAX_UNIQUE_PACKAGES = 5

    @JvmField val NONAME = Named()
    @JvmField val VOID = TypedItem()

    fun NamedOrNONAME() =
      if (Math.random() < 0.3) NONAME else
        Named().randomize()

    fun TypedOrVOID() =
      if (Math.random() < 0.3) VOID else
        TypedItem().randomize()

    private val names = "Set,Get,Or,Put,Add,Not,Combine,Has,Is,Not,Empty,Split,Join,Random,For,Each"
      .split(',')

    @JvmStatic fun randomName(count : Int = NAME_NAMES_COUNT) : String {
      var len = 1 + count.random()
      val b = StringBuilder(len * 7)

      while (len > 0) {
        b.append(names[names.size.random()])
        len--
      }
      return b.toString()
    }

    @JvmField val packages = mutableListOf<String>()
    @JvmField val pkgNames = listOf("org", "sun", "app", "add", "List", "sec")

    @JvmStatic fun randomPkg() : String {
      if (packages.size == MAX_UNIQUE_PACKAGES)
        return packages[packages.size.random()]

      val b = StringBuilder(pkgNames.size * 7)
      for (n in 4.random()) {
        if (n > 0) b.append('.')
        b.append(pkgNames[pkgNames.size.random()])
      }

      val s = b.toString()
      packages.add(s)
      return s
    }
  }

  open class Named : BaseData.Named() {
    @JvmField var name = ""
    @JvmField var pkg = ""

    open fun randomize() : Named {
      check(this != NONAME) { "Mutate NONAME!" }
      name = randomName()
      pkg = randomPkg()
      return this
    }

    val FullName : String get() = if (pkg.isNotEmpty()) pkg + "." + name else name
    fun isEmpty() : Boolean = name.isEmpty()
    fun isNotEmpty() : Boolean = name.isNotEmpty()

    override fun toString() : String =
      if (this == NONAME) NM_NONAME else name
  }

  open class TypedItem : Named() {
    @JvmField var type = NONAME

    override fun randomize() : TypedItem {
      check(this != VOID) { "Mutate NONAME!" }
      super.randomize()
      type = NamedOrNONAME()
      if (type.isNotEmpty()) type.name = "t_" + type.name
      return this
    }

    override fun toString() : String = if (this == VOID) NM_VOID else type.FullName + " " + name

    fun toStringAlign() : String =
      if (this == VOID) NM_VOID else
        type.FullName.padEnd(45) + " " + FullName.padEnd(45)
  }

  class ParamsArray : ArrayList<TypedItem>(5)

  open class MethodItem : TypedItem() {
    @JvmField var src = NONAME
    @JvmField var params = ParamsArray()

    override fun randomize() : MethodItem {
      super.randomize()
      src = NamedOrNONAME()
      if (src.isNotEmpty()) src.name = "f" + randomName(SRC_NAMES_COUNT)

      for (n in 3.random() + 2) {
        params.add(TypedOrVOID())
      }
      return this
    }

    override fun toString() : String =
      src.toString().padEnd(20) +
        super.toStringAlign() + " " +
        "( " + params.joinToString() + " )"
  }

}

class TestDataCreator : RandomSerialData() {
  override fun CreateArray(count : Int) : List<MethodItem> {
    val ar = mutableListOf<MethodItem>()
    for (n in count)
      ar.add(MethodItem().randomize())
    return ar
  }

  override fun StoreArray(ar : List<BaseData.Named>, s : OutputStream) {
  }

  override fun CloneData(ar : List<MethodItem>) = ar
  override fun LoadArray(s : InputStream) : List<BaseData.Named> = listOf<BaseData.Named>()
  override fun Description() : String = ""
}