package jm.test.Serialize

import com.eclipsesource.json.*
import jm.lib.iterator
import java.io.InputStream
import java.io.OutputStream

/* Manual serialization by JSON
   Lib: minimal-json
   Link: https://github.com/ralfstx/minimal-json
   Lib: minimal-json-0.9.4.jar
 */

// -------------------------------------------------------------------------
class TestJSONMini : RandomSerialData() {
  override fun Description() = "Manual serialization to JSON by minimal-json"
  override fun CloneData(ar : List<MethodItem>) = ar

  // -------------------------------------------------------------------------
  // SAVE
  // -------------------------------------------------------------------------
  fun outObj(to : JsonObject, nm : String, what : JsonObject?) {
    if (what == null) return
    to.set(nm, what)
  }

  fun OutNamed(p : Named) : JsonObject? {
    if (p.isEmpty()) return null
    val e = JsonObject()
    e.set("name", p.name)
    if (p.pkg.isNotEmpty()) e.set("pkg", p.pkg)
    return e
  }

  fun OutTyped(p : TypedItem) : JsonObject? {
    val e = OutNamed(p) ?: return null
    outObj(e,"type", OutNamed(p.type) )
    return e
  }

  fun OutMethod(p : MethodItem) : JsonObject? {
    val e = OutTyped(p) ?: return null
    outObj( e,"src", OutNamed(p.src) )
    if (p.params.isNotEmpty()) {
      val par = JsonArray()
      e.add("params", par)
      p.params.forEach {
        //compress: "{}" is 2 bytes but "null" is 4
        par.add(OutTyped(it) ?: JsonObject() /*Json.NULL*/)
      }
    }
    return e
  }

  override fun StoreArray(ar : List<BaseData.Named>, s : OutputStream) {
    val e = JsonArray()
    ar.forEach {
      e.add(OutMethod(it as MethodItem) ?: Json.NULL)
    }

    s.writer().use {
      e.writeTo(it, WriterConfig.MINIMAL)
    }
  }

  // -------------------------------------------------------------------------
  // LOAD
  // -------------------------------------------------------------------------
  fun JsonValue?.getObject() : JsonObject? =
    if (this != null && isObject) asObject() else null

  fun LoadNamed(e : JsonObject, nm : String) = LoadNamed(e.get(nm).getObject(), Named())
  fun LoadNamed(e : JsonObject?, p : Named) : Named {
    if (e == null) return NONAME
    p.name = e.get("name")?.asString() ?: ""
    if (p.name.isEmpty()) return NONAME
    p.pkg = e.get("pkg")?.asString() ?: ""
    return p
  }

  fun LoadTyped(e : JsonArray, nm : Int) = LoadTyped(e[nm].getObject(), TypedItem())
  fun LoadTyped(e : JsonObject?, p : TypedItem) : TypedItem {
    if (e == null) return VOID
    LoadNamed(e, p)
    if (p.isEmpty()) return VOID
    p.type = LoadNamed(e, "type")
    return p
  }

  fun LoadMethod(e : JsonObject) : MethodItem {
    val p = MethodItem()
    LoadTyped(e, p)
    p.src = LoadNamed(e, "src")

    val par = e.get("params").asArray() ?: return p
    for (n in par.size())
      p.params.add(LoadTyped(par, n))
    return p
  }

  override fun LoadArray(s : InputStream) : List<BaseData.Named> {
    val ar = mutableListOf<MethodItem>()
    s.reader().use {
      val e = (Json.parse(it) as? JsonArray) ?: throw RuntimeException("Expected array")
      for (n in e.size())
        ar.add(LoadMethod((e[n] as? JsonObject) ?: throw RuntimeException("Expected object")))
    }
    return ar
  }
}

