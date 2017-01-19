package jm.test.Serialize

import jm.lib.console.progress
import jm.lib.console.warn
import net.n3.nanoxml.*
import java.io.InputStream
import java.io.OutputStream
import java.util.*


/* Serialization to XML using NanoXML library
   Lib: NanoXML
   Link: http://nanoxml.sourceforge.net
   Lib: nanoxml-2.2.3.jar
 */

// -------------------------------------------------------------------------
class TestNanoXML : RandomSerialData() {
  override fun Description() = "Serialization to XML using NanoXML"
  override fun CloneData(ar : List<MethodItem>) = ar

  // -------------------------------------------------------------------------
  // SAVE
  // -------------------------------------------------------------------------
  fun newElement(par : IXMLElement, nm : String) : IXMLElement {
    val v = par.createElement(nm)
    par.addChild( v )
    return v
  }

  fun addAttr(par : IXMLElement, name : String, v : String) = par.setAttribute(name, v)

  fun addName(e : IXMLElement, cl : Named) {
    if (cl.isNotEmpty()) addAttr(e, "name", cl.name)
    if (cl.pkg.isNotEmpty()) addAttr(e, "pkg", cl.pkg)
  }

  fun addName(par : IXMLElement, child : String, p : Named) {
    if (p.isEmpty()) return
    addName(newElement(par, child), p)
  }

  fun addTyped(par : IXMLElement, p : TypedItem) {
    if (p.isEmpty()) return
    addName(par, p)
    addName(par, "type", p.type)
  }

  fun OutMethod(e : IXMLElement, p : MethodItem) {
    addTyped(e, p)
    addName(newElement(e, "src"), p.src)

    if (p.params.isEmpty()) return
    p.params.forEach {
      addTyped(newElement(e, "param"), it)
    }
  }

  override fun StoreArray(ar : List<BaseData.Named>, s : OutputStream) {
    val root = XMLElement("ALL")
    root.setAttribute("count", ar.size.toString())

    val cn = ar.size
    ar.forEachIndexed { n, it ->
      progress("Storing %3d%% %d/%d...", n * 100 / cn, n, cn)
      OutMethod(newElement(root, "method"), (it as MethodItem))
    }

    XMLWriter(s).write(root)
  }

  // -------------------------------------------------------------------------
  // LOAD
  // -------------------------------------------------------------------------
  fun enumAttributes(e : IXMLElement, p : Named) {
    for (it in e.attributes) {
      val nm = it.key as String
      val v = it.value as String
      when (nm) {
        "name" -> p.name = v
        "pkg"  -> p.pkg = v
        else   -> warn("Unrecognized attribute ${nm}")
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun enumNodes(e : IXMLElement, p : Named?) {
    for (it in e.children as Vector<IXMLElement>)
      when (it.name) {
        "type"  -> {
          if (p !is TypedItem) error("Expected typed")
          val v = TypedItem()
          enumAttributes(it, v)
          p.type = if (v.isEmpty()) VOID else v
        }
        "src"   -> {
          if (p !is MethodItem) error("Expected method")
          val v = Named()
          enumAttributes(it, v)
          p.src = if (v.isEmpty()) NONAME else v
        }
        "param" -> {
          if (p !is MethodItem) error("Expected method")
          val v = TypedItem()
          enumAttributes(it, v)
          enumNodes(it, v)
          p.params.add(if (v.isEmpty()) VOID else v)
        }
        else    -> warn("Unrecognized node ${it.name}")
      }
  }

  @Suppress("UNCHECKED_CAST")
  override fun LoadArray(s : InputStream) : List<BaseData.Named> {
    val ar = ArrayList<BaseData.Named>()

    val parser = XMLParserFactory.createDefaultXMLParser()
    parser.reader = StdXMLReader(s)
    val xml = parser.parse() as IXMLElement

    for (it in xml.children as Vector<IXMLElement>) {
      if (it.name != "method") continue

      val cl = MethodItem()
      enumAttributes(it, cl)
      enumNodes(it, cl)
      ar.add(cl)

      progress("Loaded %d classes", ar.size)
    }
    return ar
  }
}

