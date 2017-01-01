package jm.test.Serialize

import jm.lib.iterator
import jm.lib.random
import java.io.*
import java.util.*

// -------------------------------------------------------------------------
open class TestSerializableFull : BaseData() {
  override fun Description() = "Auto Java Serialisable serialization"

  companion object {
    @JvmField val NONAME = Named()
    @JvmField val VOID = TypedItem()
  }

  open class Named : BaseData.Named, Serializable {
    @JvmField var name = ""
    @JvmField var pkg = ""

    companion object {
      const private val serialVersionUID = 1L
    }

    constructor()
    constructor(p:RandomSerialData.Named) : super() {
      name = p.name
      pkg = p.pkg
    }

    val FullName : String get() = if (pkg.isNotEmpty()) pkg + "." + name else name
    fun isEmpty() : Boolean = name.isEmpty()
    fun isNotEmpty() : Boolean = name.isNotEmpty()

    override fun toString() : String =
      if (this == NONAME) NM_NONAME else name

    private fun readResolve() : Any =
      if (isEmpty()) NONAME else this
  }

  open class TypedItem : Named {
    @JvmField var type = NONAME

    companion object {
      const private val serialVersionUID = 1L
    }

    constructor()
    constructor(p:RandomSerialData.TypedItem) :super(p) {
      type = if ( p.type.isEmpty()) NONAME else Named(p.type)
    }

    override fun toString() : String =
      if (this == VOID) NM_VOID else type.FullName + " " + name

    fun toStringAlign() : String =
      if (this == VOID) NM_VOID else
        type.FullName.padEnd(45) + " " + FullName.padEnd(45)

    private fun readResolve() : Any =
      if (isEmpty()) VOID else this
  }

  class ParamsArray : ArrayList<TypedItem>(5)

  class MethodItem : TypedItem {
    @JvmField var src = NONAME
    @JvmField var params = ParamsArray()

    companion object {
      const private val serialVersionUID = 1L
    }

    constructor()
    constructor(p:RandomSerialData.MethodItem) :super(p) {
      src = if(p.src.isEmpty()) NONAME else Named(p.src)
      p.params.forEach {
        params.add( if (it.isEmpty()) VOID else TypedItem(it) )
      }
    }

    override fun toString() : String =
      src.toString().padEnd(20) +
        super.toStringAlign() + " " +
        "( " + params.joinToString() + " )"
  }

  override fun StoreArray(ar : List<BaseData.Named>, s : OutputStream) {
    ObjectOutputStream(s).writeObject(ar)
  }

  override fun LoadArray(s : InputStream) : List<Named> {
    @Suppress("UNCHECKED_CAST")
    return ObjectInputStream(s).readObject() as List<Named>
  }

  override fun CloneData(ar : List<RandomSerialData.MethodItem>) : List<BaseData.Named> {
    val dest = mutableListOf<MethodItem>()
    ar.forEach {
      dest.add( MethodItem(it) )
    }
    return dest
  }
}
