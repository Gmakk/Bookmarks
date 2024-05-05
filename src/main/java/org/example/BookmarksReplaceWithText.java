package org.example;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.RangeFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.example.formula.Formula;
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

                    //TODO: Проверить остальные варианты
                    Object content = ((R) listEntry).getContent().get(0);
                    if(content instanceof Text)
                        formula = ((Text)(content)).getValue();
                    if(content instanceof JAXBElement)
                        formula = ((Text)((JAXBElement)(content)).getValue()).getValue();
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

            if (rangeStart>0 && rangeEnd>=rangeStart && formula != null) {

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

                //TODO: передавать в этот метод не значение для подстановки, а брать его здесь из бд по формуле
                //если есть формула, то стилизация в соответствии с ее полями
                FormulaParser parser = new FormulaParser();
                parser.parse(formula);
                theList.add(insertIndex, createSubstitutionRun(parser,value));

            }
            else
            {
                log.warn("Bookmark " + bm.getName() + " with formula " + formula + " doesn't appear to be valid; rangeStart=" + rangeStart + ", rangeEnd=" + rangeEnd + ". Probable cause: overlapping bookmarks or empty formula");
            }
        }
    }

    /**
     * Метод по заданной формуле создает элемент для подстановки в документ
     * @param formula формула с правилами формирования элемента
     * @param value текст, содержащийся в элементе
     * @return созданный элемент
     */
    private static R createSubstitutionRun(Formula formula, String value){
        org.docx4j.wml.R run = factory.createR();
        RPr rPr = factory.createRPr();
        RFonts fonts = factory.createRFonts();
        HpsMeasure hpsmeasure = factory.createHpsMeasure();//нужно для задания размера шрифта
        Color color = factory.createColor();
        Highlight highlight = factory.createHighlight();

        String[] lines = value.split("\n");
        String lastLine = lines[lines.length - 1];

        // now add a run, replacing newline characters with BR tags
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

        if(formula != null && !formula.getSaveOldStyle()) {
            //устанавливаем жирный и курсивный текст
            if(formula.getIsCursive())
                rPr.setI(new BooleanDefaultTrue());//курсив
            if(formula.getIsBald())
                rPr.setB(new BooleanDefaultTrue());//жирный
            //выделение цветом
            if(!formula.getHighlighting().equals("absent"))
                highlight.setVal(formula.getHighlighting());
            //цвет текста
            color.setVal(formula.getColor());//"#C0C0C0"
            //устанавливаем шрифт
            fonts.setAscii(formula.getFont());//"Arial"
            fonts.setHAnsi(formula.getFont());//"Arial"
            fonts.setCs(formula.getFont());//"Arial"
            //устанавливаем размер шрифта
            hpsmeasure.setVal(BigInteger.valueOf(formula.getFontSize() * 2));


            run.setRPr(rPr);
            rPr.setRFonts(fonts);
            rPr.setSz(hpsmeasure);
            rPr.setColor(color);
            rPr.setHighlight(highlight);

            //TODO: устанавливать стиль вышестоящему параграфу? Подтягивать возможные стили из /word/styles.xml?
//        //устанавливаем стиль заголовка
//        PPrBase.PStyle style = pPr.getPStyle();
//        style.setVal("1"); || style.setVal("11") || style.setVal("a0");
        }

        return run;
    }
}