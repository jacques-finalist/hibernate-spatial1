package org.hibernatespatial.pojo;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * This class creates a Hibernate mapping file for a list of tables.
 * 
 * @author Karel Maesen, Geovise BVBA (http://www.geovise.com/)
 */
public class MappingsGenerator {

	private Document mappingDoc;
	private String packageName;
	
	public MappingsGenerator(String packageName){
		this.packageName = packageName;
	}
	
	public void write(Writer writer) throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter xmlWriter = new XMLWriter(writer, format);
		xmlWriter.write(this.mappingDoc);
		xmlWriter.close();
	}

	public Document getMappingsDoc() {
		return this.mappingDoc;
	}

	public void load(Collection<TableMetaData> tables, ClassInfoMap classInfoMap) throws PKeyNotFoundException {

		this.mappingDoc = DocumentHelper.createDocument();
		this.mappingDoc.addDocType("hibernate-mapping", "-//Hibernate/Hibernate Mapping DTD 3.0//EN", 
				"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd");
		Element root = this.mappingDoc.addElement("hibernate-mapping")
				.addAttribute("package", this.packageName);
		for(TableMetaData tmd : tables){
			addTableElement(root, classInfoMap.getClassInfo(tmd.getName()));			
		}
	}

	
	private void addTableElement(Element root, ClassInfo classInfo) throws PKeyNotFoundException{
		Element tableEl = root.addElement("class");
		tableEl.addAttribute("name", classInfo.getPOJOClass().getSimpleName());
		tableEl.addAttribute("table",classInfo.getTableName());
		AttributeInfo idAttr = classInfo.getIdAttribute();
		addColElement(tableEl,idAttr);
		for(AttributeInfo ai : classInfo.getAttributes()){
			if (!ai.isIdentifier()){
				addColElement (tableEl, ai);			
			}
		}
		
	}

	private void addColElement(Element tableEl, AttributeInfo ai) {
		Element colEl = null;
		if (ai.isIdentifier()){
			colEl = tableEl.addElement("id");		
		} else {
			colEl = tableEl.addElement("property");
		}
		colEl.addAttribute("name", ai.getFieldName());
		colEl.addAttribute("column", ai.getColumnName());
		colEl.addAttribute("type", ai.getHibernateType());
		return;
	}
}
