package jm.test.Serialize

import jm.lib.console.progress
import jm.lib.iterator
import java.io.*
import java.util.*

// -------------------------------------------------------------------------
class TestExternalizable : TestExternalizable_Data() {
  override fun Description() = "Patially manual Java Externalizable serialization"


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
abstract class TestExternalizable_Data : BaseData() {
  companion object {
    @JvmField val NONAME = Named()
    @JvmField val VOID = TypedItem()
  }

  open class Named : BaseData.Named, Externalizable, Serializable {
    @JvmField var name = ""
    @JvmField var pkg = ""

    companion object {
      @JvmField val SINGLE = 0
      @JvmField val EMPTY = 1
      @JvmField val CONTENTS = 2
      const private val serialVersionUID = 1L
    }

    constructor()
    constructor(p : RandomSerialData.Named) : super() {
      name = p.name
      pkg = p.pkg
    }

    val isNONAME : Boolean get() = this == NONAME
    val FullName : String get() = if (pkg.isNotEmpty()) pkg + "." + name else name
    fun isEmpty() : Boolean = name.isEmpty()
    fun isNotEmpty() : Boolean = name.isNotEmpty()
    override fun toString() : String =
      if (this == NONAME) NM_NONAME else name

    private var oType = SINGLE
    private fun readResolve() : Any =
      if (oType == SINGLE) NONAME else this

    override fun writeExternal(s : ObjectOutput) {
      oType =
        if (this == NONAME) SINGLE else
          if (isEmpty()) EMPTY else
            CONTENTS
      s.writeByte(oType)

      if (oType == CONTENTS) {
        s.writeUTF(name)
        s.writeUTF(pkg)
      }
    }

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
    constructor(p : RandomSerialData.TypedItem) : super(p) {
      type = if (p.type.isEmpty()) NONAME else Named(p.type)
    }

    override fun toString() : String =
      if (this == VOID) NM_VOID else type.FullName + " " + name

    fun toStringAlign() : String =
      if (this == VOID) NM_VOID else
        type.FullName.padEnd(45) + " " + FullName.padEnd(45)

    private var oType = SINGLE
    private fun readResolve() : Any =
      if (oType == SINGLE) VOID else this

    override fun writeExternal(s : ObjectOutput) {
      super.writeExternal(s)
      if (this == VOID) s.writeByte(SINGLE) else
        if (isEmpty()) s.writeByte(EMPTY) else {
          s.writeByte(CONTENTS)
          s.writeObject(type)
        }
    }

    override fun readExternal(s : ObjectInput) {
      super.readExternal(s)
      oType = s.readByte().toInt()
      if (oType == CONTENTS) type = s.readObject() as Named
    }
  }

  class ParamsArray : ArrayList<TypedItem>(5)

  class MethodItem : TypedItem {
    @JvmField var src = NONAME
    @JvmField var params = ParamsArray()

    companion object {
      const private val serialVersionUID = 1L
    }

    constructor()
    constructor(p : RandomSerialData.MethodItem) : super(p) {
      src = if (p.src.isEmpty()) NONAME else Named(p.src)
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
      s.writeObject(src)
      s.writeObject(params)
    }

    override fun readExternal(s : ObjectInput) {
      super.readExternal(s)
      src = s.readObject() as Named
      params = s.readObject() as ParamsArray
    }
  }
}
