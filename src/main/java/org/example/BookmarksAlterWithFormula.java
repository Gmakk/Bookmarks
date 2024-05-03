package org.example;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.RangeFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class BookmarksAlterWithFormula {

    protected static Logger log = LoggerFactory.getLogger(BookmarksReplaceWithText.class);

    private static org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();


    /**
     * Метод для добавления невидимого run с формулой к закладке
     * run добавляется в родительский для закладки параграф
     * @param paragraphs список параграфов документа
     * @param data  значения имя_закладки - формула
     */
    public static void alterBookmarkContent(List<Object> paragraphs, Map<DataFieldName, String> data) throws Exception {

        RangeFinder rt = new RangeFinder();
        new TraversalUtil(paragraphs, rt);

        for (CTBookmark bm : rt.getStarts()) {

            //log.info(bm.getName());

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

            R existingRun = null;
            int insertIndex = -1;
            int i = 0;
            for (Object ox : theList) {
                Object listEntry = XmlUtils.unwrap(ox);
                //находим место для подстановки
                if (listEntry.equals(bm)) {
                    insertIndex=i;
                }
                //если скрытый элемент с формулой уже существует, то меняем его, а не создаем новый
                if (listEntry instanceof R && ((R) listEntry).getRPr() != null &&
                        ((R) listEntry).getRPr().getVanish() != null && ((R) listEntry).getRPr().getVanish().isVal()) {
                    existingRun = (R) listEntry;
                }
                i++;
            }


            // now add a run, replacing newline characters with BR tags
            org.docx4j.wml.R run;
            if(existingRun == null)
                run = factory.createR();
            else {
                run = existingRun;
                run.getContent().clear();
            }
            RPr rPr = factory.createRPr();
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

            rPr.setVanish(new BooleanDefaultTrue());
            run.setRPr(rPr);

            if(existingRun == null)
                theList.add(insertIndex, run);

        }
    }
}
