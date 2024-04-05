package org.example;

import lombok.Data;
import org.docx4j.wml.PPrBase;

@Data
public class FormulaCalculator {
    //сохранять ли старую стилизацию - по умолчанию нет
    private static Boolean saveOldStyle = false;

    //TODO: для выпадающего списка посмотреть возможность выводить имеющиеся в бд поля
    //откуда брать данные - ОБЯЗАТЕЛЬНЫЕ поля
    private static String database = null;
    private static String table = null;
    private static String field = null;
    private static String primaryKey = null;

    //форматирование - по умолчанию нет
    private static Boolean isHeader = null;//является ли текст заголовком

    //стилизация - по умолчанию нет
    private static Boolean isCursive = null;//курсив
    private static Boolean isBald = null;//выделен жирным
    //TODO: валидация введенного цвета
    //public static String highlighting = null;//желтые выделения   "Yellow", etc
    //public static String color = null;//цвет текста  "Red", etc


    //шрифты, размеры - ОБЯЗАТЕЛЬНО, если не указано сохранять стилизацию
    private static String font = null;//название шрифта "Times New Roman", etc
    private static Integer fontSize = null;//размер шрифта 12, etc

    //форматирование абзаца - ОБЯЗАТЕЛЬНО, если не указано сохранять стилизацию
//    PPrBase.Ind ind = factory.createPPrBaseInd();
//    ind.setFirstLine(new BigInteger("709"));
//    pPr.setInd(ind);
    //private static Boolean hasIndent = false;//есть ли отступ в начале нового параграфа
    //остальные свойства параграфа типа текст по ширине


    private static final FormulaCalculator INSTANCE = new FormulaCalculator();


    private FormulaCalculator(){
    }

    public static FormulaCalculator getInstance(){
        return INSTANCE;
    }


    public String calculate(){
        return null;
    }
}
