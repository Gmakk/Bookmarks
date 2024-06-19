package org.example;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.RangeFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.*;
import org.example.formula.Formula;
import org.example.formula.FormulaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.example.database.Access;

/**
 * Replace text between w:bookmarkStarts and corresponding w:bookmarkEnds
 * with specified data, matching on bookmark's @w:name
 */
public class BookmarksReplaceWithText {

    //Основано на примере использования RangeFinder из репозитория библиотеки doc4j
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

    /**
     * Метод для подстановки значений на место закладок
     * @param paragraphs список параграфов документа
     * @throws SQLException Произошла ошибка при считывании данных из бд
     * @throws Exception В документе содержится закладка с некорректной формулой
     */
    public static void replaceBookmarkContent(List<Object> paragraphs) throws Exception {
        Access access = new Access();

        RangeFinder rt = new RangeFinder();
        new TraversalUtil(paragraphs, rt);

        for (CTBookmark bm : rt.getStarts()) {
            if (bm.getName()==null) continue;

            //Поднимаемся на уровень выше и далее среди дочерних элементов ищем начало, конец, их индексы, формулу
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

                //удаление старого содержимого закладки
                for (int j =rangeEnd; j>=rangeStart; j--) {
                    Object obj = XmlUtils.unwrap(theList.get(j));

                    //при закладке без размера, может находить сам себя
                    if (obj instanceof CTBookmark)  // Найдено начало пересекающейся закладки
                    {
                        log.warn("Overlapping bookmarks detected: " + bm.getName() + " and " + ((CTBookmark)obj).getName());
                    }
                    else if (obj instanceof CTMarkupRange)  // найден конец пересекающейся закладки
                    {
                        log.warn("Overlapping bookmarks detected: " + bm.getName() + " and " + ((CTMarkupRange)obj).getId());
                        //insertIndex++;
                    }
                    else
                    {
                        theList.remove(j);
                    }
                }

                //разбиваем строку с формулой на поля
                FormulaParser parser = new FormulaParser();
                parser.parse(formula);
                //по формуле получаем данные из бд
                String value = access.getData(parser);
                //подстановка нового текста на место старого
                theList.add(insertIndex, createSubstitutionRun(parser,value));

            }
            else
            {
                log.warn("Bookmark " + bm.getName() + " with formula " + formula + " doesn't appear to be valid; rangeStart=" + rangeStart + ", rangeEnd=" + rangeEnd + ". Probable cause: overlapping bookmarks or empty formula");
            }
        }
        //тк в одном документе могут несколько раз использоваться одни и те же подключения,
        // то они закрываются только после заполнения
        access.closeAllConnections();
    }

    /**
     * Метод по заданной формуле создает элемент для подстановки в документ
     * @param formula формула с правилами формирования текста
     * @param value текст подстановки
     * @return созданный run
     */
    private static R createSubstitutionRun(Formula formula, String value){
        org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
        org.docx4j.wml.R run = factory.createR();
        RPr rPr = factory.createRPr();
        RFonts fonts = factory.createRFonts();
        HpsMeasure hpsmeasure = factory.createHpsMeasure();//нужно для задания размера шрифта
        Color color = factory.createColor();

        //записываем текст в run
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

        //задаем форматирование текста по формуле
        if(formula != null && !formula.getSaveOldStyle()) {
            //устанавливаем жирный и курсивный текст
            if(formula.getIsCursive())
                rPr.setI(new BooleanDefaultTrue());//курсив
            if(formula.getIsBald())
                rPr.setB(new BooleanDefaultTrue());//жирный
            //подчеркивание
            if(formula.getIsUnderlined()){
                U u = factory.createU();
                u.setVal(org.docx4j.wml.UnderlineEnumeration.SINGLE);
                rPr.setU(u);
            }
            //выделение цветом
            if(!formula.getHighlighting().equals("absent")) {
                Highlight highlight = factory.createHighlight();
                highlight.setVal(formula.getHighlighting());
                rPr.setHighlight(highlight);
            }
            //цвет текста
            color.setVal(formula.getColor());//"#C0C0C0"
            rPr.setColor(color);
            //устанавливаем шрифт
            fonts.setAscii(formula.getFont());//"Arial"
            fonts.setHAnsi(formula.getFont());//"Arial"
            fonts.setCs(formula.getFont());//"Arial"
            rPr.setRFonts(fonts);
            //устанавливаем размер шрифта
            hpsmeasure.setVal(BigInteger.valueOf(formula.getFontSize() * 2));
            rPr.setSz(hpsmeasure);

            run.setRPr(rPr);

            //TODO: устанавливать стиль вышестоящему параграфу? Подтягивать возможные стили из /word/styles.xml?
//        //устанавливаем стиль заголовка
//        PPrBase.PStyle style = pPr.getPStyle();
//        style.setVal("1"); || style.setVal("11") || style.setVal("a0");
        }

        return run;
    }
}