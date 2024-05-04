package org.example;

import org.docx4j.TraversalUtil;
import org.docx4j.finders.RangeFinder;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTBookmark;
import org.example.formula.FormulaCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    protected static Logger log = LoggerFactory.getLogger(Test.class);

    private static String pathToFiles = System.getProperty("user.dir") + System.getProperty("file.separator") + "src"
            + System.getProperty("file.separator") + "main" + System.getProperty("file.separator") + "java"
            + System.getProperty("file.separator")+ "org" + System.getProperty("file.separator")
            + "example" + System.getProperty("file.separator");

    private static MainDocumentPart mainDocumentPart;

    public void test() throws Exception {
        FormulaCalculator calculator = new FormulaCalculator();
        calculator.setDatabaseParams("database","table","field","key");
        calculator.setFont("Arial",15);
        calculator.setStyle(true,true, "#FFFF00", "#000000");
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

    public static List<String> getBookmarkNames(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
        Body body = getDocumentBody(wordMLPackage);

        RangeFinder rt = new RangeFinder();
        new TraversalUtil(body.getContent(), rt);

        List<String> result = new ArrayList<>();

        for (CTBookmark bm : rt.getStarts()){
            result.add(bm.getName());
        }

        return result;
    }

    public static Body getDocumentBody(WordprocessingMLPackage wordMLPackage) throws Docx4JException {

        // Получаем главную часть документа
        MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();

        org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document) mainDocumentPart.getJaxbElement();//????????
        return wmlDocumentEl.getBody();
    }
}
