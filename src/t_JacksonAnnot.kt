package jm.test.Serialize

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import jm.lib.iterator
import jm.lib.random
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable

/* Auto serialization by JSON
   Lib: fasterXML-jackson
   Link: https://github.com/FasterXML/jackson
   Lib: jackson-annotations-2.0.4.jar, jackson-core-2.0.4.jar, jackson-databind-2.0.4.jar
 */

// -------------------------------------------------------------------------
open class TestJSONJackson : BaseData() {
  override fun Description() = "Auto (databind+annotations) serialization to JSON by fasterXML-jackson"

  companion object {
    @JvmField val NONAME = Named()
    @JvmField val VOID = TypedItem()
  }

  open class Named : BaseData.Named, Serializable {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JvmField var name = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JvmField var pkg = ""

    companion object {
      @JsonCreator
      @JvmStatic private fun new(@JsonProperty("name") nm : String?,
                                 @JsonProperty("pkg") pkg : String?) =
        if (nm == null || nm.isEmpty()) NONAME else {
          val v = Named()
          v.name = nm
          v.pkg = pkg ?: ""
          v
        }
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
  }

  open class TypedItem : Named {
    @JsonInclude( JsonInclude.Include.NON_NULL )
    @JvmField var type = NONAME
    @JsonGetter(value = "type")
    private fun getNamed() : Named? = if (type == NONAME) null else type

    companion object {
      @JsonCreator
      @JvmStatic private fun new(@JsonProperty("name") nm : String?,
                                 @JsonProperty("pkg") pkg : String?,
                                 @JsonProperty("type") type : Named?) =
        if (nm == null || nm.isEmpty()) VOID else {
          val v = TypedItem()
          v.name = nm
          v.pkg = pkg ?: ""
          v.type = type ?: NONAME
          v
        }
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
  }

  class ParamsArray : ArrayList<TypedItem>(5)

  class MethodItem : TypedItem {
    @JsonInclude( JsonInclude.Include.NON_NULL )
    @JvmField var src = NONAME
    @JsonProperty(value = "src")
    private fun getSrc() : Named? = if (src == NONAME) null else src

    @JvmField var params = ParamsArray()

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

  class MethodsArray : ArrayList<MethodItem>(10)

  override fun StoreArray(ar : List<BaseData.Named>, s : OutputStream) {
    ObjectMapper()
      .configure(SerializationFeature.INDENT_OUTPUT, false)
      .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
      .configure(SerializationFeature.WRAP_ROOT_VALUE, false)
      .configure(MapperFeature.AUTO_DETECT_FIELDS, true)
      .configure(MapperFeature.AUTO_DETECT_GETTERS, false)
      .configure(MapperFeature.AUTO_DETECT_SETTERS, false)
      .configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false)
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
      .writeValue(s, ar as MethodsArray)
  }

  override fun LoadArray(s : InputStream) : List<Named> {
    return ObjectMapper().readValue(s, MethodsArray::class.java)
  }

  override fun CloneData(ar : List<RandomSerialData.MethodItem>) : List<BaseData.Named> {
    val dest = MethodsArray()
    ar.forEach {
      dest.add( MethodItem(it) )
    }
    return dest
  }
}
