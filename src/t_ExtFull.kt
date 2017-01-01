package jm.test.Serialize

import jm.lib.console.progress
import jm.lib.iterator
import java.io.*
import java.util.*

// -------------------------------------------------------------------------
class TestExternalizableFull : TestExternalizableFull_data() {
  override fun Description() = "Full manual Java Externalizable serialization"

  override fun CloneData(ar : List<RandomSerialData.MethodItem>) : List<BaseData.Named> {
    val dest = mutableListOf<MethodItem>()
    ar.forEach {
      dest.add( MethodItem(it) )
    }
    return dest
  }

  override fun StoreArray(ar : List<BaseData.Named>, s : OutputStream) {
    val cn = ar.size
    val os = ObjectOutputStream(s)
    os.writeInt(cn)
    ar.forEachIndexed { n, it ->
      progress("Storing %3d%% %d/%d...", n * 100 / cn, n, cn)
      os.writeObject(it as MethodItem)
    }
  }

  override fun LoadArray(s : InputStream) : List<BaseData.Named> {
    val ar = mutableListOf<MethodItem>()
    val os = ObjectInputStream(s)
    val cn = os.readInt()
    for (n in cn) {
      progress("Loading %3d%% %d/%d...", n * 100 / cn, n, cn)
      ar.add(os.readObject() as MethodItem)
    }
    return ar
  }
}

// -------------------------------------------------------------------------
abstract class TestExternalizableFull_data : BaseData() {
  companion object {
    @JvmField val NONAME = Named()
    @JvmField val VOID = TypedItem()
  }

  open class Named : BaseData.Named, Externalizable {
    @JvmField var name = ""
    @JvmField var pkg = ""

    companion object {
      @JvmField val SINGLE = 0
      @JvmField val EMPTY = 1
      @JvmField val CONTENTS = 1
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

    override fun writeExternal(s : ObjectOutput) {
      if (this == NONAME) s.writeByte(SINGLE) else
        if (isEmpty()) s.writeByte(EMPTY) else {
          s.writeByte(CONTENTS)
          s.writeUTF(name)
          s.writeUTF(pkg)
        }
    }

    private var oType = SINGLE
    open fun readResolve() : Named = if (oType == SINGLE) NONAME else this

    override fun readExternal(s : ObjectInput) {
      oType = s.readByte().toInt()
      if (oType == CONTENTS) {
        name = s.readUTF()
        pkg = s.readUTF()
      }
    }
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

    private var oType = SINGLE
    override fun readResolve() : TypedItem = if (oType == SINGLE) VOID else this

    override fun writeExternal(s : ObjectOutput) {
      if (this == VOID) s.writeByte(SINGLE) else
        if (isEmpty()) s.writeByte(EMPTY) else {
          s.writeByte(CONTENTS)
          super.writeExternal(s)
          type.writeExternal(s)
        }
    }

    override fun readExternal(s : ObjectInput) {
      oType = s.readByte().toInt()
      if (oType != CONTENTS) return

      super.readExternal(s)

      val v = Named()
      v.readExternal(s)
      type = v.readResolve()
    }
  }

  class ParamsArray : ArrayList<TypedItem>(5), Externalizable {
    companion object {
      const private val serialVersionUID = 1L
    }

    override fun readExternal(s : ObjectInput) {
      val cn = s.readInt()
      for (n in cn) {
        val v = TypedItem()
        v.readExternal(s)
        add(v.readResolve())
      }
    }

    override fun writeExternal(s : ObjectOutput) {
      s.writeInt(size)
      for (it in this)
        it.writeExternal(s)
    }
  }

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

    override fun writeExternal(s : ObjectOutput) {
      super.writeExternal(s)
      src.writeExternal(s)
      params.writeExternal(s)
    }

    override fun readExternal(s : ObjectInput) {
      super.readExternal(s)

      val v = Named()
      v.readExternal(s)
      src = v.readResolve()

      params.readExternal(s)
    }
  }
}

