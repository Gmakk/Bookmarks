package org.example;

import org.docx4j.TraversalUtil;
import org.docx4j.finders.RangeFinder;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTBookmark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Files {
    protected static Logger log = LoggerFactory.getLogger(Files.class);

    public static List<String> getBookmarkNames(WordprocessingMLPackage wordMLPackage){
        Body body = getDocumentBody(wordMLPackage);

        RangeFinder rt = new RangeFinder();
        new TraversalUtil(body.getContent(), rt);

        List<String> result = new ArrayList<>();

        for (CTBookmark bm : rt.getStarts()){
            result.add(bm.getName());
        }

        return result;
    }

    public static Body getDocumentBody(WordprocessingMLPackage wordMLPackage){

        // Получаем главную часть документа
        MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();

        org.docx4j.wml.Document wmlDocumentEl = mainDocumentPart.getJaxbElement();
        return wmlDocumentEl.getBody();
    }
}
