package com.springmvc.lvcr.commons;

import java.io.File;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CofigUtils {
	public static String getBasePackgName(String path) throws Exception {
		SAXReader Reader = new SAXReader();
//		DocumentFactory dfb = DocumentFactory.getInstance();
//		dfb.getInstance();
		File file = new File(path);
		Document document = Reader.read(file);
		// 获取xml文档根元素
		Element rootElemet = document.getRootElement();
		List<Element> childElements = rootElemet.elements();
		Element element = childElements.get(0);
		Attribute attribute = element.attribute("base-package");
		return attribute.getText();
	}

}
