package org.example;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static String pathToFiles = System.getProperty("user.dir") + System.getProperty("file.separator") + "src"
            + System.getProperty("file.separator") + "main" + System.getProperty("file.separator") + "java"
            + System.getProperty("file.separator")+ "org" + System.getProperty("file.separator")
            + "example" + System.getProperty("file.separator");


    /*
    public static void main(String[] args) throws Exception {

        Map<DataFieldName, String> map = new HashMap<DataFieldName, String>();
        map.put( new DataFieldName("bm1"), "whale shark");


        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
                .load(new java.io.File(System.getProperty("user.dir")
                        + "/bm1.docx"));
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

        // Before..
        // System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));

        org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) documentPart
                .getJaxbElement();
        Body body = wmlDocumentEl.getBody();

        BookmarksReplaceWithText bti = new BookmarksReplaceWithText();

        bti.replaceBookmarkContents(body.getContent(), map);

        // After
        // System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));

        // save the docx...
        wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/OUT_BookmarksTextInserter.docx"));
    }
*/



    public static void main(String[] args) throws Exception {
        //BasicConfigurator.configure();

        Map<DataFieldName, String> map = new HashMap<DataFieldName, String>();
        map.put( new DataFieldName("paragraph1"), "parChange1");
        map.put( new DataFieldName("paragraph2"), "parChange2");
        map.put( new DataFieldName("DOCX"), "whale shark");

        // Открываем документ
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(pathToFiles + "templates"+ System.getProperty("file.separator") +"template.docx"));

        // Получаем главную часть документа
        MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();

        //содержимое главной части
        //List<Object> content = mainDocumentPart.getContent();//????????

        // Before..
        //System.out.println(XmlUtils.marshaltoString(mainDocumentPart.getJaxbElement(), true, true));

        org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) mainDocumentPart.getJaxbElement();//????????
        Body body = wmlDocumentEl.getBody();

        BookmarksReplaceWithText bti = new BookmarksReplaceWithText();

        bti.replaceBookmarkContents(body.getContent(), map);

        // After
        //System.out.println(XmlUtils.marshaltoString(mainDocumentPart.getJaxbElement(), true, true));

        // save the docx...
        wordMLPackage.save(new File(pathToFiles + "templates"+ System.getProperty("file.separator") +"RESULT.docx"));
    }
}