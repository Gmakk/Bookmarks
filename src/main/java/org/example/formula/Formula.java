package org.example.formula;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Formula {
    protected static final String SPLIT = "#";

    public Formula(){
    }

    //TODO: для выпадающего списка посмотреть возможность выводить имеющиеся в бд поля
    //откуда брать данные - ОБЯЗАТЕЛЬНЫЕ поля
    protected String database = null;
    protected String table = null;
    protected String field = null;
    protected String primaryKey = null;

    //сохранять ли старую стилизацию - по умолчанию нет
    protected Boolean saveOldStyle = false;

    //форматирование - по умолчанию нет
    protected Boolean isHeader = false;//является ли текст заголовком

    //стилизация - по умолчанию нет
    protected Boolean isCursive = false;//курсив
    protected Boolean isBald = false;//выделен жирным
    //TODO: валидация введенного цвета
    //public static String highlighting = null;//желтые выделения   "Yellow", etc
    //public static String color = null;//цвет текста  "Red", etc


    //шрифты, размеры - ОБЯЗАТЕЛЬНО, если не указано сохранять стилизацию
    protected String font = null;//название шрифта "Times New Roman", etc
    protected Integer fontSize = null;//размер шрифта 12, etc

    //форматирование абзаца - ОБЯЗАТЕЛЬНО, если не указано сохранять стилизацию
//    PPrBase.Ind ind = factory.createPPrBaseInd();
//    ind.setFirstLine(new BigInteger("709"));
//    pPr.setInd(ind);
    //protected static Boolean hasIndent = false;//есть ли отступ в начале нового параграфа
    //остальные свойства параграфа типа текст по ширине


    public void setOldStyle(Boolean saveOldStyle){
        this.saveOldStyle = saveOldStyle;
    }

    public void setDatabaseParams(String database, String table, String field, String primaryKey){
        if (!database.isBlank() && !table.isBlank() && !field.isBlank() && !primaryKey.isBlank()) {
            this.database = database;
            this.table = table;
            this.field = field;
            this.primaryKey = primaryKey;
        } else
            throw new IllegalArgumentException("Incorrect database params");
    }

    public void setFormatting(Boolean isHeader){
        if(isHeader != null) {
            this.isHeader = isHeader;
        } else
            throw new IllegalArgumentException("At least one of arguments is null");
    }

    public void setStyle(Boolean isCursive, Boolean isBald){
        if(isCursive != null && isBald != null) {
            this.isCursive = isCursive;//курсив
            this.isBald = isBald;//выделен жирным
        } else
            throw new IllegalArgumentException("At least one of arguments is null");
    }

    public void setFont(String font, Integer fontSize){
        this.font = font;//название шрифта "Times New Roman", etc
        this.fontSize = fontSize;
    }

    public void setToDefault(){
        //TODO:
    }
}
