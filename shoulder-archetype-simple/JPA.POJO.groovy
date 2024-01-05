import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/**
 * 可以通过 IDEA 的数据库脚本功能，通过该模板生成 与数据库表对应的、满足JPA规范的 Entity
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

packageName = "cn.itlym.shoulder;"
typeMapping = [
    (~/(?i)bigint/)                   : "Long",
    (~/(?i)tinyint/)                  : "Boolean",
    (~/(?i)int/)                      : "Integer",
    (~/(?i)float|double|decimal|real/): "Double",
    (~/(?i)datetime|timestamp/)       : "java.sql.Date",
    (~/(?i)date/)                     : "java.sql.Date",
    (~/(?i)time/)                     : "java.sql.Time",
    (~/(?i)/)                         : "String"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = javaName(table.getName(), true)
    def fields = calcFields(table)
    new File(dir, className + ".java").withPrintWriter { out -> generate(out, className, fields, table) }
}

def generate(out, className, fields, table) {
    out.println "package $packageName"
    out.println ""
    out.println "import lombok.Data;"
    out.println "import jakarta.persistence.*;"
    out.println ""
    out.println "/**"
    out.println " * entity class for ${table.getName()}"
    if (isNotEmpty(table.getComment())) {
        out.println " * ${table.getComment()}"
    }
    out.println " * @author lym"
    out.println "*/"
    out.println "@Data"
    out.println "@Entity"
    out.println "@Table(name = \"${table.getName()}\")"
    out.println "public class $className {"
    out.println ""
    fields.each() {
        out.println "\t/**"
        out.println "\t* ${isNotEmpty(it.comment) ? it.comment : it.name}"
        out.println "\t*/"
        if (it.annos.size() > 0)
            it.annos.each() {
                out.println "\t${it}"
            }
        out.println "\tprivate ${it.type} ${it.name};"
    }
    out.println ""
    out.println "}"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        def anos = [];
        if (Case.LOWER.apply(col.getName()).equals('id')) {
            anos += ["@Id", "@GeneratedValue(strategy = GenerationType.IDENTITY)"]
        } else {
            anos += ["@Column(name = \"${col.getName()}\")"]
        }
        def field = [
            name : javaName(col.getName(), false),
            type : typeStr,
            comment: col.getComment(),
            annos: anos]
        fields += [field]
    }
}

def javaName(str, capitalize) {
    def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
        .collect { Case.LOWER.apply(it).capitalize() }
        .join("")
        .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
    capitalize || s.length() == 1? s : Case.LOWER.apply(s[0]) + s[1..-1]
}


def isNotEmpty(content) {
    return content != null && content.toString().trim().length() > 0
}

static String changeStyle(String str, boolean toCamel){
    if(!str || str.size() <= 1)
        return str

    if(toCamel){
        String r = str.toLowerCase().split('_').collect{cc -> Case.LOWER.apply(cc).capitalize()}.join('')
        return r[0].toLowerCase() + r[1..-1]
    }else{
        str = str[0].toLowerCase() + str[1..-1]
        return str.collect{cc -> ((char)cc).isUpperCase() ? '_' + cc.toLowerCase() : cc}.join('')
    }
}
