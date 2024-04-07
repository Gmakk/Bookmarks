package org.example;

import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.example.formula.Formula;
import org.example.formula.FormulaCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Main {
    protected static Logger log = LoggerFactory.getLogger(Main.class);

    public static String pathToFiles = System.getProperty("user.dir") + System.getProperty("file.separator") + "src"
            + System.getProperty("file.separator") + "main" + System.getProperty("file.separator") + "java"
            + System.getProperty("file.separator")+ "org" + System.getProperty("file.separator")
            + "example" + System.getProperty("file.separator");


    public static void main(String[] args) throws Exception {
        FormulaCalculator calculator = new FormulaCalculator();
        calculator.setDatabaseParams("database","table","field","key");
        calculator.setFont("Arial",15);
        calculator.setStyle(true,true);
        //calculator.setFormatting(true);
        log.info(calculator.toString());
        log.info(calculator.calculate());

        //добавление формул
        Map<DataFieldName, String> alterMap = new HashMap<DataFieldName, String>();
        alterMap.put( new DataFieldName("paragraph1"), calculator.calculate());
        //alterMap.put( new DataFieldName("paragraph1"), "test#4");
        alterMap.put( new DataFieldName("paragraph2"), "test#5");
        alterMap.put( new DataFieldName("DOCX"), "test#6");

        //подстановка по формулам
        Map<DataFieldName, String> replaceMap = new HashMap<DataFieldName, String>();
        replaceMap.put( new DataFieldName("paragraph1"), "parChange1");
        replaceMap.put( new DataFieldName("paragraph2"), "parChange2");
        replaceMap.put( new DataFieldName("DOCX"), "whale shark");



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

        //добавление формулы в невидимый элемент
        BookmarksAlterWithFormula.alterBookmarkContent(body.getContent(), alterMap);

        //замена текста закладки
        BookmarksReplaceWithText.replaceBookmarkContents(body.getContent(), replaceMap);

        // After
        //System.out.println(XmlUtils.marshaltoString(mainDocumentPart.getJaxbElement(), true, true));

        // save the docx...
        wordMLPackage.save(new File(pathToFiles + "templates"+ System.getProperty("file.separator") +"RESULT.docx"));
    }
}