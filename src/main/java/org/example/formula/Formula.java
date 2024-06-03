package org.example.formula;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Formula {
    protected static final String SPLIT = "~";

    public Formula(){
    }

    //TODO: для выпадающего списка посмотреть возможность выводить имеющиеся в бд поля
    //откуда брать данные - ОБЯЗАТЕЛЬНЫЕ поля
    protected String url = null;
    protected String username = null;
    protected String password = null;
    protected String databaseType = null;

    protected String database = null;
    protected String table = null;
    protected String column = null;
    protected String primaryKey = null;
    protected String primaryKeyValue = null;

    //сохранять ли старую стилизацию - по умолчанию нет
    protected Boolean saveOldStyle = false;

    //форматирование - по умолчанию нет
    //TODO: убрать?
    protected Boolean isHeader = false;//является ли текст заголовком

    //стилизация - по умолчанию нет
    protected Boolean isCursive = false;//курсив
    protected Boolean isBald = false;//выделен жирным
    protected String highlighting = null;//желтые выделения   "Yellow", etc. "absent" - без выделения
    protected String color = null;//цвет текста  "Red", etc


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

    public void setDatabaseParams(String url, String username, String password, String databaseType,
                                  String database, String table, String column, String primaryKey, String primaryKeyValue){
        if (!database.isBlank() && !table.isBlank() && !column.isBlank() && !primaryKey.isBlank()
                && !url.isBlank() && !username.isBlank() && !password.isBlank()) {
            this.database = database;
            this.table = table;
            this.column = column;
            this.primaryKey = primaryKey;
            this.url = url;
            this.username = username;
            this.password = password;
            this.databaseType = databaseType;
            this.primaryKeyValue = primaryKeyValue;
        } else
            throw new IllegalArgumentException("Incorrect database params");
    }

    public void setFormatting(Boolean isHeader){
        if(isHeader != null) {
            this.isHeader = isHeader;
        } else
            throw new IllegalArgumentException("At least one of arguments is null");
    }

    public void setStyle(Boolean isCursive, Boolean isBald, String highlighting, String color){
        if(isCursive != null && isBald != null && highlighting != null && color != null) {
            this.isCursive = isCursive;//курсив
            this.isBald = isBald;//выделен жирным
            this.highlighting = highlighting;//желтые выделения
            this.color = color;//цвет текста
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

    /**
     * Метод для проверки того, что для подстановки значения в 2 формулы, нужен один Connection
     * @param formula вторая формула
     * @return true - имеет одинаковые параметры, false - нет
     */
    //TODO: добавить остальные поля
    public Boolean hasEqualDatabaseConnection(Formula formula){
        return this.url.equals(formula.url) && this.username.equals(formula.username) && this.password.equals(formula.password);
    }
}
