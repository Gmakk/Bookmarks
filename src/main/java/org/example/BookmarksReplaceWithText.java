package org.example;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.RangeFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.example.formula.FormulaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Replace text between w:bookmarkStarts and corresponding w:bookmarkEnds
 * with specified data, matching on bookmark's @w:name
 *
 */
public class BookmarksReplaceWithText {

    /*
     * Requirements:
     * - bookmarkStart and End must be in the same paragraph
     * - no attempt is made to check whether the start of some other bookmark is
     *   in that range.  If it is, it will get deleted!
     * - no attempt is made to preserve the rPr
     * - mdp only right now
     */

    protected static Logger log = LoggerFactory.getLogger(BookmarksReplaceWithText.class);

    private static boolean DELETE_BOOKMARK = false;

    private static org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

    //TODO: При проходе менять только те у коо есть формула

    /**
     * Метод для подстановки значений на место закладок
     * @param paragraphs список параграфов документа
     * @param data  значения имя_закладки - строка_подстановки
     */
    public static void replaceBookmarkContents(List<Object> paragraphs,  Map<DataFieldName, String> data) throws Exception {

        RangeFinder rt = new RangeFinder();
        new TraversalUtil(paragraphs, rt);

        for (CTBookmark bm : rt.getStarts()) {

            // do we have data for this one?
            if (bm.getName()==null) continue;
            String value = data.get(new DataFieldName(bm.getName()));
            if (value==null) continue;

            // Can't just remove the object from the parent,
            // since in the parent, it may be wrapped in a JAXBElement
            List<Object> theList = null;
            if (bm.getParent() instanceof P) {
                theList = ((ContentAccessor)(bm.getParent())).getContent();
            } else {
                continue;
            }

            String formula = null;
            int rangeStart = -1;
            int rangeEnd=-1;
            int i = 0;
            for (Object ox : theList) {
                Object listEntry = XmlUtils.unwrap(ox);
                //если нашли невидимый run, то достаем строку с формулой из него
                if (listEntry instanceof R && ((R) listEntry).getRPr() != null &&
                        ((R) listEntry).getRPr().getVanish() != null && ((R) listEntry).getRPr().getVanish().isVal()) {
                    formula = ((Text)(((R) listEntry).getContent().get(0))).getValue();
                }
                if (listEntry.equals(bm)) {
                    if (DELETE_BOOKMARK) {
                        rangeStart=i;
                    } else {
                        rangeStart=i+1;
                    }
                } else if (listEntry instanceof  CTMarkupRange) {
                    if ( ((CTMarkupRange)listEntry).getId().equals(bm.getId())) {
                        if (DELETE_BOOKMARK) {
                            rangeEnd=i;
                        } else {
                            //для непустых закладок
                            if(i > rangeStart)
                                rangeEnd=i-1;
                            //для пустых закладок
                            if(i == rangeStart)
                                rangeEnd=i;
                        }
                        break;
                    }
                }
                i++;
            }

            if (rangeStart>0 && rangeEnd>=rangeStart) {

                int insertIndex = rangeStart;

                // Delete the bookmark range
                for (int j =rangeEnd; j>=rangeStart; j--) {
                    Object obj = XmlUtils.unwrap(theList.get(j));

                    //при закладке без размера, может находить сам себя
                    if (obj instanceof CTBookmark)  // We found the start of an overlapping bookmark
                    {
                        log.warn("Overlapping bookmarks detected: " + bm.getName() + " and " + ((CTBookmark)obj).getName());
                    }
                    else if (obj instanceof CTMarkupRange)  // We found the end of an overlapping bookmark
                    {
                        log.warn("Overlapping bookmarks detected: " + bm.getName() + " and " + ((CTMarkupRange)obj).getId());
                        insertIndex++;
                    }
                    else
                    {
                        theList.remove(j);
                    }
                }

                //TODO: получить значение из бд по формуле
                if(formula != null)
                    log.info("formula" + formula + "" );

                FormulaParser parser = new FormulaParser();
                parser.parse(formula);

                // now add a run, replacing newline characters with BR tags
                theList.add(insertIndex, createSubstitutionRun(formula,value));
            }
            else
            {
                log.warn("Bookmark " + bm.getName() + " doesn't appear to be valid; rangeStart=" + rangeStart + ", rangeEnd=" + rangeEnd + ". Probable cause: overlapping bookmarks.");
            }
        }
    }

    /**
     * Метод по заданной формуле создает элемент для подстановки в документ
     * @param formula формула с правилами формирования элемента
     * @param value текст, содержащийся в элементе
     * @return созданный элемент
     */
    private static R createSubstitutionRun(String formula, String value){
        org.docx4j.wml.R run = factory.createR();
        RPr rPr = factory.createRPr();
        RFonts fonts = factory.createRFonts();
        HpsMeasure hpsmeasure = factory.createHpsMeasure();//нужно для задания размера шрифта
        String[] lines = value.split("\n");
        String lastLine = lines[lines.length - 1];

        for (final String line : lines)
        {
            org.docx4j.wml.Text  t = factory.createText();
            run.getContent().add(t);
            t.setValue(line);

            if (!line.equals(lastLine))
            {
                org.docx4j.wml.Br br = factory.createBr();
                run.getContent().add(br);
            }
        }

        //устанавливаем шрифт
        fonts.setAscii("Arial");
        fonts.setHAnsi("Arial");
        fonts.setCs("Arial");
        run.setRPr(rPr);
        rPr.setRFonts(fonts);
        //устанавливаем размер шрифта
        hpsmeasure.setVal(BigInteger.valueOf(30*2));
        rPr.setSz(hpsmeasure);
        //устанавливаем жирный и курсивный текст
        rPr.setB(new BooleanDefaultTrue());//жирный
        rPr.setI(new BooleanDefaultTrue());//курсив
        //TODO: устанавливать стиль вышестоящему параграфу? Подтягивать возможные стили из /word/styles.xml?
//        //устанавливаем стиль заголовка
//        PPrBase.PStyle style = pPr.getPStyle();
//        style.setVal("1"); || style.setVal("11") || style.setVal("a0");


        return run;
    }
}